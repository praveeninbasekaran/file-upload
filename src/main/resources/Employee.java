package com.dbtojson.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Employee {

    @JsonProperty("employeeId")
    private Long employeeId;
    
    @JsonProperty("employeeName")
    private String employeeName;
    
    @JsonProperty("employeeAge")
    private Integer employeeAge;
    
    @JsonProperty("employeePhone")
    private List<String> employeePhone;

    @JsonProperty("employeeAddress")
    private Address employeeAddress;
    
    // Add this no-arg constructor to allow instantiation by the GenericDataProcessor
    public Employee() {}

    /**
     * Factory method to create an Employee object from a database row map.
     * This method contains the business logic for mapping data.
     * @param row The database row as a Map.
     * @return A new Employee object.
     */
    public static Employee fromMap(Map<String, Object> row) {
        Employee employee = new Employee();

        // Check active status
        Object activeStatusObject = row.get("active_status");
        String activeStatus = activeStatusObject != null ? String.valueOf(activeStatusObject) : null;

        // Populate basic information first
        employee.setEmployeeId(row.get("employee_id") != null ? ((Number) row.get("employee_id")).longValue() : null);
        employee.setEmployeeName((String) row.get("employee_name"));
        employee.setEmployeeAge(row.get("employee_age") != null ? ((Number) row.get("employee_age")).intValue() : null);

        // Apply business logic to skip sensitive data for inactive employees
        if (activeStatus == null || !activeStatus.equalsIgnoreCase("active")) {
            System.out.println("Skipping mapping sensitive data for inactive employee.");
            return employee; // Return DTO with only basic info
        }

        // Map employee_phone (string to list)
        String employeePhoneDb = (String) row.get("employee_phone");
        if (employeePhoneDb != null && !employeePhoneDb.isEmpty()) {
            List<String> phoneNumbers = Arrays.asList(employeePhoneDb.split(","));
            employee.setEmployeePhone(phoneNumbers);
        }

        // Map nested Address object
        Address address = new Address();
        address.setStreet((String) row.get("street"));
        address.setCity((String) row.get("city"));
        address.setZipCode((String) row.get("zip_code"));
        employee.setEmployeeAddress(address);

        return employee;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setEmployeeAge(Integer employeeAge) {
        this.employeeAge = employeeAge;
    }
    
    public void setEmployeePhone(List<String> employeePhone) {
        this.employeePhone = employeePhone;
    }

    public void setEmployeeAddress(Address employeeAddress) {
        this.employeeAddress = employeeAddress;
    }

    // Fix: Add 'static' keyword here
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address {
        private String street;
        private String city;
        private String zipCode;

        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getZipCode() { return zipCode; }

        public void setStreet(String street) { this.street = street; }
        public void setCity(String city) { this.city = city; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }
}
