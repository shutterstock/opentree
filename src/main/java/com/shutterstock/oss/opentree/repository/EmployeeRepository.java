package com.shutterstock.oss.opentree.repository;

import java.util.List;

import com.shutterstock.oss.opentree.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findAllByActiveOrderByEmployeeIdAsc(boolean active);
    List<Employee> findAllByOrderByEmployeeIdAsc();
    @Query("SELECT DISTINCT e FROM Employee e WHERE e.email=?1 AND e.active = TRUE")
    Employee findByEmail(String email);
    void deleteByEmployeeId(String id);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.active = TRUE")
    long countActiveEmployees();

    @Query("SELECT DISTINCT location FROM Employee WHERE location NOT LIKE '%REMOTE%' AND active = TRUE")
    List<String> findDistinctLocations();

    @Query("SELECT DISTINCT title FROM Employee WHERE active = TRUE ORDER BY title")
    List<String> findDistinctTitles();

    @Query("SELECT e FROM Employee e WHERE e.location = ?1 AND e.active = TRUE")
    List<Employee> findByLocation(@Param("location") String location);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.location = ?1 AND e.active = TRUE")
    long countByLocation(String location);

    @Query("SELECT e FROM Employee e WHERE e.type = ?1 AND e.active = TRUE")
    List<Employee> findByType(String type);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.type = ?1 AND e.active = TRUE")
    long countEmployeeByType(String type);

    @Query("SELECT e FROM Employee e WHERE e.location LIKE ?1 AND e.costCenter = ?2 AND e.title = ?3 AND e.active = TRUE")
    List<Employee> findByLocationAndCostCenterAndTitle(@Param("location") String location,
                                                       @Param("costCenter") String costCenter,
                                                       @Param("title") String title);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.location LIKE ?1 AND e.costCenter = ?2 AND e.title = ?3 AND e.active = TRUE")
    long countEmployeeByLocationAndCostCenterAndTitle(String location, String costCenter, String title);

    @Query("SELECT DISTINCT costCenter FROM Employee WHERE active = TRUE ORDER BY costCenter ASC")
    List<String> findDistinctCostCenters();

    @Query("SELECT e FROM Employee e WHERE e.costCenter = ?1 AND e.active = TRUE ORDER BY costCenter ASC")
    List<Employee> findByCostCenter(String costCenter);
    long countByCostCenter(String costCenter);

    @Query("SELECT e FROM Employee e WHERE e.title = ?1 AND e.active = TRUE ORDER BY title ASC")
    List<Employee> findByTitle(String title);
    long countByTitle(String title);

    @Query("SELECT e FROM Employee e WHERE e.costCenter = ?1 AND e.title = ?2 AND e.active = TRUE")
    List<Employee> findByCostCenterAndTitle(String costCenter, String title);
    long countByCostCenterAndTitle(String costCenter, String title);

    @Query("SELECT e FROM Employee e WHERE e.location LIKE ?1 AND e.title = ?2 AND e.active = TRUE")
    List<Employee> findByLocationAndTitle(String location, String title);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.location LIKE ?1 AND e.title = ?2 AND e.active = TRUE")
    long countByLocationAndTitle(String location, String title);

    @Query("SELECT e FROM Employee e WHERE e.costCenter = ?1 AND e.location LIKE ?2 AND e.active = TRUE")
    List<Employee> findByCostCenterAndLocation(String costCenter, String location);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.costCenter = ?1 AND e.location LIKE ?2 AND e.active = TRUE")
    long countByCostCenterAndLocation(String costCenter, String location);

    List<Employee> findByActiveOrderByLastUpdatedDesc(boolean active);

    List<Employee> findAllOrderByEmployeeIdIn(List<String> ids);
}
