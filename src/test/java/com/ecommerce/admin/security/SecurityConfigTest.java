package com.ecommerce.admin.security;

import com.ecommerce.admin.config.SecurityConfig;
import com.ecommerce.admin.controller.AdminController;
import com.ecommerce.admin.exception.GlobalExceptionHandler;
import com.ecommerce.admin.service.AuditService;
import com.ecommerce.admin.service.DashboardService;
import com.ecommerce.admin.service.OrderProjectionService;
import com.ecommerce.admin.service.UserProjectionService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private DashboardService dashboardService;
    @MockitoBean private OrderProjectionService orderProjectionService;
    @MockitoBean private UserProjectionService userProjectionService;
    @MockitoBean private AuditService auditService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private String generateToken(String subject, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Test
    void dashboard_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboard_customerRole_returns403() throws Exception {
        String token = generateToken("user-1", "CUSTOMER");
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboard_adminRole_returns200() throws Exception {
        String token = generateToken("admin-1", "ADMIN");
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void orders_customerRole_returns403() throws Exception {
        String token = generateToken("user-1", "CUSTOMER");
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void orders_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isUnauthorized());
    }
}
