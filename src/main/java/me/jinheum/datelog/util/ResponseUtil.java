package me.jinheum.datelog.util;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

public class ResponseUtil {
    public static void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json, charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}