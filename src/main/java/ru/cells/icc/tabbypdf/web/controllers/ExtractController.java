package ru.cells.icc.tabbypdf.web.controllers;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cells.icc.tabbypdf.web.services.ExtractTablesService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Контроллер для обработки запросов по извлечению таблиц.
 *
 * @author aaltaev
 * @since 0.1
 */
@RestController
@RequestMapping("**/api/extract")
public class ExtractController {

    private static final Logger logger = Logger.getLogger(ExtractController.class);

    @Autowired
    private ExtractTablesService extractTablesService;

    /**
     * Извлечение таблиц.
     * @param data JSON, содержащий в себе идентификатор pdf-файла, и координаты таблиц
     * @return {@link ResponseEntity}&lt;{@link JSONObject}&gt; - JSON, содержащий таблицы в HTML,
     *         и id файла для скачивания
     */
    @RequestMapping(method = {RequestMethod.POST})
    public ResponseEntity<JSONObject> extract(@RequestParam("data") String data) {
        try {
            logger.info("trying to parse incoming json");
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(data);

            logger.info("trying to extract tables");
            JSONObject result = extractTablesService.extract(json);
            logger.info("successfully extracted. sending response with resulting JSON");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IOException | TransformerException | ParserConfigurationException e) {
            logger.error("could not extract tables:", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ParseException e) {
            logger.error("malformed JSON:", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
