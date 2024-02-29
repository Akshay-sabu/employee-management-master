package com.employee.management.repository;

import com.employee.management.models.Employee;
import com.employee.management.models.HikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface HikeRepository extends JpaRepository<HikeEntity, Long> {
    @Query("SELECT h FROM HikeEntity h WHERE h.isApproved = :status AND h.employee = :employee")
    Optional<HikeEntity> findByStatusAndEmployee(boolean status, Employee employee);
    @Query("SELECT h FROM HikeEntity h WHERE h.isApproved = false")
    List<HikeEntity> findAllByStatusFalse();
    List<HikeEntity>findByEffectiveDate(Date date);
    Optional<HikeEntity>findByEmployee(Employee employee);
}

