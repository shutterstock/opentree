package com.shutterstock.oss.opentree.repository;

import java.util.List;

import com.shutterstock.oss.opentree.model.entity.EmployeeUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeUpdateRepository extends JpaRepository<EmployeeUpdate, Long> {
    List<EmployeeUpdate> findAllByOrderByEmployeeId();
}
