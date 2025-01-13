package org.myspringframework.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.myspringframework.mapper.model.Address;
import org.myspringframework.mapper.model.Company;
import org.myspringframework.mapper.model.Person;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectMapperTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        JsonParser jsonParser = new JsonParser();
        objectMapper = new ObjectMapper(jsonParser);
    }

    @Test
    public void testSerialization() throws Exception {
        // When
        Address address = new Address("123 Main St", "Springfield", "12345");
        Person person = new Person("Jane Doe", 28, true, address, Arrays.asList("Java", "Spring", "SQL"));
        Company company = new Company("Tech Solutions Inc.", Arrays.asList(person), Map.of("Headquarters", address));

        // Given
        String jsonString = objectMapper.writeValueAsString(company);

        // Then
        String expectedJsonPart = "\"name\": \"Tech Solutions Inc.\"";
        assertTrue(jsonString.contains(expectedJsonPart), "JSON should contain the company name.");
        assertTrue(jsonString.contains("\"street\": \"123 Main St\""), "JSON should contain the address street.");
        assertTrue(jsonString.contains("\"name\": \"Jane Doe\""), "JSON should contain the person's name.");
    }

    @Test
    public void testDeserialization() throws Exception {
        // When
        String jsonString = """
                {
                    "name": "Tech Solutions Inc.",
                    "employees": [
                        {
                            "name": "Jane Doe",
                            "age": 28,
                            "active": true,
                            "address": {
                                "street": "123 Main St",
                                "city": "Springfield",
                                "zipcode": "12345"
                            },
                            "skills": ["Java", "Spring", "SQL"]
                        }
                    ],
                    "offices": {
                        "Headquarters": {
                            "street": "123 Main St",
                            "city": "Springfield",
                            "zipcode": "12345"
                        }
                    }
                }
                """;

        // Given
        Company company = objectMapper.readValue(jsonString, Company.class);

        // Then
        assertNotNull(company, "Deserialized company should not be null.");
        assertEquals("Tech Solutions Inc.", company.getName(), "Company name should match.");
        assertNotNull(company.getEmployees(), "Employees list should not be null.");
        assertEquals(1, company.getEmployees().size(), "There should be one employee.");
        Person person = company.getEmployees().get(0);
        assertEquals("Jane Doe", person.getName(), "Person's name should match.");
        assertEquals(28, person.getAge(), "Person's age should match.");
        assertTrue(person.isActive(), "Person should be active.");
        assertNotNull(person.getAddress(), "Person's address should not be null.");
        assertEquals("123 Main St", person.getAddress().getStreet(), "Address street should match.");
        assertEquals("Springfield", person.getAddress().getCity(), "Address city should match.");
        assertEquals("12345", person.getAddress().getZipcode(), "Address zipcode should match.");
        assertNotNull(person.getSkills(), "Person's skills should not be null.");
        assertEquals(Arrays.asList("Java", "Spring", "SQL"), person.getSkills(), "Person's skills should match.");
        assertNotNull(company.getOffices(), "Offices map should not be null.");
        assertTrue(company.getOffices().containsKey("Headquarters"), "Offices should contain 'Headquarters'.");
        Address officeAddress = company.getOffices().get("Headquarters");
        assertEquals("123 Main St", officeAddress.getStreet(), "Office address street should match.");
    }

    @Test
    public void testSerializeDeserialize() throws Exception {
        // When
        Address homeAddress = new Address("123 Main St", "Springfield", "12345");
        Address officeAddress = new Address("456 Corporate Blvd", "Metropolis", "67890");

        Person person1 = new Person("Jane Doe", 28, true, homeAddress, Arrays.asList("Java", "Spring", "SQL"));
        Person person2 = new Person("John Smith", 35, false, homeAddress, Arrays.asList("Python", "Django", "JavaScript"));
        Person person3 = new Person("Alice Johnson", 30, true, officeAddress, Arrays.asList("C#", ".NET", "Azure"));

        List<Person> employees = Arrays.asList(person1, person2, person3);
        Map<String, Address> offices = new HashMap<>();
        offices.put("Headquarters", officeAddress);
        offices.put("Branch", homeAddress);

        Company originalCompany = new Company("Tech Solutions Inc.", employees, offices);

        // Given
        String jsonString = objectMapper.writeValueAsString(originalCompany);
        Company deserializedCompany = objectMapper.readValue(jsonString, Company.class);

        // Then
        assertEquals(originalCompany, deserializedCompany, "Deserialized company should be equal to the original.");
    }

    @Test
    public void testNullValues() throws Exception {
        // When
        Company company = new Company();
        company.setName(null);
        company.setEmployees(null);
        company.setOffices(null);

        // Given
        String jsonString = objectMapper.writeValueAsString(company);
        Company deserializedCompany = objectMapper.readValue(jsonString, Company.class);

        // Then
        assertNotNull(deserializedCompany, "Deserialized company should not be null.");
        assertNull(deserializedCompany.getName(), "Company name should be null.");
        assertNull(deserializedCompany.getEmployees(), "Employees list should be null.");
        assertNull(deserializedCompany.getOffices(), "Offices map should be null.");
    }
}
