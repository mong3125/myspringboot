package org.myspringframework.mapper.model;

import java.util.List;
import java.util.Objects;

public class Person {
    private String name;
    private int age;
    private boolean active;
    private Address address;
    private List<String> skills;

    // 기본 생성자
    public Person() {}

    public Person(String name, int age, boolean active, Address address, List<String> skills) {
        this.name = name;
        this.age = age;
        this.active = active;
        this.address = address;
        this.skills = skills;
    }

    // Getters 및 Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    // equals 및 hashCode 오버라이드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;
        return age == person.age &&
                active == person.active &&
                Objects.equals(name, person.name) &&
                Objects.equals(address, person.address) &&
                Objects.equals(skills, person.skills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, active, address, skills);
    }

    @Override
    public String toString() {
        return "Person{ " +
                "name='" + name + '\'' +
                ", age=" + age +
                ", active=" + active +
                ", address=" + address +
                ", skills=" + skills +
                " }";
    }
}
