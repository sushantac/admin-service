package com.ecommerce.admin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "dashboard_snapshots")
public class DashboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_name", nullable = false, unique = true, length = 100)
    private String metricName;

    @Column(name = "metric_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal metricValue;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected DashboardSnapshot() {}

    public DashboardSnapshot(String metricName, BigDecimal metricValue, Instant recordedAt) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public String getMetricName() { return metricName; }
    public BigDecimal getMetricValue() { return metricValue; }
    public Instant getRecordedAt() { return recordedAt; }

    public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
