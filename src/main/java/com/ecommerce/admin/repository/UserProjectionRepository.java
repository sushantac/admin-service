package com.ecommerce.admin.repository;

import com.ecommerce.admin.entity.UserProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProjectionRepository extends JpaRepository<UserProjection, Long> {

    @Query("SELECT u FROM UserProjection u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<UserProjection> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(u) FROM UserProjection u")
    long countAll();
}
