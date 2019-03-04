package com.shutterstock.oss.opentree.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.shutterstock.oss.opentree.model.entity.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class WorkdayDataService {
    private static final Logger logger = LogManager.getLogger(WorkdayDataService.class);

    @Value("${opentree.production}")
    private boolean production;
    @Value("${wd.endpoint}")
    private String empDataEndpoint;
    @Value("${wd.username}")
    private String empDataEndpointUsername;
    @Value("${wd.password}")
    private String empDataEndpointPassword;
    @Value("${opentree.wd.retries}")
    private static int maxRetries;

    public List<Employee> getEmployees() {
        return processCsvRecords(getEmployeeData());
    }

    private String getEmployeeData() {
        if (production) {
            logger.debug("hitting workday endpoint for employee data");
            HttpResponse<String> response;
            int retries = 0;
            while (true) {
                try {
                    response = Unirest.get(empDataEndpoint)
                            .basicAuth(
                                    empDataEndpointUsername,
                                    empDataEndpointPassword
                            ).asString();
                    return response.getBody();
                } catch (UnirestException e) {
                    logger.warn("error hitting workday endpoint (attempt #" + retries + 1 + "):", e.getMessage());
                    if (++retries == maxRetries) {
                        logger.fatal("failed getting data from workday endpoint for maxRetries: ", e.getMessage());
                        break;
                    }
                }
            }
        } else {
            logger.debug("not production: returning local, testing employee data");
            ClassPathResource cpr = new ClassPathResource("employees.csv");
            StringBuilder sb = new StringBuilder();
            try {
                InputStream is = cpr.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException ioe) {
                logger.fatal("failed to load test employee data from classpath");
            }
            return sb.toString();
        }
        return null;
    }

    private List<Employee> processCsvRecords(String csvRecords) {
        logger.debug("processing csv records");
        CsvToBean<Employee> csvToEmployee = new CsvToBeanBuilder<Employee>(new StringReader(csvRecords))
                .withType(Employee.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        Iterator<Employee> employeeIterator = csvToEmployee.iterator();
        List<Employee> results = new ArrayList<>();
        try {
            while (employeeIterator.hasNext())
                results.add(employeeIterator.next());
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (results.isEmpty()) {
            logger.warn("no employee beans converted");
            return null;
        } else {
            logger.debug("returning " + results.size() + " employees from csv");
            return results;
        }
    }
}
