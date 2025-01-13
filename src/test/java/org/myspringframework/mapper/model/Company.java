package org.myspringframework.mapper.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Company {
    private String name;
    private List<Person> employees;
    private Map<String, Address> offices;

    // 기본 생성자
    public Company() {}

    public Company(String name, List<Person> employees, Map<String, Address> offices) {
        this.name = name;
        this.employees = employees;
        this.offices = offices;
    }

    // Getters 및 Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Person> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Person> employees) {
        this.employees = employees;
    }

    public Map<String, Address> getOffices() {
        return offices;
    }

    public void setOffices(Map<String, Address> offices) {
        this.offices = offices;
    }

    // equals 및 hashCode 오버라이드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;
        return Objects.equals(name, company.name) &&
                Objects.equals(employees, company.employees) &&
                Objects.equals(offices, company.offices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, employees, offices);
    }

    @Override
    public String toString() {
        return "Company{ " +
                "name='" + name + '\'' +
                ", employees=" + employees +
                ", offices=" + offices +
                " }";
    }
}
