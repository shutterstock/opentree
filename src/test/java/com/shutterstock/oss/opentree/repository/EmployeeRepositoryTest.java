package com.shutterstock.oss.opentree.repository;

import java.util.Date;
import java.util.List;

import com.shutterstock.oss.opentree.model.entity.Employee;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    private EmployeeRepository employeeRepository;
    @Autowired
    public void setEmployeeRepository(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    private Employee e1, e2;

    @Before
    public void init() {
        // given
        e1 = new Employee("000001", "John Doe", "000001", "John Doe",
                "New York City", "CEO", "jdoe@superbigmegacorp.com", "Employee",
                "Office of the CEO", "Administration", true, new Date(), new Date());
        entityManager.persist(e1);

        e2 = new Employee("000002", "Zack Jones", "000001", "John Doe",
                "New York City", "Chief Technology Officer", "zjones@superbigmegacorp.com", "Employee",
                "Office of the CEO", "Engineering", true, new Date(), new Date());
        entityManager.persist(e2);
    }

    @After
    public void destroyEmployeeRecords() {
        entityManager.remove(e1);
        entityManager.remove(e2);
    }

    @Test
    public void whenCountActiveEmployees_thenReturnCount() {
        long count = employeeRepository.countActiveEmployees();
        assertThat(count).isEqualTo(2);
    }

    @Test
    public void whenFindByEmail_thenReturnEmployee() {
        Employee e = employeeRepository.findByEmail("jdoe@superbigmegacorp.com");
        assertThat(e.getEmployeeId()).isEqualTo("000001");
    }

    @Test
    public void whenFindDistinctLocations_thenReturnList() {
        List<String> locations = employeeRepository.findDistinctLocations();
        assertThat(locations.size()).isEqualTo(1);
        assertThat(locations).contains("New York City");
    }

    @Test
    public void whenFindDistinctTitles_thenReturnList() {
        List<String> titles = employeeRepository.findDistinctTitles();
        assertThat(titles.size()).isEqualTo(2);
        assertThat(titles).contains("CEO", "Chief Technology Officer");
    }

    @Test
    public void whenFindByLocation_thenReturnList() {
        List<Employee> employees = employeeRepository.findByLocation("New York City");
        assertThat(employees.size()).isEqualTo(2);
    }

    @Test
    public void whenFindByLocationAndCostCenterAndTitle_returnList() {
        List<Employee> employees = employeeRepository.findByLocationAndCostCenterAndTitle("New York City",
                "Office of the CEO", "CEO");
        assertThat(employees).contains(e1);
    }

    @Test
    public void whenFindDistinctCostCenters_returnList() {
        List<String> costCenters = employeeRepository.findDistinctCostCenters();
        assertThat(costCenters).contains("Office of the CEO");
    }

    @Test
    public void whenFindByTitle_returnList() {
        List<Employee> employees = employeeRepository.findByTitle("CEO");
        assertThat(employees).contains(e1);
    }
}
