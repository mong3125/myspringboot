package org.myspringframework.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    /**
     * JSON 문자열을 파싱하고 관련된 Java object를 반환합니다.
     * Supports parsing JSON objects, Lists, Strings, Numbers, Booleans, and null values.
     *
     * @param jsonString JSON 문자
     * @return Java object
     * @throws IllegalArgumentException if the JSON string is invalid
     */
    public Object parse(String jsonString) {
        return parseValue(jsonString);
    }

    private Object parseValue(String value) throws IllegalArgumentException {
        value = value.trim();
        if (value.startsWith("{")) {
            return parseMap(value);
        } else if (value.startsWith("[")) {
            return parseArray(value);
        } else if (value.startsWith("\"")) {
            return parseString(value);
        } else if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else if (value.equals("null")) {
            return null;
        } else {
            return parsePrimitiveOrWrapper(value);
        }
    }

    private HashMap<String, Object> parseMap(String jsonString) throws IllegalArgumentException {
        HashMap<String, Object> res = new HashMap<>();

        // whitespace를 제거하고 Map으로 변환할 수 있는지 여부를 확인합니다.
        jsonString = jsonString.trim();
        if (!jsonString.startsWith("{") || !jsonString.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON object");
        }

        // 중괄호 제거
        jsonString = jsonString.substring(1, jsonString.length() - 1).trim();

        int index = 0;
        while (index < jsonString.length()) {
            // key 파싱
            if (jsonString.charAt(index) != '\"') { // key는 문자열이여야 합니다.
                throw new IllegalArgumentException("Expected '\"' at position " + index);
            }
            int keyEnd = findClosingQuote(jsonString, index + 1);
            String key = parseString(jsonString.substring(index, keyEnd + 1));
            index = keyEnd + 1;

            // 공백과 ':' 문자를 건너뜁니다.
            index = skipWhitespace(jsonString, index);
            if (jsonString.charAt(index) != ':') {
                throw new IllegalArgumentException("Expected ':' after key at position " + index);
            }
            index++;
            index = skipWhitespace(jsonString, index);

            // value 파싱
            int[] newIndex = new int[1];
            Object value = parseValueWithIndex(jsonString, index, newIndex);
            res.put(key, value);
            index = newIndex[0];

            // comma를 건너뜁니다.
            index = skipWhitespace(jsonString, index);
            index = skipComma(jsonString, index);
        }
        return res;
    }

    /**
     * Parses a value from the given JSON string starting at the specified index,
     * and updates the index to the position immediately after the parsed value.
     *
     * @param jsonString The JSON string to parse from
     * @param start The starting index in the JSON string
     * @param newIndex An array to hold the updated index after parsing the value
     * @return The parsed object, which may be a Map, List, String, Boolean, Number, or null
     * @throws IllegalArgumentException If an error occurs during parsing
     */
    private Object parseValueWithIndex(String jsonString, int start, int[] newIndex) throws IllegalArgumentException {
        int index = start;

        if (jsonString.charAt(index) == '{') {
            int end = findClosingBrace(jsonString, index);
            String objStr = jsonString.substring(index, end + 1);
            HashMap<String, Object> obj = parseMap(objStr);
            newIndex[0] = end + 1;
            return obj;
        } else if (jsonString.charAt(index) == '[') {
            int end = findClosingBracket(jsonString, index);
            String arrStr = jsonString.substring(index, end + 1);
            List<Object> arr = parseArray(arrStr);
            newIndex[0] = end + 1;
            return arr;
        } else if (jsonString.charAt(index) == '\"') {
            int end = findClosingQuote(jsonString, index + 1);
            String str = parseString(jsonString.substring(index, end + 1));
            newIndex[0] = end + 1;
            return str;
        } else {
            // parse literals (number, true, false, null)
            int end = index;
            while (end < jsonString.length() && !isDelimiter(jsonString.charAt(end))) {
                end++;
            }
            String literal = jsonString.substring(index, end);
            Object value = parseValue(literal);
            newIndex[0] = end;
            return value;
        }
    }

    private List<Object> parseArray(String jsonString) throws IllegalArgumentException {
        List<Object> res = new ArrayList<>();
        jsonString = jsonString.trim();
        if (!jsonString.startsWith("[") || !jsonString.endsWith("]")) {
            throw new IllegalArgumentException("Invalid JSON array");
        }
        jsonString = jsonString.substring(1, jsonString.length() - 1).trim();
        int index = 0;
        while (index < jsonString.length()) {
            int[] newIndex = new int[1];
            Object value = parseValueWithIndex(jsonString, index, newIndex);
            res.add(value);
            index = newIndex[0];

            // Skip comma if present
            index = skipWhitespace(jsonString, index);
            index = skipComma(jsonString, index);
        }
        return res;
    }

    private String parseString(String value) throws IllegalArgumentException {
        if (value.length() < 2 || !value.endsWith("\"")) {
            throw new IllegalArgumentException("Invalid string value: " + value);
        }

        String content = value.substring(1, value.length() - 1);
        return unescape(content);
    }

    public static Object parsePrimitiveOrWrapper(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        value = value.trim(); // 입력값 양쪽 공백 제거

        try {
            // boolean인지 확인
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(value);
            }

            // 정수인지 확인
            if (value.matches("-?\\d+")) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return Long.parseLong(value);
                }
            }

            // 실수인지 확인
            if (value.matches("-?\\d*\\.\\d+")) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return value;
    }

    private int findClosingQuote(String s, int start) throws IllegalArgumentException {
        int index = start;
        while (index < s.length()) {
            if (s.charAt(index) == '\\') {
                index += 2; // Skip escaped character
            } else if (s.charAt(index) == '\"') {
                return index;
            } else {
                index++;
            }
        }
        throw new IllegalArgumentException("Closing quote not found");
    }

    private int findClosingBrace(String s, int start) throws IllegalArgumentException {
        int count = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '{') count++;
            else if (s.charAt(i) == '}') {
                count--;
                if (count == 0) return i;
            }
        }
        throw new IllegalArgumentException("Closing brace not found");
    }

    private int findClosingBracket(String s, int start) throws IllegalArgumentException {
        int count = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '[') count++;
            else if (s.charAt(i) == ']') {
                count--;
                if (count == 0) return i;
            }
        }
        throw new IllegalArgumentException("Closing bracket not found");
    }

    private int skipWhitespace(String s, int index) {
        while (index < s.length() && Character.isWhitespace(s.charAt(index))) {
            index++;
        }
        return index;
    }

    private int skipComma(String jsonString, int index) {
        while (index < jsonString.length() && jsonString.charAt(index) == ',') {
            index++;
            index = skipWhitespace(jsonString, index);
        }
        return index;
    }

    private boolean isDelimiter(char c) {
        return c == ',' || c == '}' || c == ']' || Character.isWhitespace(c);
    }

    private String unescape(String escaped) {
        StringBuilder res = new StringBuilder();
        int index = 0;
        while (index < escaped.length()) {
            char c = escaped.charAt(index);
            if (c == '\\') {
                char next = escaped.charAt(index + 1);
                switch (next) {
                    case '\"' -> {
                        res.append('\"');
                        index += 2;
                    }
                    case '\\' -> {
                        res.append('\\');
                        index += 2;
                    }
                    case '/' -> {
                        res.append('/');
                        index += 2;
                    }
                    case 'b' -> {
                        res.append('\b');
                        index += 2;
                    }
                    case 'f' -> {
                        res.append('\f');
                        index += 2;
                    }
                    case 'n' -> {
                        res.append('\n');
                        index += 2;
                    }
                    case 'r' -> {
                        res.append('\r');
                        index += 2;
                    }
                    case 't' -> {
                        res.append('\t');
                        index += 2;
                    }
                    case 'u' -> {
                        String hex = escaped.substring(index + 2, index + 6);
                        res.append((char) Integer.parseInt(hex, 16));
                        index += 6;
                    }
                }
            } else {
                res.append(c);
                index++;
            }
        }

        return res.toString();
    }
}
