package com.shutterstock.oss.opentree.model.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Component;

@Component
public class TreeBuilder {
    private static final Logger logger = LogManager.getLogger(TreeBuilder.class);

    private final static ObjectMapper mapper = new ObjectMapper();
    private static List<Employee> employees;
    private static List<String> managerIds;
    private static JsonNode jsonRoot;
    private static final String fakeId = "Placeholder";
    private static final Employee fakeRoot = new Employee(
            fakeId, fakeId, fakeId, fakeId, fakeId, fakeId, fakeId, fakeId, fakeId, fakeId);

    private TreeBuilder() {}

    public TreeBuilder build(List<Employee> employees, Employee specifiedRoot) throws IllegalStateException {
        logger.debug("building tree with employee set size: " + employees.size());

        Employee root;
        // root is the employee who is their own manager
        if (specifiedRoot == null) {
            root = employees.stream()
                    .filter(employee -> employee.getEmployeeId().equals(employee.getManagerId()))
                    .findAny()
                    .orElse(null);
        } else {
            root = specifiedRoot;
        }

        if (root == null) {
            // all emp ids in this collection
            List<String> allIds = employees.stream()
                    .map(Employee::getEmployeeId)
                    .collect(Collectors.toList());

            // all employees whose managers don't exist in this subset -- they will be roots
            List<Employee> roots = employees.stream()
                    .filter(employee -> !allIds.contains(employee.getManagerId()))
                    .collect(Collectors.toList());

            managerIds = employees.stream()
                    .filter(employee -> allIds.contains(employee.getEmployeeId()))
                    .map(Employee::getManagerId)
                    .collect(Collectors.toList());

            // for each of these roots, attach them to the fake root
            roots.forEach(employee -> employee.setManagerId(fakeId));

            jsonRoot = getNodeFromEmployee(fakeRoot);
        } else {
            managerIds = employees.stream()
                    .map(Employee::getManagerId)
                    .collect(Collectors.toList());
            jsonRoot = getNodeFromEmployee(root);
            employees.remove(root);
        }

        TreeBuilder.employees = employees;
        processNode(jsonRoot);
        return this;
    }

    public TreeBuilder build(List<Employee> employees) throws IllegalStateException {
        return build(employees, null);
    }

    public String getTree() {
        if (jsonRoot != null && jsonRoot.size() > 0) {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonRoot);
            } catch (JsonProcessingException ex) {
                logger.error("failed to write json as string", ex);
                return null;
            }
        }
        else
            return null;
    }

    private static JsonNode processNode(JsonNode node) {
        List<JsonNode> children = getChildren(node);
        if (children != null) {
            for (final JsonNode n : children) {
                ArrayNode childrenNode = ((ObjectNode) node).withArray("children");
                ((ObjectNode) node).put("numChildren", children.size());
                if (childrenNode != null && childrenNode.isArray()) {
                    childrenNode.add(processNode(n));
                } else {
                    ((ObjectNode) node).putArray("children");
                }
            }
        }
        return node;
    }

    private static List<JsonNode> getChildren(JsonNode node) {
        List<Employee> children = employees.stream()
                .filter(employee -> employee.getManagerId().equals(node.path("id").asText()))
                .collect(Collectors.toList());

        return children.stream()
                .map(TreeBuilder::getNodeFromEmployee)
                .collect(Collectors.toList());
    }

    private static JsonNode getNodeFromEmployee(Employee e) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", e.getEmployeeId())
                .put("name", e.getName())
                .put("managerId", e.getManagerId())
                .put("managerName", e.getManagerName())
                .put("location", e.getLocation())
                .put("title", e.getTitle())
                .put("email", e.getEmail())
                .put("type", e.getType())
                .put("costCenter", e.getCostCenter())
                .put("costCenterHierarchy", e.getCostCenterHierarchy());

        if (managerIds.contains(e.getEmployeeId())) {
            ArrayNode children = mapper.createArrayNode();
            node.set("children", children);
        }
        return node;
    }
}
