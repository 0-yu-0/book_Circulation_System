package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQL脚本执行工具：执行指定的SQL文件
 */
public class SqlRunner {

    public static void runSqlFile(String filePath) throws Exception {
        try (Connection conn = db.getConnection(); 
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            
            System.out.println("Connected to: " + conn.getMetaData().getURL());
            System.out.println("Running SQL file: " + filePath);
            
            StringBuilder statementBuffer = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过注释和空行
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                statementBuffer.append(line).append(" ");
                
                // 如果语句以分号结尾，则执行
                if (line.endsWith(";")) {
                    String sql = statementBuffer.toString().trim();
                    if (!sql.isEmpty() && !sql.equals(";")) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                            System.out.println("Executed: " + sql.substring(0, Math.min(sql.length(), 50)) + "...");
                        } catch (SQLException e) {
                            System.err.println("Error executing: " + sql);
                            System.err.println("Error: " + e.getMessage());
                        }
                    }
                    statementBuffer.setLength(0); // 清空缓冲区
                }
            }
            
            System.out.println("SQL file executed successfully.");
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java db.SqlRunner <sql_file_path>");
            return;
        }
        
        try {
            runSqlFile(args[0]);
        } catch (Exception e) {
            System.err.println("Failed to run SQL file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}