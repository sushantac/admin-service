package com.ecommerce.admin.repository;

import com.ecommerce.admin.entity.DashboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DashboardSnapshotRepository extends JpaRepository<DashboardSnapshot, Long> {
    Optional<DashboardSnapshot> findByMetricName(String metricName);
}
