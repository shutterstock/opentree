package com.shutterstock.oss.opentree.web.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import com.shutterstock.oss.opentree.model.entity.Employee;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.shutterstock.oss.opentree.web.FormState;
import com.shutterstock.oss.opentree.service.EmployeeService;

@Controller
public class OpenTreeController {
    private static final Logger logger = LogManager.getLogger(OpenTreeController.class);

    private EmployeeService employeeService;

    @Autowired
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Value("${opentree.report-issue-url}")
    private String reportIssueUrl;

    @Value("${opentree.hangout.prefix}")
    private String hangoutPrefix;

    @Value("${opentree.company.name}")
    private String companyName;

    @Value("${opentree.web.fonts}")
    private String fonts;

    @ModelAttribute("hangoutPrefix")
    public String getHangoutPrefix() {
        return hangoutPrefix;
    }

    @ModelAttribute("reportIssueUrl")
    public String getReportIssueUrl() {
        return reportIssueUrl;
    }

    @ModelAttribute("companyName")
    public String getCompanyName() {
        return companyName;
    }

    @ModelAttribute("fonts")
    public String getFonts() {
        return fonts;
    }

    @ModelAttribute("today")
    public Date getToday() {
        return new Date();
    }

    @ModelAttribute("locations")
    public List<String> getLocations() {
        return employeeService.getAllEmployeeLocations();
    }

    @ModelAttribute("departments")
    public List<String> getDepartments() {
        return employeeService.getAllDepartments();
    }

    @ModelAttribute("titles")
    public List<String> getTitles() {
        return employeeService.getAllEmployeeTitles();
    }

    private static final Escaper escaper = UrlEscapers.urlFormParameterEscaper();

    @GetMapping(value = "/")
    @ResponseBody
    public ModelAndView index(HttpServletRequest request) {
        logger.debug("getting opentree index");
        ModelAndView modelAndView = new ModelAndView("index");
        String employees = employeeService.getEmployees();
        long numEmployees = employeeService.getNumberOfEmployees();
        modelAndView.addObject("state", FormState.build(FormState.FilterOption.NONE));
        modelAndView.addObject("employees", employees);
        modelAndView.addObject("numEmployees", numEmployees);
        return modelAndView;
    }

    @GetMapping(value = "/filter", params="subtree")
    @ResponseBody
    public ModelAndView getEmployeeTreeByEmail(@RequestParam("subtree") String email, HttpServletRequest request) {
        logger.debug("employee subtree for " + email);
        ModelAndView modelAndView = new ModelAndView("index");
        if (email == null || email.trim().equals("")) {
            logger.warn("empty employee username submitted: " + email);
            return index(request);
        }
        String employees = employeeService.getEmployees(email);
        if (employees == null) {
            logger.warn("unable to retrieve data for specified email: " + email);
            return index(request);
        }
        Employee e = employeeService.getEmployeeByLdap(email);
        modelAndView.addObject("state", FormState.build(FormState.FilterOption.SUBTREE));
        modelAndView.addObject("employeeName", e.getName());
        modelAndView.addObject("employees", employees);
        return modelAndView;
    }

    @GetMapping(value = "/filter", params = "location")
    @ResponseBody
    public ModelAndView getEmployeesByLocation(@RequestParam("location") String location, HttpServletRequest request) {
        logger.debug("filtering employees by location: " + location);
        if (location.equals("All"))
            return new ModelAndView("redirect:/");
        ModelAndView modelAndView = new ModelAndView("index");
        String employees = employeeService.getEmployeesByLocation(location);
        long numEmployees = employeeService.getNumberOfEmployeesAtLocation(location);
        modelAndView.addObject("state", FormState.build(FormState.FilterOption.LOCATION));
        modelAndView.addObject("selectedLocation", location);
        modelAndView.addObject("employees", employees);
        modelAndView.addObject("numEmployees", numEmployees);
        return modelAndView;
    }

    @GetMapping(value = "/filter", params = "type")
    @ResponseBody
    public ModelAndView getOnlyEmployees(String type, HttpServletRequest request) {
        logger.debug("filtering employees by type: " + type);
        if (!type.equals("Employee"))
            return new ModelAndView("redirect:/");
        ModelAndView modelAndView = new ModelAndView("index");
        String employees = employeeService.getEmployeesByType(type);
        long numEmployees = employeeService.getNumberOfFulltimeEmployees();
        modelAndView.addObject("state", FormState.build(FormState.FilterOption.TYPE));
        modelAndView.addObject("employees", employees);
        modelAndView.addObject("numEmployees", numEmployees);
        return modelAndView;
    }

    @GetMapping(value = "/filter", params = "department")
    @ResponseBody
    public ModelAndView getEmployeesByDepartment(@RequestParam("department") String department, HttpServletRequest request) {
        logger.debug("filtering employees by department: " + department);
        if (department.equals("All"))
            return new ModelAndView("redirect:/");
        ModelAndView modelAndView = new ModelAndView("index");
        String employees = employeeService.getEmployeesByDepartment(department);
        long numEmployees = employeeService.getNumberOfDepartmentEmployees(department);
        modelAndView.addObject("state", FormState.build(FormState.FilterOption.DEPARTMENT));
        modelAndView.addObject("selectedDepartment", department);
        modelAndView.addObject("employees", employees);
        modelAndView.addObject("numEmployees", numEmployees);
        return modelAndView;
    }

    @GetMapping(value = "/filter", params = "title")
    @ResponseBody
    public ModelAndView getEmployeesByTitle(@RequestParam("title") String title, HttpServletRequest request) {
        logger.debug("filter employees by title: " + title);
        if (title.equals("All"))
            return new ModelAndView("redirect:/");
        ModelAndView modelAndView = new ModelAndView("index");
        String employees = employeeService.getEmployeesByTitle(title);
        long numEmployees = employeeService.getNumberOfEmployeesForTitle(title);
        modelAndView.addObject("state", FormState.build(FormState.FilterOption.TITLE));
        modelAndView.addObject("selectedTitle", title);
        modelAndView.addObject("employees", employees);
        modelAndView.addObject("numEmployees", numEmployees);
        return modelAndView;
    }

    @GetMapping(value = "/filter", params = {"location", "department", "title"})
    @ResponseBody
    public ModelAndView getEmployeesByAllFilters(@RequestParam String location,
                                                 @RequestParam String department,
                                                 @RequestParam String title,
                                                 HttpServletRequest request) {
        logger.debug("filtering employees by location/department/title: " + location + " / " + department + " / " + title);
        if (location.equals("All") && department.equals("All"))
            return new ModelAndView("redirect:/filter?title=" + escaper.escape(title));
        if (department.equals("All") && title.equals("All"))
            return new ModelAndView("redirect:/filter?location=" + escaper.escape(location));
        if (location.equals("All") && title.equals("All"))
            return new ModelAndView("redirect:/filter?department=" + escaper.escape(department));

        ModelAndView modelAndView = new ModelAndView("index");
        String employees = employeeService.getEmployees(location, department, title);
        long numEmployees = employeeService.getNumberOfEmployees(location, department, title);
        modelAndView.addObject("state", FormState.build(FormState.FilterOption.ALL));
        modelAndView.addObject("selectedDepartment", department);
        modelAndView.addObject("selectedLocation", location);
        modelAndView.addObject("selectedTitle", title);
        modelAndView.addObject("employees", employees);
        modelAndView.addObject("numEmployees", numEmployees);
        return modelAndView;
    }

    @GetMapping(value = "/reset")
    @ResponseBody
    public ModelAndView reset(HttpServletRequest request) {
        logger.debug("resetting filters");
        return new ModelAndView("redirect:/");
    }
}
