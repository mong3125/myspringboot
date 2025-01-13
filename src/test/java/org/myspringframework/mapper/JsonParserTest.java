package org.myspringframework.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    private JsonParser jsonParser;

    @BeforeEach
    public void setUp() {
        jsonParser = new JsonParser();
    }

    @Test
    public void testParseNull() {
        // given
        String json = "null";

        // when
        Object result = jsonParser.parse(json);

        // then
        assertNull(result, "Parsing 'null' should return null.");
    }

    @Test
    public void testParseBooleanTrue() {
        // given
        String json = "true";

        // when
        Object result = jsonParser.parse(json);

        // then
        assertEquals(Boolean.TRUE, result, "Parsing 'true' should return Boolean.TRUE.");
    }

    @Test
    public void testParseBooleanFalse() {
        // given
        String json = "false";

        // when
        Object result = jsonParser.parse(json);

        // then
        assertEquals(Boolean.FALSE, result, "Parsing 'false' should return Boolean.FALSE.");
    }

    @Test
    public void testParseInteger() {
        // Arrange
        String json = "123";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertEquals(123, result, "Parsed integer value should be 123.");
    }

    @Test
    public void testParseLong() {
        // Arrange
        String json = "1234567890123";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof Long, "Result should be of type Long.");
        assertEquals(1234567890123L, result, "Parsed long value should be 1234567890123L.");
    }

    @Test
    public void testParseDouble() {
        // Arrange
        String json = "123.456";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof Double, "Result should be of type Double.");
        assertEquals(123.456, (Double) result, 0.0001, "Parsed double value should be 123.456.");
    }

    @Test
    public void testParseString() {
        // Arrange
        String json = "\"Hello, World!\"";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof String, "Result should be of type String.");
        assertEquals("Hello, World!", result, "Parsed string should be 'Hello, World!'.");
    }

    @Test
    public void testParseEmptyObject() {
        // Arrange
        String json = "{}";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof HashMap, "Result should be of type HashMap.");
        assertTrue(((HashMap<?, ?>) result).isEmpty(), "Parsed empty JSON object should result in an empty HashMap.");
    }

    @Test
    public void testParseEmptyArray() {
        // Arrange
        String json = "[]";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof ArrayList, "Result should be of type ArrayList.");
        assertTrue(((List<?>) result).isEmpty(), "Parsed empty JSON array should result in an empty ArrayList.");
    }

    @Test
    public void testParseSimpleObject() {
        // Arrange
        String json = "{\"name\": \"John\", \"age\": 30}";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof HashMap, "Result should be of type HashMap.");
        HashMap<?, ?> map = (HashMap<?, ?>) result;
        assertEquals(2, map.size(), "Parsed JSON object should have 2 entries.");
        assertEquals("John", map.get("name"), "Value for key 'name' should be 'John'.");
        assertEquals(30, map.get("age"), "Value for key 'age' should be 30.");
    }

    @Test
    public void testParseNestedObject() {
        // Arrange
        String json = "{"
                + "\"person\": {"
                + "    \"name\": \"Alice\","
                + "    \"details\": {"
                + "        \"age\": 25,"
                + "        \"active\": true"
                + "    }"
                + "},"
                + "\"hobbies\": [\"Reading\", \"Cycling\"]"
                + "}";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof HashMap, "Result should be of type HashMap.");
        HashMap<?, ?> map = (HashMap<?, ?>) result;
        assertEquals(2, map.size(), "Parsed JSON object should have 2 entries.");

        // Verify 'person' object
        Object personObj = map.get("person");
        assertTrue(personObj instanceof HashMap, "'person' should be a HashMap.");
        HashMap<?, ?> personMap = (HashMap<?, ?>) personObj;
        assertEquals(2, personMap.size(), "'person' should have 2 entries.");
        assertEquals("Alice", personMap.get("name"), "Person's name should be 'Alice'.");

        // Verify 'details' object
        Object detailsObj = personMap.get("details");
        assertTrue(detailsObj instanceof HashMap, "'details' should be a HashMap.");
        HashMap<?, ?> detailsMap = (HashMap<?, ?>) detailsObj;
        assertEquals(2, detailsMap.size(), "'details' should have 2 entries.");
        assertEquals(25, detailsMap.get("age"), "Age should be 25.");
        assertEquals(true, detailsMap.get("active"), "Active should be true.");

        // Verify 'hobbies' array
        Object hobbiesObj = map.get("hobbies");
        assertTrue(hobbiesObj instanceof ArrayList, "'hobbies' should be an ArrayList.");
        ArrayList<?> hobbiesList = (ArrayList<?>) hobbiesObj;
        assertEquals(2, hobbiesList.size(), "'hobbies' should have 2 elements.");
        assertEquals("Reading", hobbiesList.get(0), "First hobby should be 'Reading'.");
        assertEquals("Cycling", hobbiesList.get(1), "Second hobby should be 'Cycling'.");
    }

    @Test
    public void testParseArrayOfObjects() {
        // Arrange
        String json = "["
                + "{\"id\": 1, \"value\": \"A\"},"
                + "{\"id\": 2, \"value\": \"B\"},"
                + "{\"id\": 3, \"value\": \"C\"}"
                + "]";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof ArrayList, "Result should be of type ArrayList.");
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(3, list.size(), "Parsed JSON array should have 3 elements.");

        // Verify each object in the array
        for (int i = 0; i < 3; i++) {
            Object item = list.get(i);
            assertTrue(item instanceof HashMap, "Each item should be a HashMap.");
            HashMap<?, ?> itemMap = (HashMap<?, ?>) item;
            assertEquals(2, itemMap.size(), "Each object should have 2 entries.");
            assertEquals(i + 1, itemMap.get("id"), "ID should match.");
            assertEquals(String.valueOf((char) ('A' + i)), itemMap.get("value"), "Value should match.");
        }
    }

    @Test
    public void testParseComplexNestedStructure() {
        // Arrange
        String json = "{"
                + "\"users\": ["
                + "    {"
                + "        \"id\": 1,"
                + "        \"name\": \"User1\","
                + "        \"roles\": [\"admin\", \"user\"]"
                + "    },"
                + "    {"
                + "        \"id\": 2,"
                + "        \"name\": \"User2\","
                + "        \"roles\": [\"user\"]"
                + "    }"
                + "],"
                + "\"metadata\": {"
                + "    \"count\": 2,"
                + "    \"active\": true"
                + "}"
                + "}";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof HashMap, "Result should be of type HashMap.");
        HashMap<?, ?> map = (HashMap<?, ?>) result;

        // Verify 'users' array
        Object usersObj = map.get("users");
        assertTrue(usersObj instanceof ArrayList, "'users' should be an ArrayList.");
        ArrayList<?> usersList = (ArrayList<?>) usersObj;
        assertEquals(2, usersList.size(), "'users' array should have 2 elements.");

        // Verify first user
        Object user1Obj = usersList.get(0);
        assertTrue(user1Obj instanceof HashMap, "First user should be a HashMap.");
        HashMap<?, ?> user1Map = (HashMap<?, ?>) user1Obj;
        assertEquals(3, user1Map.size(), "First user should have 3 entries.");
        assertEquals(1, user1Map.get("id"), "First user's ID should be 1.");
        assertEquals("User1", user1Map.get("name"), "First user's name should be 'User1'.");
        Object roles1Obj = user1Map.get("roles");
        assertTrue(roles1Obj instanceof ArrayList, "First user's roles should be an ArrayList.");
        ArrayList<?> roles1List = (ArrayList<?>) roles1Obj;
        assertEquals(Arrays.asList("admin", "user"), roles1List, "First user's roles should match.");

        // Verify second user
        Object user2Obj = usersList.get(1);
        assertTrue(user2Obj instanceof HashMap, "Second user should be a HashMap.");
        HashMap<?, ?> user2Map = (HashMap<?, ?>) user2Obj;
        assertEquals(3, user2Map.size(), "Second user should have 3 entries.");
        assertEquals(2, user2Map.get("id"), "Second user's ID should be 2.");
        assertEquals("User2", user2Map.get("name"), "Second user's name should be 'User2'.");
        Object roles2Obj = user2Map.get("roles");
        assertTrue(roles2Obj instanceof ArrayList, "Second user's roles should be an ArrayList.");
        ArrayList<?> roles2List = (ArrayList<?>) roles2Obj;
        assertEquals(Collections.singletonList("user"), roles2List, "Second user's roles should match.");

        // Verify 'metadata' object
        Object metadataObj = map.get("metadata");
        assertTrue(metadataObj instanceof HashMap, "'metadata' should be a HashMap.");
        HashMap<?, ?> metadataMap = (HashMap<?, ?>) metadataObj;
        assertEquals(2, metadataMap.size(), "'metadata' should have 2 entries.");
        assertEquals(2, metadataMap.get("count"), "Metadata count should be 2.");
        assertEquals(true, metadataMap.get("active"), "Metadata active should be true.");
    }

    @Test
    public void testParseEscapedString() {
        // Arrange
        String json = "\"Hello, \\\"World\\\"!\\\\\"";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof String, "Result should be of type String.");
        assertEquals("Hello, \"World\"!\\", result, "Parsed string should handle escaped characters correctly.");
    }

    @Test
    public void testParseWithWhitespace() {
        // Arrange
        String json = "   {   \n"
                + "    \"key1\"  :  \"value1\" , \n"
                + "    \"key2\" : [ 1, 2, 3 ] ,\n"
                + "    \"key3\" : { \"nestedKey\" : true } \n"
                + "}   ";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof HashMap, "Result should be of type HashMap.");
        HashMap<?, ?> map = (HashMap<?, ?>) result;
        assertEquals(3, map.size(), "Parsed JSON object should have 3 entries.");
        assertEquals("value1", map.get("key1"), "Value for 'key1' should be 'value1'.");

        // Verify 'key2' array
        Object key2Obj = map.get("key2");
        assertTrue(key2Obj instanceof ArrayList, "'key2' should be an ArrayList.");
        ArrayList<?> key2List = (ArrayList<?>) key2Obj;
        assertEquals(Arrays.asList(1, 2, 3), key2List, "'key2' array should match.");

        // Verify 'key3' object
        Object key3Obj = map.get("key3");
        assertTrue(key3Obj instanceof HashMap, "'key3' should be a HashMap.");
        HashMap<?, ?> key3Map = (HashMap<?, ?>) key3Obj;
        assertEquals(1, key3Map.size(), "'key3' should have 1 entry.");
        assertEquals(true, key3Map.get("nestedKey"), "'nestedKey' should be true.");
    }

    @Test
    public void testParseInvalidJsonObject() {
        // Arrange
        String json = "{ \"key1\": \"value1\", \"key2\": [1, 2, 3}, \"key3\": true }"; // Mismatched braces

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jsonParser.parse(json);
        }, "Parsing invalid JSON object should throw IllegalArgumentException.");

        assertTrue(exception.getMessage().contains("Closing bracket not found") ||
                        exception.getMessage().contains("Closing quote not found"),
                "Exception message should indicate the specific parsing error.");
    }

    @Test
    public void testParseInvalidJsonArray() {
        // Arrange
        String json = "[1, 2, 3, 4"; // Missing closing bracket

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jsonParser.parse(json);
        }, "Parsing invalid JSON array should throw IllegalArgumentException.");
    }

    @Test
    public void testParseMalformedString() {
        // Arrange
        String json = "\"Unclosed string";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jsonParser.parse(json);
        }, "Parsing malformed string should throw IllegalArgumentException.");
    }

    @Test
    public void testParseMixedTypesInArray() {
        // Arrange
        String json = "[1, \"two\", true, null, {\"three\": 3}, [4, 5]]";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof ArrayList, "Result should be of type ArrayList.");
        ArrayList<?> list = (ArrayList<?>) result;
        assertEquals(6, list.size(), "Parsed array should have 6 elements.");
        assertEquals(1, list.get(0), "First element should be 1.");
        assertEquals("two", list.get(1), "Second element should be 'two'.");
        assertEquals(true, list.get(2), "Third element should be true.");
        assertNull(list.get(3), "Fourth element should be null.");

        // Verify nested object
        Object obj = list.get(4);
        assertTrue(obj instanceof HashMap, "Fifth element should be a HashMap.");
        HashMap<?, ?> objMap = (HashMap<?, ?>) obj;
        assertEquals(1, objMap.size(), "Nested object should have 1 entry.");
        assertEquals(3, objMap.get("three"), "Value for 'three' should be 3.");

        // Verify nested array
        Object nestedArr = list.get(5);
        assertTrue(nestedArr instanceof ArrayList, "Sixth element should be an ArrayList.");
        ArrayList<?> nestedList = (ArrayList<?>) nestedArr;
        assertEquals(Arrays.asList(4, 5), nestedList, "Nested array should match [4, 5].");
    }

    @Test
    public void testParseStringWithEscapedCharacters() {
        // Arrange
        String json = "\"Line1\\nLine2\\tTabbed\\\\Backslash\\\"Quote\"";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof String, "Result should be of type String.");
        assertEquals("Line1\nLine2\tTabbed\\Backslash\"Quote", result, "Parsed string should correctly handle escaped characters.");
    }

    @Test
    public void testParseBooleanAsString() {
        // Arrange
        String json = "\"true\"";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof String, "Result should be of type String, not Boolean.");
        assertEquals("true", result, "Parsed string should be 'true'.");
    }

    @Test
    public void testParseEmptyString() {
        // Arrange
        String json = "\"\"";

        // Act
        Object result = jsonParser.parse(json);

        // Assert
        assertTrue(result instanceof String, "Result should be of type String.");
        assertEquals("", result, "Parsed string should be empty.");
    }
}
