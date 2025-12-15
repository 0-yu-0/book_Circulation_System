import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import entity.bookInformation;
import entity.readerInformation;
import service.bookService;
import service.borrowService;
import service.readerService;
import service.returnService;
import db.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Server {
    // Jackson ObjectMapper for JSON handling
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Add CORS header to all responses
        server.createContext("/api", new CorsHandler());

        // Books
        server.createContext("/api/books", new BooksHandler());
        server.createContext("/api/readers", new ReadersHandler());
        server.createContext("/api/borrow", new BorrowHandler());
        server.createContext("/api/return", new ReturnHandler());
        // Auth and Statistics
        server.createContext("/api/auth", new AuthHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/logout", new LogoutHandler());
        server.createContext("/api/statistics", new StatisticsHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
        System.out.println("https://localhost:3000/");
    }

    // Handler helpers
    static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) result.put(pair[0], decode(pair[1]));
        }
        return result;
    }

    static String decode(String s) {
        return s.replace("%20", " ").replace("+", " ");
    }

    static String readBody(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        try (Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
            String body = s.hasNext() ? s.next() : "";
            return body;
        } finally {
            is.close();
        }
    }
    static void sendJson(HttpExchange ex, int code, Object obj) throws IOException {
        // Add CORS headers to all JSON responses
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        byte[] bytes = mapper.writeValueAsBytes(obj);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Unified response helpers (success/error) to ensure frontend/backend contract consistency
    static void sendOk(HttpExchange ex, Object data) throws IOException {
        Map<String,Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("data", data);
        sendJson(ex, 200, resp);
    }

    static void sendError(HttpExchange ex, int httpCode, int code, String message) throws IOException {
        Map<String,Object> resp = new HashMap<>();
        resp.put("code", code);
        resp.put("message", message);
        sendJson(ex, httpCode, resp);
    }
    
    // CORS handler to allow cross-origin requests
    // This handler only processes OPTIONS preflight requests
    static class CorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            // Add CORS headers to response
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            // Handle preflight OPTIONS request
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                ex.sendResponseHeaders(200, -1);
                ex.close();
                return;
            }
            
            // For non-OPTIONS requests to /api, return 404
            // This is expected as specific handlers will handle actual API paths
            ex.sendResponseHeaders(404, -1);
            ex.close();
        }
    }

    // Authentication helpers
    static String getAuthToken(HttpExchange ex) {
        String a = ex.getRequestHeaders().getFirst("Authorization");
        if (a == null) return null;
        if (a.startsWith("Bearer ")) return a.substring(7).trim();
        return null;
    }

    static boolean requireAuth(HttpExchange ex) throws IOException {
        String token = getAuthToken(ex);
        if (token == null || !service.authService.validateToken(token)) {
            // 对于OPTIONS请求，即使没有token也要放行，否则CORS预检会失败
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                return true;
            }
            sendJson(ex,401, Map.of("code",401,"message","unauthorized"));
            return false;
        }
        return true;
    }

    // Handlers
    static class BooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!requireAuth(ex)) return;
                String method = ex.getRequestMethod();
                URI uri = ex.getRequestURI();
                String path = uri.getPath();
                if ("GET".equalsIgnoreCase(method)) {
                    // /api/books or /api/books/{id}
                    String[] parts = path.split("/");
                    if (parts.length == 3 || (parts.length==4 && parts[3].isEmpty())) {
                        // list (support offset/limit and page/size)
                        Map<String, String> q = queryToMap(uri.getQuery());
                        int offset = 0; int limit = 20;
                        if (q.containsKey("page") && q.containsKey("size")) {
                            try { int page = Integer.parseInt(q.get("page")); int size = Integer.parseInt(q.get("size")); offset = Math.max(0, (page-1))*size; limit = size; } catch(Exception exx) { offset = Integer.parseInt(q.getOrDefault("offset", "0")); limit = Integer.parseInt(q.getOrDefault("limit", "20")); }
                        } else {
                            offset = Integer.parseInt(q.getOrDefault("offset", "0"));
                            limit = Integer.parseInt(q.getOrDefault("limit", "20"));
                        }
                        String search = q.get("search");
                        String author = q.get("author");
                        String category = q.get("category");
                        String availableOnly = q.get("availableOnly");
                        
                        List<bookInformation> items;
                        int total;
                        
                        if ("true".equalsIgnoreCase(availableOnly)) {
                            // 只获取可用图书
                            items = bookService.getVacantBooks(offset, limit);
                            total = bookService.countVacantBooks();
                        } else {
                            // 获取所有图书
                            items = bookService.listBooks(offset, limit, search, author, category);
                            total = bookService.countBooks(search, author, category);
                        }
                        
                        Map<String,Object> data = new HashMap<>();
                        data.put("items", items);
                        data.put("total", total);
                        sendOk(ex, data);
                        return;
                    } else if (parts.length >=4) {
                        String id = parts[3];
                        bookInformation b = bookService.getBookById(id);
                        if (b == null) { sendError(ex,404,1,"not found"); return; }
                        sendOk(ex, b);
                        return;
                    }
                }
                // PATCH for stock updates
                if ("PATCH".equalsIgnoreCase(method) && path.startsWith("/api/books/") && path.endsWith("/stock")) {
                    String[] parts = path.split("/");
                    if (parts.length>=5) {
                        String id = parts[3];
                        String body = readBody(ex);
                        Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                        if (m.get("adjustment")!=null) {
                            int adj = Integer.parseInt(String.valueOf(m.get("adjustment")));
                            boolean ok = bookService.adjustStock(id, adj);
                            if (ok) { sendJson(ex,200, Map.of("code",0)); } else { sendJson(ex,500, Map.of("code",1,"message","adjust failed")); }
                            return;
                        }
                        sendJson(ex,400, Map.of("code",400,"message","invalid body"));
                        return;
                    }
                }
                // POST Create
                if ("POST".equalsIgnoreCase(method) && (path.equals("/api/books") || path.equals("/api/books/"))) {
                    String body = readBody(ex);
                    Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                    bookInformation b = new bookInformation();
                    if (m.get("bookId")!=null) b.setBookId(String.valueOf(m.get("bookId")));
                    if (m.get("isbn")!=null) b.setIsbn(String.valueOf(m.get("isbn")));
                    if (m.get("bookName")!=null) b.setBookName(String.valueOf(m.get("bookName")));
                    if (m.get("bookAuthor")!=null) b.setBookAuthor(String.valueOf(m.get("bookAuthor")));
                    if (m.get("bookPublisher")!=null) b.setBookPublisher(String.valueOf(m.get("bookPublisher")));
                    if (m.get("bookPubDate")!=null) b.setBookPubDate(LocalDate.parse(String.valueOf(m.get("bookPubDate"))));
                    if (m.get("bookCategory")!=null) b.setBookCategory(String.valueOf(m.get("bookCategory")));
                    if (m.get("bookPrice")!=null) b.setBookPrice(Integer.parseInt(String.valueOf(m.get("bookPrice"))));
                    if (m.get("bookLocation")!=null) b.setBookLocation(String.valueOf(m.get("bookLocation")));
                    if (m.get("bookTotalCopies")!=null) b.setBookTotalCopies(Integer.parseInt(String.valueOf(m.get("bookTotalCopies"))));
                    if (m.get("bookAvailableCopies")!=null) b.setBookAvailableCopies(Integer.parseInt(String.valueOf(m.get("bookAvailableCopies"))));
                    if (m.get("borrowCount")!=null) b.setBorrowCount(Integer.parseInt(String.valueOf(m.get("borrowCount"))));
                    boolean ok = bookService.createBook(b);
                    if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","create failed"));
                    return;
                }

                // PUT update
                if ("PUT".equalsIgnoreCase(method) && path.startsWith("/api/books/")) {
                    String[] parts = path.split("/");
                    if (parts.length >=4) {
                        String id = parts[3];
                        String body = readBody(ex);
                        bookInformation b = bookService.getBookById(id);
                        if (b == null) { sendError(ex,404,1,"not found"); return; }
                        Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                        
                        // Check ISBN uniqueness before updating
                        if (m.get("isbn") != null) {
                            String newIsbn = String.valueOf(m.get("isbn"));
                            // Only check if ISBN is being changed
                            if (!newIsbn.equals(b.getIsbn())) {
                                // Check if new ISBN already exists in other books
                                bookInformation existingBook = bookService.getBookByIsbn(newIsbn);
                                if (existingBook != null && !existingBook.getBookId().equals(id)) {
                                    sendJson(ex, 400, Map.of("code", 400, "message", "ISBN already exists: " + newIsbn));
                                    return;
                                }
                            }
                            b.setIsbn(newIsbn);
                        }
                        
                        if (m.get("bookName")!=null) b.setBookName(String.valueOf(m.get("bookName")));
                        if (m.get("bookAuthor")!=null) b.setBookAuthor(String.valueOf(m.get("bookAuthor")));
                        if (m.get("bookPublisher")!=null) b.setBookPublisher(String.valueOf(m.get("bookPublisher")));
                        if (m.get("bookPubDate")!=null) b.setBookPubDate(LocalDate.parse(String.valueOf(m.get("bookPubDate"))));
                        if (m.get("bookCategory")!=null) b.setBookCategory(String.valueOf(m.get("bookCategory")));
                        if (m.get("bookPrice")!=null) b.setBookPrice(Integer.parseInt(String.valueOf(m.get("bookPrice"))));
                        if (m.get("bookLocation")!=null) b.setBookLocation(String.valueOf(m.get("bookLocation")));
                        if (m.get("bookTotalCopies")!=null) b.setBookTotalCopies(Integer.parseInt(String.valueOf(m.get("bookTotalCopies"))));
                        if (m.get("bookAvailableCopies")!=null) b.setBookAvailableCopies(Integer.parseInt(String.valueOf(m.get("bookAvailableCopies"))));
                        if (m.get("borrowCount")!=null) b.setBorrowCount(Integer.parseInt(String.valueOf(m.get("borrowCount"))));
                        boolean ok = bookService.updateBook(b);
                        if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","update failed"));
                        return;
                    }
                }

                // DELETE
                if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/api/books/")) {
                    String[] parts = path.split("/");
                    if (parts.length>=4) {
                        String id = parts[3];
                        try {
                            boolean ok = bookService.deleteBook(id);
                            if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","delete failed"));
                        } catch (SQLException e) {
                            sendJson(ex,500, Map.of("code",1,"message","Cannot delete book with borrowing records"));
                        }
                        return;
                    }
                }

                sendJson(ex,405, Map.of("code",405,"message","method not allowed"));
            } catch (SQLException se) {
                sendJson(ex,500, Map.of("code",500,"message", se.getMessage()));
            }
        }
    }

    static class ReadersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!requireAuth(ex)) return;
                String method = ex.getRequestMethod();
                URI uri = ex.getRequestURI();
                String path = uri.getPath();
                String[] parts = path.split("/");
                // support GET /api/readers/byCard/{cardNumber}
                if ("GET".equalsIgnoreCase(method) && parts.length>=5 && "byCard".equals(parts[3])) {
                    String card = parts[4];
                    readerInformation r = readerService.getReaderByCardNumber(card);
                    if (r==null) { sendJson(ex,404, Map.of("code",1,"message","not found")); return; }
                    sendOk(ex, r);
                    return;
                }
                // GET list or GET item
                if ("GET".equalsIgnoreCase(method)) {
                    if (parts.length == 3 || (parts.length==4 && parts[3].isEmpty())) {
                        Map<String,String> q = queryToMap(uri.getQuery());
                        int offset = 0; int limit = 20;
                        if (q.containsKey("page") && q.containsKey("size")) {
                            try { int page = Integer.parseInt(q.get("page")); int size = Integer.parseInt(q.get("size")); offset = Math.max(0,(page-1))*size; limit = size; } catch(Exception exx) { offset = Integer.parseInt(q.getOrDefault("offset","0")); limit = Integer.parseInt(q.getOrDefault("limit","20")); }
                        } else {
                            offset = Integer.parseInt(q.getOrDefault("offset","0"));
                            limit = Integer.parseInt(q.getOrDefault("limit","20"));
                        }
                        String search = q.get("search");
                        List<readerInformation> items = readerService.listReaders(offset, limit, search);
                        int total = readerService.countReaders(search);
                        Map<String,Object> data = new HashMap<>();
                        data.put("items", items);
                        data.put("total", total);
                        sendOk(ex, data);
                        return;
                    } else if (parts.length>=4) {
                        String id = parts[3];
                        readerInformation r = readerService.getReaderById(id);
                        if (r==null) { sendError(ex,404,1,"not found"); return; }
                        sendOk(ex, r);
                        return;
                    }
                }

                // POST create
                if ("POST".equalsIgnoreCase(method) && (path.equals("/api/readers") || path.equals("/api/readers/"))) {
                    String body = readBody(ex);
                    Map<String,Object> m = mapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});
                    
                    // Debug: log all received fields to help diagnose the issue
                    System.out.println("Received reader creation request with fields: " + m.keySet());
                    
                    readerInformation r = new readerInformation();
                    
                    // Handle readerName with better error handling and case insensitivity
                    String readerName = null;
                    if (m.get("readerName") != null) {
                        readerName = String.valueOf(m.get("readerName")).trim();
                    } else if (m.get("readername") != null) { // Handle case insensitive
                        readerName = String.valueOf(m.get("readername")).trim();
                    } else if (m.get("name") != null) { // Try common alternative field names
                        readerName = String.valueOf(m.get("name")).trim();
                    } else if (m.get("username") != null) { // Try common alternative field names
                        readerName = String.valueOf(m.get("username")).trim();
                    }
                    
                    if (readerName == null || readerName.isEmpty()) {
                        // Provide more detailed error message with available fields
                        sendJson(ex, 400, Map.of(
                            "code", 400, 
                            "message", "readerName is required", 
                            "availableFields", m.keySet(),
                            "suggestion", "Please include 'readerName' field in your request"
                        ));
                        return;
                    }
                    
                    r.setReaderName(readerName);
                    if (m.get("readerId")!=null) r.setReaderId(String.valueOf(m.get("readerId")));
                    if (m.get("readerCardType")!=null) r.setReaderCardType(String.valueOf(m.get("readerCardType")));
                    if (m.get("readerCardNumber")!=null) r.setReaderCardNumber(String.valueOf(m.get("readerCardNumber")));
                    if (m.get("readerPhoneNumber")!=null) r.setReaderPhoneNumber(String.valueOf(m.get("readerPhoneNumber")));
                    if (m.get("registerDate")!=null) r.setRegisterDate(LocalDate.parse(String.valueOf(m.get("registerDate"))));
                    if (m.get("readerStatus")!=null) r.setReaderStatus(Integer.parseInt(String.valueOf(m.get("readerStatus"))));
                    if (m.get("totalBorrowNumber")!=null) r.setMaxBorrowNumber(Integer.parseInt(String.valueOf(m.get("totalBorrowNumber"))));
                    if (m.get("nowBorrowNumber")!=null) r.setNowBorrowNumber(Integer.parseInt(String.valueOf(m.get("nowBorrowNumber"))));
                    boolean ok = readerService.createReader(r);
                    if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","create failed"));
                    return;
                }

                // PUT update
                if ("PUT".equalsIgnoreCase(method) && path.startsWith("/api/readers/")) {
                    String[] p = path.split("/");
                    if (p.length>=4) {
                        String id = p[3];
                        String body = readBody(ex);
                        readerInformation r = readerService.getReaderById(id);
                        if (r==null) { sendJson(ex,404, Map.of("code",1,"message","not found")); return; }
                        Map<String,Object> m = mapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});
                        if (m.get("readerName")!=null) r.setReaderName(String.valueOf(m.get("readerName")));
                        if (m.get("readerCardType")!=null) r.setReaderCardType(String.valueOf(m.get("readerCardType")));
                        if (m.get("readerCardNumber")!=null) r.setReaderCardNumber(String.valueOf(m.get("readerCardNumber")));
                        if (m.get("readerPhoneNumber")!=null) r.setReaderPhoneNumber(String.valueOf(m.get("readerPhoneNumber")));
                        if (m.get("registerDate")!=null) r.setRegisterDate(LocalDate.parse(String.valueOf(m.get("registerDate"))));
                        if (m.get("readerStatus")!=null) r.setReaderStatus(Integer.parseInt(String.valueOf(m.get("readerStatus"))));
                        if (m.get("totalBorrowNumber")!=null) r.setMaxBorrowNumber(Integer.parseInt(String.valueOf(m.get("totalBorrowNumber"))));
                        if (m.get("nowBorrowNumber")!=null) r.setNowBorrowNumber(Integer.parseInt(String.valueOf(m.get("nowBorrowNumber"))));
                        boolean ok = readerService.updateReader(r);
                        if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","update failed"));
                        return;
                    }
                }

                // DELETE
                if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/api/readers/")) {
                    String[] p = path.split("/");
                    if (p.length>=4) {
                        String id = p[3];
                        try {
                            boolean ok = readerService.deleteReader(id);
                            if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","delete failed"));
                        } catch (SQLException e) {
                            sendJson(ex,500, Map.of("code",1,"message","Cannot delete reader with borrowing records"));
                        }
                        return;
                    }
                }

                sendJson(ex,405, Map.of("code",405,"message","method not allowed"));
            } catch (SQLException se) {
                sendJson(ex,500, Map.of("code",500,"message", se.getMessage()));
            }
        }
    }

    static class BorrowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!requireAuth(ex)) return;
                String method = ex.getRequestMethod();
                URI uri = ex.getRequestURI();
                String path = uri.getPath();
                // GET /api/borrow or /api/borrow/record
                if ("GET".equalsIgnoreCase(method)) {
                    Map<String,String> q = queryToMap(uri.getQuery());
                    String readerId = q.get("readerId");
                    String statusS = q.get("status");
                    String bookTitle = q.get("bookTitle");
                    String borrowDateFrom = q.get("borrowDateFrom");
                    String borrowDateTo = q.get("borrowDateTo");
                    Integer status = null;
                    if (statusS != null) {
                        try { status = Integer.parseInt(statusS); } catch (Exception ex1) {
                            if ("borrowed".equalsIgnoreCase(statusS)) status = 0;
                            else if ("returned".equalsIgnoreCase(statusS)) status = 1;
                        }
                    }
                    int offset = 0; int limit = 20;
                    if (q.containsKey("page") && q.containsKey("size")) {
                        try { int page = Integer.parseInt(q.get("page")); int size = Integer.parseInt(q.get("size")); offset = Math.max(0,(page-1))*size; limit = size; } catch(Exception exx) { offset = Integer.parseInt(q.getOrDefault("offset","0")); limit = Integer.parseInt(q.getOrDefault("limit","20")); }
                    } else { offset = Integer.parseInt(q.getOrDefault("offset","0")); limit = Integer.parseInt(q.getOrDefault("limit","20")); }
                    var items = borrowService.listBorrows(readerId, status, bookTitle, borrowDateFrom, borrowDateTo, offset, limit);
                    int total = borrowService.countBorrows(readerId, status, bookTitle, borrowDateFrom, borrowDateTo);
                    sendOk(ex, Map.of("items", items, "total", total));
                    return;
                }

                // POST /api/borrow/refresh - 刷新借阅状态（优先处理）
                if ("POST".equalsIgnoreCase(method) && path.equals("/api/borrow/refresh")) {
                    int updatedCount = borrowService.refreshBorrowStatus();
                    sendOk(ex, Map.of("updatedCount", updatedCount, "message", "借阅状态刷新成功"));
                    return;
                }

                if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readBody(ex);
                    Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                    String readerId = m.get("readerId") == null ? "" : String.valueOf(m.get("readerId"));
                    String borrowDateS = m.get("borrowDate") == null ? "" : String.valueOf(m.get("borrowDate"));
                    String dueDateS = m.get("dueDate") == null ? "" : String.valueOf(m.get("dueDate"));
                    LocalDate borrowDate = borrowDateS.isEmpty() ? LocalDate.now() : LocalDate.parse(borrowDateS);
                    LocalDate dueDate = dueDateS.isEmpty() ? borrowDate.plusDays(14) : LocalDate.parse(dueDateS);
                    if (m.get("books") != null) {
                        java.util.List<?> books = (java.util.List<?>) m.get("books");
                        java.util.List<String> bookIds = new java.util.ArrayList<>();
                        for (Object o : books) {
                            if (o instanceof java.util.Map) {
                                Object bid = ((java.util.Map<?,?>)o).get("bookId");
                                if (bid != null) bookIds.add(String.valueOf(bid));
                            } else {
                                bookIds.add(String.valueOf(o));
                            }
                        }
                        var created = borrowService.createBorrowBatch(bookIds, readerId, borrowDate, dueDate);
                        sendOk(ex, Map.of("borrowIds", created));
                    } else {
                        String bookId = m.get("bookId") == null ? "" : String.valueOf(m.get("bookId"));
                        long borrowId = borrowService.createBorrowSingle(bookId, readerId, borrowDate, dueDate);
                        sendOk(ex, Map.of("borrowId", borrowId));
                    }
                     return;
                 }

                sendJson(ex,405, Map.of("code",405,"message","method not allowed"));
                return;
            } catch (SQLException se) {
                sendJson(ex,500, Map.of("code",500,"message", se.getMessage()));
            } catch (Exception e) {
                sendJson(ex,400, Map.of("code",400,"message","invalid request"));
            }
        }
    }

    static class ReturnHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!requireAuth(ex)) return;
                if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    sendJson(ex,405, Map.of("code",405,"message","method not allowed"));
                    return;
                }
                String body = readBody(ex);
                Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                String returnDateS = m.get("returnDate") == null ? "" : String.valueOf(m.get("returnDate"));
                LocalDate returnDate = returnDateS.isEmpty() ? LocalDate.now() : LocalDate.parse(returnDateS);
                if (m.get("borrowIds") != null) {
                    java.util.List<?> ids = (java.util.List<?>) m.get("borrowIds");
                    java.util.List<String> borrowIds = new java.util.ArrayList<>();
                    for (Object o : ids) borrowIds.add(String.valueOf(o));
                    var result = returnService.createReturnBatch(borrowIds, returnDate);
                    sendOk(ex, Map.of("returned", result));
                } else {
                    String borrowId = m.get("borrowId") == null ? "" : String.valueOf(m.get("borrowId"));
                    long returnId = returnService.createReturn(borrowId, returnDate);
                    sendOk(ex, Map.of("returnId", returnId));
                }
             } catch (SQLException se) {
                 sendJson(ex,500, Map.of("code",500,"message", se.getMessage()));
             } catch (Exception e) {
                 sendJson(ex,400, Map.of("code",400,"message","invalid request"));
             }
         }
     }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    sendJson(ex,405, Map.of("code",405,"message","method not allowed"));
                    return;
                }
                String body = readBody(ex);
                Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                String username = m.get("username") == null ? "" : String.valueOf(m.get("username"));
                String password = m.get("password") == null ? "" : String.valueOf(m.get("password"));
                String token = service.authService.login(username, password);
                if (token == null) {
                    sendJson(ex,401, Map.of("code",1,"message","invalid credentials"));
                    return;
                }
                sendJson(ex,200, Map.of("code",0,"data", Map.of("token", token)));
            } catch (Exception e) {
                sendJson(ex,400, Map.of("code",400,"message","invalid request"));
            }
        }
    }

    // New explicit login/logout handlers that match frontend paths
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { sendJson(ex,405, Map.of("code",405,"message","method not allowed")); return; }
                String body = readBody(ex);
                Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                String username = m.get("username") == null ? "" : String.valueOf(m.get("username"));
                String password = m.get("password") == null ? "" : String.valueOf(m.get("password"));
                String token = service.authService.login(username, password);
                if (token == null) { sendJson(ex,401, Map.of("code",1,"message","invalid credentials")); return; }
                sendJson(ex,200, Map.of("code",0,"data", Map.of("token", token)));
            } catch (Exception e) {
                sendJson(ex,400, Map.of("code",400,"message","invalid request"));
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { sendJson(ex,405, Map.of("code",405,"message","method not allowed")); return; }
                String token = getAuthToken(ex);
                // simple: remove token if exists
                if (token != null) { /* authService doesn't expose remove, but we can accept logout as client-side action */ }
                sendJson(ex,200, Map.of("code",0));
            } catch (Exception e) {
                sendJson(ex,400, Map.of("code",400,"message","invalid request"));
            }
        }
    }

    static class StatisticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (!requireAuth(ex)) return;
                String method = ex.getRequestMethod();
                if (!"GET".equalsIgnoreCase(method)) { sendJson(ex,405, Map.of("code",405,"message","method not allowed")); return; }
                URI uri = ex.getRequestURI();
                String path = uri.getPath();
                String[] parts = path.split("/");
                if (parts.length < 4) { sendJson(ex,400, Map.of("code",400,"message","no statistic type")); return; }
                String type = parts[3];
                Map<String,String> q = queryToMap(uri.getQuery());
                // accept frontend naming too
                if ("popular".equalsIgnoreCase(type) || "popular-books".equalsIgnoreCase(type)) {
                    int top = Integer.parseInt(q.getOrDefault("top", "10"));
                    var items = bookService.getPopularBooks(top);
                    sendJson(ex,200, Map.of("code",0, "data", Map.of("items", items)));
                    return;
                } else if ("vacant".equalsIgnoreCase(type) || "vacant-books".equalsIgnoreCase(type)) {
                    int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                    int limit = Integer.parseInt(q.getOrDefault("limit","20"));
                    var items = bookService.getVacantBooks(offset, limit);
                    int total = bookService.countVacantBooks();
                    Map<String,Object> data = new HashMap<>();
                    data.put("items", items);
                    data.put("total", total);
                    sendOk(ex, data);
                    return;
                } else if ("overdue".equalsIgnoreCase(type) || "overdue-books".equalsIgnoreCase(type)) {
                    int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                    int limit = Integer.parseInt(q.getOrDefault("limit","20"));
                    String readerName = q.get("readerName");
                    String bookTitle = q.get("bookTitle");
                    var items = borrowService.getOverdueList(readerName, bookTitle, offset, limit);
                    int total = borrowService.countOverdue(readerName, bookTitle);
                    Map<String,Object> data = new HashMap<>();
                    data.put("items", items);
                    data.put("total", total);
                    sendOk(ex, data);
                    return;
                } else if ("overview".equalsIgnoreCase(type)) {
                    // compute simple overview metrics via direct queries
                    try (java.sql.Connection c = db.getConnection()) {
                        int totalBooks = 0; int totalReaders = 0; int borrowedNow = 0; int overdue = 0;
                        int todayBorrows = 0; int todayReturns = 0;
                        
                        // 修改：统计图书总册数而不是图书种类数
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT SUM(bookTotalCopies) FROM bookInformation")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) totalBooks = rs.getInt(1); }
                        }
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM readerInformation")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) totalReaders = rs.getInt(1); }
                        }
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM borrowTable WHERE borrowStates = 0")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) borrowedNow = rs.getInt(1); }
                        }
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM borrowTable WHERE borrowStates = 0 AND dueDate < CURRENT_DATE()")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) overdue = rs.getInt(1); }
                        }
                        // 今日借书：统计今天借出的图书数量
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM borrowTable WHERE DATE(borrowDate) = CURRENT_DATE()")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) todayBorrows = rs.getInt(1); }
                        }
                        // 今日还书：统计今天归还的图书数量
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM returnTable WHERE DATE(returnDate) = CURRENT_DATE()")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) todayReturns = rs.getInt(1); }
                        }
                        
                        sendJson(ex,200, Map.of("code",0, "data", Map.of(
                            "totalBooks", totalBooks, 
                            "totalReaders", totalReaders, 
                            "borrowedNow", borrowedNow, 
                            "overdue", overdue,
                            "todayBorrows", todayBorrows,
                            "todayReturns", todayReturns
                        )));
                        return;
                    }
                } else if ("borrow-details".equalsIgnoreCase(type)) {
                    // expected path: /api/statistics/borrow-details/{readerId}
                    if (parts.length>=5) {
                        String readerId = parts[4];
                        int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                        int limit = Integer.parseInt(q.getOrDefault("limit","100"));
                        var items = borrowService.listBorrows(readerId, null, null, null, null, offset, limit);
                        sendJson(ex,200, Map.of("code",0, "data", Map.of("items", items)));
                        return;
                    } else { sendJson(ex,400, Map.of("code",400,"message","reader id required")); return; }
                } else {
                    sendJson(ex,404, Map.of("code",1,"message","unknown statistic type"));
                    return;
                }
            } catch (SQLException se) {
                sendJson(ex,500, Map.of("code",500,"message", se.getMessage()));
            } catch (Exception e) {
                sendJson(ex,400, Map.of("code",400,"message","invalid request"));
            }
        }
    }

    // no manual JSON extractor needed; Jackson is used instead

}
