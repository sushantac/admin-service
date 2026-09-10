package com.ecommerce.admin.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_projections")
public class UserProjection {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    protected UserProjection() {}

    public UserProjection(Long userId, String email, String fullName, Instant registeredAt) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.registeredAt = registeredAt;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Instant getRegisteredAt() { return registeredAt; }
}
