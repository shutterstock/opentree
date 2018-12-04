package com.shutterstock.oss.opentree.model.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "updates")
public class EmployeeUpdate  {
    public EmployeeUpdate() {}

    public EmployeeUpdate(String employeeId, String valueName, String oldValue, String newValue) {
        this.employeeId = employeeId;
        this.valueName = valueName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        createdAt = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "update_id")
    private long updateId;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "value_name")
    private String valueName;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "created_at")
    private Date createdAt;

    public long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(long updateId) {
        this.updateId = updateId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format(
                "EmployeeUpdate[updateId='%s', employeeId='%s', valueName='%s', oldValue='%s', " +
                        "newValue='%s', createdAt='%tD']",
                updateId, employeeId, valueName, oldValue, newValue, createdAt
        );
    }
}
