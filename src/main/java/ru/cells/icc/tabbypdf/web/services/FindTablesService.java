package ru.cells.icc.tabbypdf.web.services;

import org.json.simple.JSONObject;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;

import java.io.IOException;

/**
 * Интерфейс предоставляющий метод для поиска таблиц в PDF-файле
 */
public interface FindTablesService {
    /**
     * Метод для поиска таблиц в PDF-файле
     * @param file файл
     * @return JSON, содержащий координаты таблиц
     * @throws IOException если не удалось обработать PDF
     */
    JSONObject find(FileReference file) throws IOException;
}
