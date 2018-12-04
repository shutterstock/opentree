package com.shutterstock.oss.opentree.web;

public class FormState {
    private boolean filterBySubtree = false;
    private boolean filterByType = false;
    private boolean filterByLocation = false;
    private boolean filterByDepartment = false;
    private boolean filterByTitle = false;

    public enum FilterOption {
        NONE,
        SUBTREE,
        TYPE,
        LOCATION,
        DEPARTMENT,
        TITLE,
        LOCATION_AND_TYPE,
        DEPARTMENT_AND_TYPE,
        LOCATION_AND_DEPARTMENT,
        ALL
    }

    private FormState() {}

    private FormState(boolean filterBySubtree, boolean filterByType,
                      boolean filterByLocation, boolean filterByDepartment,
                      boolean filterByTitle) {
        this.filterBySubtree = filterBySubtree;
        this.filterByType = filterByType;
        this.filterByLocation = filterByLocation;
        this.filterByDepartment = filterByDepartment;
        this.filterByTitle = filterByTitle;
    }

    public static FormState build(FilterOption option) {
        switch (option) {
            case SUBTREE:
                return new FormState(
                        true,
                        false,
                        false,
                        false,
                        false);
            case TYPE:
                return new FormState(
                        false,
                        true,
                        false,
                        false,
                        false);
            case LOCATION:
                return new FormState(
                        false,
                        false,
                        true,
                        false,
                        false);
            case TITLE:
                return new FormState(
                        false,
                        false,
                        false,
                        false,
                        true);
            case LOCATION_AND_TYPE:
                return new FormState(
                        false,
                        true,
                        true,
                        false,
                        false);
            case DEPARTMENT:
                return new FormState(
                        false,
                        false,
                        false,
                        true,
                        false);
            case DEPARTMENT_AND_TYPE:
                return new FormState(
                        false,
                        true,
                        false,
                        true,
                        false);
            case LOCATION_AND_DEPARTMENT:
                return new FormState(
                        false,
                        false,
                        true,
                        true,
                        false);
            case ALL:
                return new FormState(
                        false,
                        false,
                        true,
                        true,
                        true);
            case NONE:
            default:
                return new FormState();
        }
    }

    public boolean forNone() {
        return !filterByType && !filterBySubtree && !filterByLocation && !filterByDepartment;
    }

    public boolean forSubtree() {
        return filterBySubtree;
    }

    public boolean forType() {
        return filterByType;
    }

    public boolean forLocation() {
        return filterByLocation;
    }

    public boolean forDepartment() {
        return filterByDepartment;
    }

    public boolean forTitle() {
        return filterByTitle;
    }

    public boolean forLocationAndType() {
        return filterByLocation && filterByType;
    }

    public boolean forDepartmentAndType() {
        return filterByDepartment && filterByType;
    }

    public boolean forLocationAndDepartment() {
        return filterByLocation && filterByDepartment;
    }
}
