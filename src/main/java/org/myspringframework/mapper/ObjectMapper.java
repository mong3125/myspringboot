package org.myspringframework.mapper;

import org.myspringframework.annotations.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
     * Convert json string to object
     * @param json json string
     * @param type class of object
     * @return object
     * @param <T> type of object
     * @throws Exception
     */
    public <T> T readValue(String json, Class<T> type) {
        Object object = jsonParser.parse(json);
        return readObjectValue(object, type);
    }

    @SuppressWarnings("unchecked")
    private <T> T readObjectValue(Object object, Class<T> type) {
        if (object == null) {
            return null;
        }

        if (Map.class.isAssignableFrom(object.getClass())) {
            return readMapValue((Map<String, Object>) object, type);
        } else if (Collection.class.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException("아직 지원하지 않는 Collection 타입입니다: " + object.getClass().getName());
            // return readCollectionValue((Collection<?>) object, type);
        }

        return convertValue(object, type);
    }

    private <T> T readMapValue(Map<String, Object> map, Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                String fieldName = field.getName();
                Object fieldValue = map.get(fieldName);

                if (fieldValue == null) {
                    continue;   // JSON에 해당 필드가 없는 경우 현재 값을 유지
                }

                Object convertedValue = readObjectValue(fieldValue, field.getType());
                field.set(instance, convertedValue);
            }

            return instance;

        } catch (InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException e) {
            throw new RuntimeException("역직렬화 실패: " + e.getMessage(), e);
        }
    }

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

        throw new RuntimeException("지원하지 않는 타입입니다.");
    }

    /**
     * Convert object to json string
     * @param obj object
     * @return json string
     */
    public String writeValueAsString(Object obj) {
        return convertValueToJsonString(obj, 0);
    }

    private String convertValueToJsonString(Object obj, int indent) {
        StringBuilder res = new StringBuilder();

        String indentation = getIndentation(indent);
        if (obj instanceof Map) {
            res.append("{\n");
            Map<String, Object> map = (Map<String, Object>) obj;
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                Object value = map.get(key);
                res.append(indentation).append("\t").append('"').append(escapeString(key)).append("\": ");
                res.append(convertValueToJsonString(value, indent + 1));
                if (i != keys.size() - 1) {
                    res.append(",");
                }
                res.append("\n");
            }
            res.append(indentation).append("}");
        } else if (obj instanceof List) {
            res.append("[\n");
            List<Object> list = (List<Object>) obj;
            for (int i = 0; i < list.size(); i++) {
                res.append(indentation).append("\t").append(convertValueToJsonString(list.get(i), indent + 1));
                if (i != list.size() - 1) {
                    res.append(",");
                }
                res.append("\n");
            }
            res.append(indentation).append("]");
        } else if (obj instanceof String) {
            res.append('"').append(escapeString((String) obj)).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            res.append(obj.toString());
        } else if (obj == null) {
            res.append("null");
        } else if (isTimeRelatedObject(obj)) {
            res.append('"').append(obj.toString()).append('"');
        } else {
            res.append("{\n");

            Field[] fields = obj.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                try {
                    res.append(indentation).append("\t").append('"').append(field.getName()).append("\": ");
                    res.append(convertValueToJsonString(field.get(obj), indent + 1));

                    if (i < fields.length - 1) {
                        res.append(",");
                    }
                    res.append("\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            res.append(indentation).append("}");
        }
        return res.toString();
    }

    private String getIndentation(int level) {
        return "\t".repeat(Math.max(0, level));
    }

    private String escapeString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isTimeRelatedObject(Object obj) {
        return obj instanceof LocalDateTime ||
                obj instanceof LocalDate ||
                obj instanceof LocalTime;
    }
}