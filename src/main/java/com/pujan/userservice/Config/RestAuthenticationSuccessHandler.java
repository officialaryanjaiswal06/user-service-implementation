package com.pujan.userservice.Config;

import com.pujan.userservice.Utils.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 2. Generate the JWT using the updated method
        String jwt = jwtUtils.generateTokenFromUsername(userDetails);

        String token = jwtUtils.generateTokenFromUsername(userDetails);
        Map<String,String> responseMap = new HashMap<>();
        responseMap.put("token",token);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(responseMap));
        response.getWriter().flush();


    }
}
