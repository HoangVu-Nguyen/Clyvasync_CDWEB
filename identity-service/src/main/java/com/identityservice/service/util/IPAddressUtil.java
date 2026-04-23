package com.identityservice.service.util;

import jakarta.servlet.http.HttpServletRequest;

public class IPAddressUtil {

    public static String getClientIp(HttpServletRequest request) {

        String ip = extractHeader(request, "X-Forwarded-For");

        if (isInvalid(ip)) {
            ip = extractHeader(request, "X-Real-IP");
        }

        if (isInvalid(ip)) {
            ip = extractHeader(request, "Proxy-Client-IP");
        }

        if (isInvalid(ip)) {
            ip = extractHeader(request, "WL-Proxy-Client-IP");
        }

        if (isInvalid(ip)) {
            ip = request.getRemoteAddr();
        }

        // Nếu có nhiều IP (proxy chain)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // Chuẩn hoá localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    public static String extractHeader(HttpServletRequest request, String header) {
        return request.getHeader(header);
    }

    public static boolean isInvalid(String ip) {
        return ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip);
    }
    public static String parseDeviceName(String userAgent) {
        if (userAgent == null) return "Unknown Device";
        if (userAgent.contains("iPhone")) return "iPhone";
        if (userAgent.contains("iPad")) return "iPad";
        if (userAgent.contains("Macintosh")) return "MacBook";
        if (userAgent.contains("Windows")) return "Windows PC";
        if (userAgent.contains("Android")) return "Android Device";
        if (userAgent.contains("Linux")) return "Linux PC";
        return "Web Browser";
    }

    public static String parseDeviceType(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            return "MOBILE";
        }
        return "DESKTOP";
    }
    public static String formatDeviceName(String rawName) {
        if (rawName == null) return "Thiết bị không xác định";
        if (rawName.contains("Macintosh")) return "Máy Mac";
        if (rawName.contains("Windows")) return "Máy tính Windows";
        if (rawName.contains("iPhone")) return "iPhone";
        return rawName; // Trả về nguyên gốc nếu không khớp rule nào
    }

    // Helper: Xác định loại để FE hiện icon (Desktop/Mobile)
    public static String determineIconType(String name, String type) {
        if (type != null && type.equals("MOBILE")) return "MOBILE";
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.contains("iphone") || lower.contains("android") || lower.contains("mobile")) {
                return "MOBILE";
            }
        }
        return "DESKTOP";
    }
}
