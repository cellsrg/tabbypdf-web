package ru.cells.icc.tabbypdf.web.utils;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON-утилиты
 *
 * @author aaltaev
 * @since 0.1
 */
public class JsonUtils {

    /**
     * Получение списка сортированных по алфавиту ключей json-объекта.
     * @param json json-объект
     * @return Список сортированных по алфавиту ключей json-объекта
     */
    public static List getSortedKeys(JSONObject json) {
        List keys = new ArrayList(json.keySet());
        keys.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));
        return keys;
    }

    /**
     * Получение float из json-объекта по ключу
     * @param json json-объект
     * @param field ключ
     * @return float
     */
    public static float getFloat(JSONObject json, String field) {
        Object val = json.get(field);
        float floatVal;
        try {
            floatVal = ((Double) val).floatValue();
        } catch (ClassCastException e) {
            floatVal = (Long) val;
        }
        return floatVal;
    }
}
