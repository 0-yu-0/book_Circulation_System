
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
        byte[] bytes = mapper.writeValueAsBytes(obj);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
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
                        // list
                        Map<String, String> q = queryToMap(uri.getQuery());
                        int offset = Integer.parseInt(q.getOrDefault("offset", "0"));
                        int limit = Integer.parseInt(q.getOrDefault("limit", "20"));
                        String search = q.get("search");
                        String author = q.get("author");
                        String category = q.get("category");
                        List<bookInformation> items = bookService.listBooks(offset, limit, search, author, category);
                        Map<String,Object> data = new HashMap<>();
                        data.put("items", items);
                        data.put("total", items.size());
                        Map<String,Object> resp = new HashMap<>();
                        resp.put("code", 0);
                        resp.put("data", data);
                        sendJson(ex,200, resp);
                        return;
                    } else if (parts.length >=4) {
                        String id = parts[3];
                        bookInformation b = bookService.getBookById(id);
                        if (b == null) { sendJson(ex,404, Map.of("code",1,"message","not found")); return; }
                        sendJson(ex,200, b);
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
                        if (b == null) { sendJson(ex,404,"{\"code\":1,\"message\":\"not found\"}"); return; }
                        Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
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
                        boolean ok = bookService.deleteBook(id);
                        if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","delete failed"));
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
                    sendJson(ex,200, r);
                    return;
                }
                // GET list or GET item
                if ("GET".equalsIgnoreCase(method)) {
                    if (parts.length == 3 || (parts.length==4 && parts[3].isEmpty())) {
                        Map<String,String> q = queryToMap(uri.getQuery());
                        int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                        int limit = Integer.parseInt(q.getOrDefault("limit","20"));
                        String search = q.get("search");
                        List<readerInformation> items = readerService.listReaders(offset, limit, search);
                        Map<String,Object> data = new HashMap<>();
                        data.put("items", items);
                        data.put("total", items.size());
                        sendJson(ex,200, Map.of("code",0, "data", data));
                        return;
                    } else if (parts.length>=4) {
                        String id = parts[3];
                        readerInformation r = readerService.getReaderById(id);
                        if (r==null) { sendJson(ex,404, Map.of("code",1,"message","not found")); return; }
                        sendJson(ex,200, r);
                        return;
                    }
                }

                // POST create
                if ("POST".equalsIgnoreCase(method) && (path.equals("/api/readers") || path.equals("/api/readers/"))) {
                    String body = readBody(ex);
                    Map<String,Object> m = mapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});
                    readerInformation r = new readerInformation();
                    if (m.get("readerId")!=null) r.setReaderId(String.valueOf(m.get("readerId")));
                    if (m.get("readerName")!=null) r.setReaderName(String.valueOf(m.get("readerName")));
                    if (m.get("readerCardType")!=null) r.setReaderCardType(String.valueOf(m.get("readerCardType")));
                    if (m.get("readerCardNumber")!=null) r.setReaderCardNumber(String.valueOf(m.get("readerCardNumber")));
                    if (m.get("readerPhoneNumber")!=null) r.setReaderPhoneNumber(String.valueOf(m.get("readerPhoneNumber")));
                    if (m.get("registerDate")!=null) r.setRegisterDate(LocalDate.parse(String.valueOf(m.get("registerDate"))));
                    if (m.get("readerStatus")!=null) r.setReaderStatus(Integer.parseInt(String.valueOf(m.get("readerStatus"))));
                    if (m.get("totalBorrowNumber")!=null) r.setTotalBorrowNumber(Integer.parseInt(String.valueOf(m.get("totalBorrowNumber"))));
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
                        if (m.get("totalBorrowNumber")!=null) r.setTotalBorrowNumber(Integer.parseInt(String.valueOf(m.get("totalBorrowNumber"))));
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
                        boolean ok = readerService.deleteReader(id);
                        if (ok) sendJson(ex,200, Map.of("code",0)); else sendJson(ex,500, Map.of("code",1,"message","delete failed"));
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
                    Integer status = null;
                    if (statusS != null) {
                        try { status = Integer.parseInt(statusS); } catch (Exception ex1) {
                            if ("borrowed".equalsIgnoreCase(statusS)) status = 0;
                            else if ("returned".equalsIgnoreCase(statusS)) status = 1;
                        }
                    }
                    int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                    int limit = Integer.parseInt(q.getOrDefault("limit","20"));
                    var items = borrowService.listBorrows(readerId, status, offset, limit);
                    sendJson(ex,200, Map.of("code",0, "data", Map.of("items", items, "total", items.size())));
                    return;
                }

                if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readBody(ex);
                    Map<String,Object> m = mapper.readValue(body, new TypeReference<Map<String,Object>>(){});
                    String readerId = m.get("readerId") == null ? "" : String.valueOf(m.get("readerId"));
                    String bookId = m.get("bookId") == null ? "" : String.valueOf(m.get("bookId"));
                    String borrowDateS = m.get("borrowDate") == null ? "" : String.valueOf(m.get("borrowDate"));
                    String dueDateS = m.get("dueDate") == null ? "" : String.valueOf(m.get("dueDate"));
                    LocalDate borrowDate = borrowDateS.isEmpty() ? LocalDate.now() : LocalDate.parse(borrowDateS);
                    LocalDate dueDate = dueDateS.isEmpty() ? borrowDate.plusDays(14) : LocalDate.parse(dueDateS);
                    long borrowId = borrowService.createBorrowSingle(bookId, readerId, borrowDate, dueDate);
                    sendJson(ex,200, Map.of("code",0, "data", Map.of("borrowId", borrowId)));
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
                String borrowIdS = m.get("borrowId") == null ? "" : String.valueOf(m.get("borrowId"));
                String returnDateS = m.get("returnDate") == null ? "" : String.valueOf(m.get("returnDate"));
                long borrowId = Long.parseLong(borrowIdS);
                LocalDate returnDate = returnDateS.isEmpty() ? LocalDate.now() : LocalDate.parse(returnDateS);
                long returnId = returnService.createReturn(borrowId, returnDate);
                sendJson(ex,200, Map.of("code",0, "data", Map.of("returnId", returnId)));
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
                    var items = service.bookService.getPopularBooks(top);
                    sendJson(ex,200, Map.of("code",0, "data", Map.of("items", items)));
                    return;
                } else if ("vacant".equalsIgnoreCase(type) || "vacant-books".equalsIgnoreCase(type)) {
                    int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                    int limit = Integer.parseInt(q.getOrDefault("limit","20"));
                    var items = service.bookService.getVacantBooks(offset, limit);
                    sendJson(ex,200, Map.of("code",0, "data", Map.of("items", items)));
                    return;
                } else if ("overdue".equalsIgnoreCase(type) || "overdue-books".equalsIgnoreCase(type)) {
                    int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                    int limit = Integer.parseInt(q.getOrDefault("limit","20"));
                    var items = service.borrowService.getOverdueList(offset, limit);
                    sendJson(ex,200, Map.of("code",0, "data", Map.of("items", items)));
                    return;
                } else if ("overview".equalsIgnoreCase(type)) {
                    // compute simple overview metrics via direct queries
                    try (java.sql.Connection c = db.getConnection()) {
                        int totalBooks = 0; int totalReaders = 0; int borrowedNow = 0; int overdue = 0;
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM bookInformation")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) totalBooks = rs.getInt(1); }
                        }
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM readerInformation")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) totalReaders = rs.getInt(1); }
                        }
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM borrowTable WHERE borrowStatus = 0")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) borrowedNow = rs.getInt(1); }
                        }
                        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM borrowTable WHERE borrowStatus = 0 AND dueDate < CURRENT_DATE()")) {
                            try (java.sql.ResultSet rs = ps.executeQuery()) { if (rs.next()) overdue = rs.getInt(1); }
                        }
                        sendJson(ex,200, Map.of("code",0, "data", Map.of("totalBooks", totalBooks, "totalReaders", totalReaders, "borrowedNow", borrowedNow, "overdue", overdue)));
                        return;
                    }
                } else if ("borrow-details".equalsIgnoreCase(type)) {
                    // expected path: /api/statistics/borrow-details/{readerId}
                    if (parts.length>=5) {
                        String readerId = parts[4];
                        int offset = Integer.parseInt(q.getOrDefault("offset","0"));
                        int limit = Integer.parseInt(q.getOrDefault("limit","100"));
                        var items = borrowService.listBorrows(readerId, null, offset, limit);
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
