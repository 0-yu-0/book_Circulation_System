package service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单认证服务（用于开发/测试）：
 * - 支持用户名/密码登录（硬编码一个 admin 用户）
 * - 返回一个随机 token（UUID），并在内存中保存映射
 * 注意：生产环境请使用 JWT 或更完善的认证方案。
 */
public class authService {

    private static final Map<String,String> users = new ConcurrentHashMap<>(); // username -> password
    private static final Map<String,String> tokenToUser = new ConcurrentHashMap<>(); // token -> username

    static {
        // 默认测试账户
        users.put("admin", "password");
    }

    public static String login(String username, String password) {
        if (username == null || password == null) return null;
        String pw = users.get(username);
        if (pw != null && pw.equals(password)) {
            String token = UUID.randomUUID().toString();
            tokenToUser.put(token, username);
            return token;
        }
        return null;
    }

    public static boolean validateToken(String token) {
        return token != null && tokenToUser.containsKey(token);
    }

    public static String getUsername(String token) {
        return tokenToUser.get(token);
    }

}
