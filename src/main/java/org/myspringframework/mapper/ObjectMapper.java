package org.myspringframework.mapper;

import org.myspringframework.annotations.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class ObjectMapper {
    @Autowired
    private JsonParser jsonParser;

    public ObjectMapper() {
    }

    public ObjectMapper(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    /**
     * JSON 문자열을 객체로 변환
     * @param json JSON 문자열
     * @param type 변환할 객체의 타입
     * @return 객체
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Type type) {
        Object object = jsonParser.parse(json);
        return (T) readObjectValue(object, type);
    }

    private Object readObjectValue(Object object, Type type) {
        if (object == null) {
            return null;
        }

        if (type instanceof ParameterizedType pType) {
            // 예: List<?>, Map<String, ?> 등 제네릭 타입 처리
            return handleParameterizedType(object, pType);

        } else if (type instanceof Class<?> clazz) {
            // 예: String, Integer, MyDto ... 등 클래스 타입 처리
            return handleClassType(object, clazz);

        } else {
            throw new UnsupportedOperationException("지원하지 않는 타입: " + type);
        }
    }

    private Object handleParameterizedType(Object object, ParameterizedType pType) {
        Type rawType = pType.getRawType(); // 예: List, Map
        Type[] actualTypes = pType.getActualTypeArguments();

        // 1. List<T>
        if (rawType == List.class) {
            return handleList(object, actualTypes[0]);

            // 2. Set<T>
        } else if (rawType == Set.class) {
            return handleSet(object, actualTypes[0]);

            // 3. Map<K, V>
        } else if (rawType == Map.class) {
            return handleMap(object, actualTypes[0], actualTypes[1]);
        }

        // 그 외 커스텀 제네릭(예: Optional<T>, Queue<T> 등)
        throw new UnsupportedOperationException("아직 지원하지 않는 ParameterizedType입니다. : " + rawType);
    }

    private Object handleClassType(Object object, Class<?> clazz) {
        // 1. 기본 래퍼 or String or Number or DateTime
        if (isWrapperOrString(clazz)) {
            return convertValue(object, clazz);
        }

        // 2. Map (제네릭 정보가 전혀 없이 Map.class로만 넘어온 경우)
        if (Map.class.isAssignableFrom(clazz)) {
            return handleMapNoGeneric(object);
        }

        // 3. List (제네릭 정보 없이 List.class로만 넘어온 경우)
        if (List.class.isAssignableFrom(clazz)) {
            return handleListNoGeneric(object);
        }

        // 4. Set (제네릭 정보 없이 Set.class로만 넘어온 경우)
        if (Set.class.isAssignableFrom(clazz)) {
            return handleSetNoGeneric(object);
        }

        // 4. 그 외엔 일반 POJO
        return handlePojo(object, clazz);
    }

    /**
     * List<T> 처리
     */
    private Object handleList(Object object, Type elementType) {
        if (!(object instanceof List<?> sourceCollection)) {
            throw new RuntimeException("JSON 구조가 List가 아닌데, List로 역직렬화하려 함");
        }

        List<Object> resultList = new ArrayList<>();
        for (Object item : sourceCollection) {
            Object converted = readObjectValue(item, elementType);
            resultList.add(converted);
        }
        return resultList;
    }

    /**
     * Set<T> 처리
     */
    private Object handleSet(Object object, Type elementType) {
        if (!(object instanceof Set<?> sourceCollection)) {
            throw new RuntimeException("JSON 구조가 Set이 아닌데, Set으로 역직렬화하려 함");
        }

        Set<Object> resultSet = new HashSet<>();
        for (Object item : sourceCollection) {
            Object converted = readObjectValue(item, elementType);
            resultSet.add(converted);
        }
        return resultSet;
    }

    /**
     * Map<K, V> 처리
     */
    private Object handleMap(Object object, Type keyType, Type valueType) {
        if (!(object instanceof Map<?, ?> sourceMap)) {
            throw new RuntimeException("JSON 구조가 Map이 아닌데, Map으로 역직렬화하려 함");
        }

        Map<Object, Object> resultMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            Object key = readObjectValue(entry.getKey(), keyType);
            Object val = readObjectValue(entry.getValue(), valueType);
            resultMap.put(key, val);
        }
        return resultMap;
    }

    /**
     * 제네릭 정보가 없는 Collection.class 처리
     *  -> 모든 요소를 Object.class로 역직렬화
     */
    private Object handleListNoGeneric(Object object) {
        if (!(object instanceof List<?> sourceCollection)) {
            throw new RuntimeException("JSON 구조가 List가 아닌데, List로 역직렬화하려 함");
        }

        List<Object> resultList = new ArrayList<>();
        for (Object item : sourceCollection) {
            Object converted = readObjectValue(item, Object.class);
            resultList.add(converted);
        }
        return resultList;
    }

    /**
     * 제네릭 정보가 없는 Set.class 처리
     *  -> 모든 요소를 Object.class로 역직렬화
     */
    private Object handleSetNoGeneric(Object object) {
        if (!(object instanceof Set<?> sourceCollection)) {
            throw new RuntimeException("JSON 구조가 Set이 아닌데, Set으로 역직렬화하려 함");
        }

        Set<Object> resultSet = new HashSet<>();
        for (Object item : sourceCollection) {
            Object converted = readObjectValue(item, Object.class);
            resultSet.add(converted);
        }
        return resultSet;
    }

    /**
     * 제네릭 정보가 없는 Map.class 처리
     *  -> (key, value) 전부 Object.class로 역직렬화
     */
    private Object handleMapNoGeneric(Object object) {
        if (!(object instanceof Map<?, ?> sourceMap)) {
            throw new RuntimeException("JSON 구조가 Map이 아닌데, Map으로 역직렬화하려 함");
        }

        Map<Object, Object> resultMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            Object key = readObjectValue(entry.getKey(), Object.class);
            Object val = readObjectValue(entry.getValue(), Object.class);
            resultMap.put(key, val);
        }
        return resultMap;
    }

    /**
     * 일반 POJO (ex: MyDto, UserEntity 등) 처리
     */
    private Object handlePojo(Object obj, Class<?> clazz) {
        if (!(obj instanceof Map)) {
            throw new RuntimeException("JSON 구조가 객체({})가 아닌데, POJO로 역직렬화하려 함");
        }

        Map<String, Object> map = (Map<String, Object>) obj;

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object rawValue = map.get(field.getName());
                if (rawValue == null) continue;

                // 필드의 제네릭 정보까지 포함
                Type genericFieldType = field.getGenericType();
                Object convertedValue = readObjectValue(rawValue, genericFieldType);

                field.set(instance, convertedValue);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("POJO 변환 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 문자열, 숫자, 날짜/시간 등 기본 타입 변환
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object fieldValue, Class<T> type) {
        if (fieldValue == null) {
            return null;
        }

        if (type == String.class) {
            return (T) fieldValue.toString();
        }

        if (type == Boolean.class || type == boolean.class) {
            if (fieldValue instanceof Boolean) {
                return (T) fieldValue;
            }
            return (T) Boolean.valueOf(fieldValue.toString());
        }

        if (type == Integer.class || type == int.class) {
            if (fieldValue instanceof Number) {
                return (T) Integer.valueOf(((Number) fieldValue).intValue());
            }
            return (T) Integer.valueOf(fieldValue.toString());
        }

        if (type == Long.class || type == long.class) {
            if (fieldValue instanceof Number) {
                return (T) Long.valueOf(((Number) fieldValue).longValue());
            }
            return (T) Long.valueOf(fieldValue.toString());
        }

        if (type == Double.class || type == double.class) {
            if (fieldValue instanceof Number) {
                return (T) Double.valueOf(((Number) fieldValue).doubleValue());
            }
            return (T) Double.valueOf(fieldValue.toString());
        }

        if (type == LocalDate.class) {
            return (T) LocalDate.parse(fieldValue.toString());
        }

        if (type == LocalTime.class) {
            return (T) LocalTime.parse(fieldValue.toString());
        }

        if (type == LocalDateTime.class) {
            return (T) LocalDateTime.parse(fieldValue.toString());
        }

        throw new RuntimeException("지원하지 않는 타입입니다: " + type);
    }

    /**
     * 기본 래퍼/문자열/숫자/시간 타입인지 확인
     */
    private boolean isWrapperOrString(Class<?> type) {
        return type == String.class
                || type == Boolean.class || type == boolean.class
                || Number.class.isAssignableFrom(type)
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Double.class || type == double.class
                || type == LocalDate.class
                || type == LocalTime.class
                || type == LocalDateTime.class;
    }

    /**
     * 객체를 JSON 문자열로 변환
     * @param obj 객체
     * @return json JSON 문자열
     */
    public String writeValueAsString(Object obj) {
        return convertValueToJsonString(obj, 0);
    }

    /**
     * JSON 문자열 변환의 핵심 메서드
     */
    private String convertValueToJsonString(Object obj, int indent) {
        if (obj == null) {
            return handleNull();
        }
        if (obj instanceof Map) {
            return handleMap((Map<?, ?>) obj, indent);
        }
        if (obj instanceof List) {
            return handleList((List<?>) obj, indent);
        }
        if (obj instanceof String) {
            return handleString((String) obj);
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return handleNumberOrBoolean(obj);
        }
        if (isTimeRelatedObject(obj)) {
            return handleTimeObject(obj);
        }
        return handlePojo(obj, indent);
    }

    /**
     * Map 처리
     */
    @SuppressWarnings("unchecked")
    private String handleMap(Map<?, ?> mapObj, int indent) {
        // Map의 Key를 정렬하기 위해 String으로 캐스팅 후 정렬
        // (Key가 모두 String이라고 가정함)
        Map<String, Object> map = (Map<String, Object>) mapObj;
        StringBuilder res = new StringBuilder();
        String indentation = getIndentation(indent);

        res.append("{\n");
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = map.get(key);

            res.append(indentation).append("\t")
                    .append('"').append(escapeString(key)).append("\": ")
                    .append(convertValueToJsonString(value, indent + 1));

            if (i != keys.size() - 1) {
                res.append(",");
            }
            res.append("\n");
        }
        res.append(indentation).append("}");
        return res.toString();
    }

    /**
     * List 처리
     */
    private String handleList(List<?> listObj, int indent) {
        StringBuilder res = new StringBuilder();
        String indentation = getIndentation(indent);

        res.append("[\n");
        for (int i = 0; i < listObj.size(); i++) {
            Object item = listObj.get(i);

            res.append(indentation).append("\t")
                    .append(convertValueToJsonString(item, indent + 1));

            if (i != listObj.size() - 1) {
                res.append(",");
            }
            res.append("\n");
        }
        res.append(indentation).append("]");
        return res.toString();
    }

    /**
     * String 처리 (escape 처리)
     */
    private String handleString(String value) {
        return "\"" + escapeString(value) + "\"";
    }

    /**
     * Number, Boolean 처리
     */
    private String handleNumberOrBoolean(Object value) {
        // 숫자, 불리언은 그대로 문자열화
        return value.toString();
    }

    /**
     * null 처리
     */
    private String handleNull() {
        return "null";
    }

    /**
     * LocalDate, LocalTime, LocalDateTime 등 시간 관련 객체 처리
     */
    private String handleTimeObject(Object timeObj) {
        // 보통 JSON에서는 날짜/시간을 문자열로 표현
        return "\"" + timeObj.toString() + "\"";
    }

    /**
     * 일반 POJO 처리
     */
    private String handlePojo(Object obj, int indent) {
        StringBuilder res = new StringBuilder();
        String indentation = getIndentation(indent);

        res.append("{\n");

        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            try {
                Object fieldValue = field.get(obj);

                res.append(indentation).append("\t")
                        .append("\"").append(field.getName()).append("\": ")
                        .append(convertValueToJsonString(fieldValue, indent + 1));

                if (i < fields.length - 1) {
                    res.append(",");
                }
                res.append("\n");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("POJO 객체 변환 실패: " + e.getMessage(), e);
            }
        }

        res.append(indentation).append("}");
        return res.toString();
    }

    /**
     * 인덴트 문자열 생성
     */
    private String getIndentation(int indent) {
        return "\t".repeat(Math.max(0, indent));
    }

    /**
     * 문자열 escape 처리
     */
    private String escapeString(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * LocalDate, LocalTime, LocalDateTime 등 시간 관련 객체인지 확인
     */
    private boolean isTimeRelatedObject(Object obj) {
        return obj instanceof LocalDateTime
                || obj instanceof LocalDate
                || obj instanceof LocalTime;
    }
}