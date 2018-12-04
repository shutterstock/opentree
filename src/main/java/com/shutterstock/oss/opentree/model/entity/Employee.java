package com.shutterstock.oss.opentree.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;

@Entity
@Table(name = "employees")
public class Employee {
    public Employee() {}

    public Employee(String employeeId, String name, String managerId, String managerName,
                    String location, String title, String email, String type,
                    String costCenter, String costCenterHierarchy) {
        this.employeeId = employeeId;
        this.name = name;
        this.managerId = managerId;
        this.managerName = managerName;
        this.location = location;
        this.title = title;
        this.email = email;
        this.type = type;
        this.costCenter = costCenter;
        this.costCenterHierarchy = costCenterHierarchy;
    }

    public Employee(String employeeId, String name, String managerId, String managerName,
                    String location, String title, String email, String type,
                    String costCenter, String costCenterHierarchy, boolean active, Date createdAt, Date lastUpdated) {
        this.employeeId = employeeId;
        this.name = name;
        this.managerId = managerId;
        this.managerName = managerName;
        this.location = location;
        this.title = title;
        this.email = email;
        this.type = type;
        this.costCenter = costCenter;
        this.costCenterHierarchy = costCenterHierarchy;
        this.active = active;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    /*
     * This enumerates the only fields that are subject to change. This is used to track updates to any of these
     * fields for a given employee over time
     */
    enum UpdateableFields {
        NAME("name"),
        MANAGER_ID("manager id"),
        MANAGER_NAME("manager name"),
        LOCATION("location"),
        TITLE("title"),
        EMAIL("email"),
        TYPE("type"),
        COST_CENTER("cost center"),
        COST_CENTER_HIERARCHY("cost center hierarchy");

        final String fieldName;

        UpdateableFields(String fieldName) {
            this.fieldName = fieldName;
        }

        String value() {
            return fieldName;
        }
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @CsvBindByName(column = "EmployeeID", required = true)
    private String employeeId;

    @NotNull
    @Column(name = "name")
    @CsvBindByName(column = "Name", required = true)
    private String name;

    @JsonIgnore
    @Column(name = "manager_id")
    @CsvBindByName(column = "ManagerID", required = true)
    private String managerId;

    @JsonIgnore
    @Column(name = "manager_name")
    @CsvBindByName(column = "ManagerName", required = true)
    private String managerName;

    @NotNull
    @CsvBindByName(column = "Location", required = true)
    private String location;

    @NotNull
    @CsvBindByName(column = "Title", required = true)
    private String title;

    @Column(unique = true)
    @CsvBindByName(column = "WorkEmail")
    private String email;

    @NotNull
    @CsvBindByName(column = "Type", required = true)
    private String type;

    @NotNull
    @CsvBindByName(column = "CostCenter", required = true)
    private String costCenter;

    @NotNull
    @CsvBindByName(column = "CostCenterHierarchy", required = true)
    private String costCenterHierarchy;

    @NotNull
    private boolean active;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    private Date lastUpdated;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = cleanseName(name);
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = cleanseName(managerName);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter.replaceAll("\\d", "").trim();
    }

    public String getCostCenterHierarchy() {
        return costCenterHierarchy;
    }

    public void setCostCenterHierarchy(String costCenterHierarchy) {
        this.costCenterHierarchy = costCenterHierarchy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    private String cleanseName(String name) {
        return name.replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * In conjunction with the UpdateableFields enum, this method compares previous records (from the database, if
     * extant), with data pulled from Workday and populates a List of EmployeeUpdates
     * @param newEmployeeData Employee's new data from Workday
     * @return collection of employee field updates
     */
    public List<EmployeeUpdate> merge(Employee newEmployeeData) {
        List<EmployeeUpdate> updates = new ArrayList<>();
        if (!name.equals(newEmployeeData.name)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.NAME.value(), name, newEmployeeData.name));
            name = newEmployeeData.name;
        }
        if (!managerId.equals(newEmployeeData.managerId)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.MANAGER_ID.value(), managerId, newEmployeeData.managerId));
            managerId = newEmployeeData.managerId;
        }
        if (!managerName.equals(newEmployeeData.managerName)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.MANAGER_NAME.value(), managerName, newEmployeeData.managerName));
            managerName = newEmployeeData.managerName;
        }
        if (!title.equals(newEmployeeData.title)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.TITLE.value(), title, newEmployeeData.title));
            title = newEmployeeData.title;
        }
        if (!location.equals(newEmployeeData.location)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.LOCATION.value(), location, newEmployeeData.location));
            location = newEmployeeData.location;
        }
        if (!email.equals(newEmployeeData.email)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.EMAIL.value(), email, newEmployeeData.email));
            email = newEmployeeData.email;
        }
        if (!type.equals(newEmployeeData.type)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.TYPE.value(), type, newEmployeeData.type));
            type = newEmployeeData.type;
        }
        if (!costCenter.equals(newEmployeeData.costCenter)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.COST_CENTER.value(), costCenter, newEmployeeData.costCenter));
            costCenter = newEmployeeData.costCenter;
        }
        if (!costCenterHierarchy.equals(newEmployeeData.costCenterHierarchy)) {
            updates.add(new EmployeeUpdate(employeeId, UpdateableFields.COST_CENTER_HIERARCHY.value(), costCenterHierarchy, newEmployeeData.costCenterHierarchy));
            costCenterHierarchy = newEmployeeData.costCenterHierarchy;
        }
        lastUpdated = new Date();
        return updates;
    }

    public String toString() {
        return String.format(
                "Employee[employeeId='%s', name='%s', managerId='%s', managerName='%s', " +
                        "title='%s', location='%s', email='%s', type='%s', " +
                        "costCenter='%s', costCenterHierarchy='%s', active='%s', createdAt='%tD', lastUpdated='%tD']",
                employeeId, name, managerId, managerName, title, location, email,
                type, costCenter, costCenterHierarchy, active, createdAt, lastUpdated
        );
    }

    public boolean notSame(Employee e) {
        if (e == null || getClass() != e.getClass()) return false;
        return active != e.active &&
                !name.equals(e.name) ||
                !managerId.equals(e.managerId) ||
                !managerName.equals(e.managerName) ||
                !location.equals(e.location) ||
                !title.equals(e.title) ||
                !email.equals(e.email) ||
                !type.equals(e.type) ||
                !costCenter.equals(e.costCenter) ||
                !costCenterHierarchy.equals(e.costCenterHierarchy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return active == employee.active &&
                Objects.equals(employeeId, employee.employeeId) &&
                Objects.equals(name, employee.name) &&
                Objects.equals(managerId, employee.managerId) &&
                Objects.equals(email, employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, name, managerId, email, active);
    }
}
