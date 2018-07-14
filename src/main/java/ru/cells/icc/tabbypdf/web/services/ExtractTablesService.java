package ru.cells.icc.tabbypdf.web.services;

import org.json.simple.JSONObject;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Интерфейс, предоставляющий метод для извлечения таблиц из PDF.
 *
 * @author aaltaev
 * @since 0.1
 */
public interface ExtractTablesService {
    /**
     * Извлечение таблиц из PDF.
     * @param json JSON, содержащий id pdf-файла и координаты таблиц
     * @return {@link JSONObject} - JSON, содержащий таблицы в виде HTML, и id файла для скачивания
     * @throws IOException Если не получилось извлечь таблицы
     * @throws TransformerException Если не получилось трансформировать таблицы в html
     * @throws ParserConfigurationException Если не получилось трансформировать таблицы в html
     */
    TableLocations extract(TableLocations json) throws IOException, TransformerException, ParserConfigurationException;
}
