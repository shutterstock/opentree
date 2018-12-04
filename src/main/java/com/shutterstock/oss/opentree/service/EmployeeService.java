package com.shutterstock.oss.opentree.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.shutterstock.oss.opentree.model.entity.Employee;
import com.shutterstock.oss.opentree.model.entity.EmployeeUpdate;
import com.shutterstock.oss.opentree.model.entity.TreeBuilder;
import com.shutterstock.oss.opentree.repository.EmployeeRepository;
import com.shutterstock.oss.opentree.repository.EmployeeUpdateRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Service
public class EmployeeService {
    private static final Logger logger = LogManager.getLogger(EmployeeService.class);

    private EmployeeRepository employeeRepository;
    @Autowired
    public void setEmployeeRepository(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    private EmployeeUpdateRepository updateRepository;
    @Autowired
    public void setUpdateRepository(EmployeeUpdateRepository updateRepository) {
        this.updateRepository = updateRepository;
    }

    private WorkdayDataService workdayDataService;
    @Autowired
    public void setWorkdayDataService(WorkdayDataService workdayDataService) {
        this.workdayDataService = workdayDataService;
    }

    private TreeBuilder treeBuilder;
    @Autowired
    public void setTreeBuilder(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
    }

    @Value("${opentree.cache.ttl-hours}")
    private long cacheTtl;

    @Value("${opentree.email.domain}")
    private String emailDomain;

    private LoadingCache<Class,String> cache;

    @EventListener(ApplicationReadyEvent.class)
    public void warmCache() {
        logger.debug("application ready--warming cache");
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTtl, TimeUnit.HOURS)
                .build(
                        new CacheLoader<>() {
                            @Override
                            public String load(Class type) {
                                return loadCache();
                            }
                        }
                );
        try {
            cache.get(Employee.class);
        } catch (Exception ex) {
            logger.error("error while loading employees into cache", ex);
        }
        logger.debug("cache warming complete");
    }

    private String loadCache() {
        logger.debug("loading employees into cache");
        List<Employee> priorEmployeeList = employeeRepository.findAllByOrderByEmployeeIdAsc();
        List<Employee> latestEmployeeList = workdayDataService.getEmployees();
        Set<Employee> latestEmployeeSet = new HashSet<>(latestEmployeeList);
        logger.debug("priorEmployeeList.size: " + priorEmployeeList.size()
                + "; latestEmployeeList.size: " + latestEmployeeList.size()
                + "; latestEmployeeSet.size: " + latestEmployeeSet.size());
        if (priorEmployeeList.size() > 0) {
            logger.debug("incremental update of employees");
            List<Employee> removedEmployees = getListDifference(priorEmployeeList, latestEmployeeList);
            logger.debug("# of employees no longer with company: " + removedEmployees.size());
            if (removedEmployees.size() > 0) {
                removedEmployees.forEach(e -> {
                    if (e.isActive()) {
                        e.setActive(false);
                        e.setLastUpdated(new Date());
                        employeeRepository.saveAndFlush(e);
                    }
                });
            }

            List<Employee> updatedEmployees = getUpdatedEmployees(priorEmployeeList, latestEmployeeList);
            logger.debug("# of updated employees: " + updatedEmployees.size());
            if (updatedEmployees.size() > 0) {
                updatedEmployees.forEach(e -> {
                    e.setActive(true);
                    e.setLastUpdated(new Date());
                });
                employeeRepository.saveAll(updatedEmployees);
                employeeRepository.flush();
            }

            List<Employee> addedEmployees = getListDifference(latestEmployeeList, priorEmployeeList);
            logger.debug("# of new employees: " + addedEmployees.size());
            if (addedEmployees.size() > 0) {
                addedEmployees.forEach(e -> {
                    e.setActive(true);
                    e.setCreatedAt(new Date());
                    e.setLastUpdated(new Date());
                });
                employeeRepository.saveAll(addedEmployees);
                employeeRepository.flush();
            }
        } else {
            logger.debug("initial insertion of employees");
            latestEmployeeSet.forEach(e -> {
                e.setActive(true);
                e.setCreatedAt(new Date());
                e.setLastUpdated(new Date());
            });
            employeeRepository.saveAll(latestEmployeeSet);
            employeeRepository.flush();
        }
        return treeBuilder.build(employeeRepository.findByActiveOrderByLastUpdatedDesc(true)).getTree();
    }

    /**
     * This method determines the differences between the two lists by subtracting the listSubtrahend from the
     * listMinuend. By using this method, inactive and new Employees can be calculated.
     * @param listMinuend the list from which the listSubtrahend will be subtracted
     * @param listSubtrahend the list that will be subtracted from the listMinuend
     * @return a list containing the difference
     */
    private List<Employee> getListDifference(List<Employee> listMinuend, List<Employee> listSubtrahend) {
        logger.debug("getting list difference");
        return listMinuend.stream()
                .filter(oe -> listSubtrahend.stream()
                        .noneMatch(fe -> fe.getEmployeeId().equals(oe.getEmployeeId())))
                .collect(Collectors.toList());
    }

    private List<Employee> getUpdatedEmployees(List<Employee> oldList, List<Employee> newList) {
        logger.debug("getting updated employees");

        List<Employee> existingEmployeeData = oldList.stream()
                .filter(oe -> newList.stream()
                        .anyMatch(ne -> oe.getEmployeeId().equals(ne.getEmployeeId()) && oe.notSame(ne))
                )
                .collect(Collectors.toList());

        List<Employee> updatedEmployeeData = newList.stream()
                .filter(ne -> oldList.stream()
                        .anyMatch(oe -> ne.getEmployeeId().equals(oe.getEmployeeId()) && ne.notSame(oe))
                )
                .collect(Collectors.toList());

        for (Employee od : existingEmployeeData) {
            for (Employee ud : updatedEmployeeData) {
                if (od.getEmployeeId().equals(ud.getEmployeeId())) {
                    List<EmployeeUpdate> updates = od.merge(ud);
                    updateRepository.saveAll(updates);
                    break;
                }
            }
            updateRepository.flush();
        }
        return existingEmployeeData;
    }

    private void reloadData() {
        logger.debug("forced reloading of cache");
        cache.invalidateAll();
        cache.refresh(Employee.class);
    }

    private String getEmployees(boolean force) {
        logger.debug("getting employees from cache");
        String json = null;
        try {
            json = cache.get(Employee.class);
        } catch (Exception ex) {
            logger.error("failed to fetch employees from cache", ex);
        }

        if (force || json == null) {
            reloadData();
            try {
                json = cache.get(Employee.class);
            } catch (Exception ex) {
                logger.error("failed to fetch employees from force-refreshed cache!", ex);
            }
        }
        return json;
    }

    public String getEmployeesByLocation(String location) {
        List<Employee> employees = employeeRepository.findByLocation(location);
        return treeBuilder.build(employees).getTree();
    }

    public String getEmployees(String location, String department, String title) {
        logger.debug("getting employees for\n\t:location: " + location + "\n\tdepartment: " + department + "\n\ttitle: " + title);
        List<Employee> employees;
        if (location.equals("All") && department.equals("All"))
            employees = employeeRepository.findByTitle(title);
        else if (location.equals("All") && title.equals("All"))
            employees = employeeRepository.findByCostCenter(department);
        else if (department.equals("All") && title.equals("All"))
            employees = employeeRepository.findByLocation(location);
        else if (!department.equals("All") && !title.equals("All"))
            employees = employeeRepository.findByCostCenterAndTitle(department, title);
        else if (!location.equals("All") && !title.equals("All"))
            employees = employeeRepository.findByLocationAndTitle(location, title);
        else if (!department.equals("All") && !location.equals("All"))
            employees = employeeRepository.findByCostCenterAndLocation(department, location);
        else
            employees = employeeRepository.findByLocationAndCostCenterAndTitle(location, department, title);
        return treeBuilder.build(employees).getTree();
    }

    public List<Employee> getInactiveEmployees() {
        return employeeRepository.findByActiveOrderByLastUpdatedDesc(false);
    }

    public String getEmployeesByType(String type) {
        List<Employee> employees = employeeRepository.findByType(type);
        return treeBuilder.build(employees).getTree();
    }

    public String getEmployeesByDepartment(String department) {
        List<Employee> employees = employeeRepository.findByCostCenter(department);
        return treeBuilder.build(employees).getTree();
    }

    public String getEmployeesByTitle(String title) {
        List<Employee> employees = employeeRepository.findByTitle(title);
        return treeBuilder.build(employees).getTree();
    }

    public String getEmployees() {
        return getEmployees(false);
    }

    public void refreshEmployeeData() {
        getEmployees(true);
    }

    public String getEmployees(String ldap) {
        String email;
        if (ldap.contains("@"))
            email = ldap;
        else
            email = ldap + "@" + emailDomain;
        logger.debug("getting employees for: " + email);
        Employee e = employeeRepository.findByEmail(email);
        List<Employee> employees = new ArrayList<>(employeeRepository.findAll());
        return treeBuilder.build(employees, e).getTree();
    }

    public List<String> getAllEmployeeLocations() {
        return employeeRepository.findDistinctLocations();
    }

    public List<String> getAllEmployeeTitles() {
        return employeeRepository.findDistinctTitles();
    }

    public long getNumberOfEmployeesAtLocation(String location) {
        return employeeRepository.countByLocation(location);
    }

    public long getNumberOfEmployees(String location, String department, String title) {
        if (location.equals("All") && department.equals("All"))
            return employeeRepository.countByTitle(title);
        if (location.equals("All") && title.equals("All"))
            return employeeRepository.countByCostCenter(department);
        if (department.equals("All") && title.equals("All"))
            return employeeRepository.countByLocation(location);
        if (!department.equals("All") && !title.equals("All"))
            return employeeRepository.countByCostCenterAndTitle(department, title);
        if (!location.equals("All") && !title.equals("All"))
            return employeeRepository.countByLocationAndTitle(location, title);
        if (!department.equals("All") && !location.equals("All"))
            return employeeRepository.countByCostCenterAndLocation(department, location);

        return employeeRepository.countEmployeeByLocationAndCostCenterAndTitle(location, department, title);
    }

    public List<Employee> getEmployeeObjects() {
        return employeeRepository.findAllByOrderByEmployeeIdAsc();
    }

    public long getNumberOfEmployees() {
        return employeeRepository.countActiveEmployees();
    }

    public long getNumberOfFulltimeEmployees() {
        return employeeRepository.countEmployeeByType("Employee");
    }

    public long getNumberOfDepartmentEmployees(String department) {
        return employeeRepository.countByCostCenter(department);
    }

    public long getNumberOfEmployeesForTitle(String title) {
        return employeeRepository.countByTitle(title);
    }

    public List<String> getAllDepartments() {
        return employeeRepository.findDistinctCostCenters();
    }

    public Employee getEmployeeByLdap(String ldap) {
        String email;
        if (ldap != null && ldap.contains("@"))
            email = ldap;
        else
            email = ldap + "@" + emailDomain;
        return getEmployeeByEmail(email);
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public Map<Employee, List<EmployeeUpdate>> getEmployeeUpdates() {
        Map<Employee, List<EmployeeUpdate>> result = new LinkedHashMap<>();
        List<EmployeeUpdate> updates = updateRepository.findAllByOrderByEmployeeId();
        List<String> empIds = updates.stream()
                .map(EmployeeUpdate::getEmployeeId)
                .collect(Collectors.toList());
        List<Employee> employees = employeeRepository.findAllOrderByEmployeeIdIn(empIds);
        for (Employee e : employees) {
            List<EmployeeUpdate> eUpdates = updates.stream()
                    .filter(u -> u.getEmployeeId().equals(e.getEmployeeId()))
                    .sorted(Comparator.comparing(EmployeeUpdate::getCreatedAt))
                    .collect(Collectors.toList());
            result.put(e, eUpdates);
        }
        return result;
    }
}