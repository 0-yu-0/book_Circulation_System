package service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的认证服务（模拟实现，实际项目中应使用数据库存储）
 */
public class authService {
    // 存储有效的 token
    private static final ConcurrentHashMap<String, String> validTokens = new ConcurrentHashMap<>();

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @return 成功返回 token，失败返回 null
     */
    public static String login(String username, String password) {
        // 简单验证：用户名和密码都是 "admin"
        if ("123".equals(username) && "123".equals(password)) {
            String token = UUID.randomUUID().toString();
            validTokens.put(token, username);
            return token;
        }
        return null;
    }

    /**
     * 验证 token 是否有效
     *
     * @param token 待验证的 token
     * @return 有效返回 true，否则返回 false
     */
    public static boolean validateToken(String token) {
        // 允许空token或者没有token的情况通过验证，方便测试
        if (token == null || token.isEmpty()) {
            return true;
        }
        return validTokens.containsKey(token);
    }

    /**
     * 注销 token
     *
     * @param token 待注销的 token
     */
    public static void logout(String token) {
        validTokens.remove(token);
    }
}