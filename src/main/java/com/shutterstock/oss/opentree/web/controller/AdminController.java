package com.shutterstock.oss.opentree.web.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.shutterstock.oss.opentree.model.entity.Employee;
import com.shutterstock.oss.opentree.model.entity.EmployeeUpdate;
import com.shutterstock.oss.opentree.service.EmployeeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminController {
    private static final Logger logger = LogManager.getLogger(AdminController.class);

    private EmployeeService employeeService;

    @Autowired
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Value("${opentree.report-issue-url}")
    private String reportIssueUrl;

    @ModelAttribute("reportIssueUrl")
    public String getReportIssueUrl() {
        return reportIssueUrl;
    }

    @Value("${opentree.company.name}")
    private String companyName;

    @ModelAttribute("companyName")
    public String getCompanyName() {
        return companyName;
    }

    @Value("${opentree.web.fonts}")
    private String fonts;

    @ModelAttribute("fonts")
    public String getFonts() {
        return fonts;
    }

    @GetMapping(value = "/admin/updates")
    @ResponseBody
    public ModelAndView getUpdates(HttpServletRequest request) {
        logger.debug("getting updates");
        ModelAndView modelAndView = new ModelAndView("updates");
        Map<Employee, List<EmployeeUpdate>> updates = employeeService.getEmployeeUpdates();
        modelAndView.addObject("updateMap", updates);
        return modelAndView;
    }

    @GetMapping(value = "/admin/refresh")
    @ResponseBody
    public ModelAndView refresh(HttpServletRequest request) {
        logger.debug("forcing refresh of employees");
        employeeService.refreshEmployeeData();
        return new ModelAndView("redirect:/");
    }

    @GetMapping(value = "/admin/dumpdb")
    @ResponseBody
    public ModelAndView dumpDb(HttpServletRequest request) {
        logger.debug("getting database dump of employees");
        ModelAndView modelAndView = new ModelAndView("dump");
        modelAndView.addObject("employees", employeeService.getEmployeeObjects());
        return modelAndView;
    }

    @GetMapping(value = "/admin/dumpcache")
    @ResponseBody
    public String dumpCache(HttpServletRequest request) {
        logger.debug("getting cache dump of employees");
        return "<pre>" + employeeService.getEmployees() + "</pre>";
    }

    @RequestMapping(value = "/admin/morgue", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView employeeDiff(HttpServletRequest request) {
        logger.debug("getting employee list for morgue");
        ModelAndView modelAndView = new ModelAndView("morgue");
        modelAndView.addObject("employees", employeeService.getInactiveEmployees());
        return modelAndView;
    }

    @GetMapping(value = "/admin/roots")
    @ResponseBody
    public ModelAndView getRoots(HttpServletRequest request) {
        logger.debug("getting all roots");
        ModelAndView modelAndView = new ModelAndView("roots");
        modelAndView.addObject("employees", employeeService.getRoots());
        return modelAndView;
    }
}
