package ru.cells.icc.tabbypdf.web.controllers;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.cells.icc.tabbypdf.web.services.FileSaveService;
import ru.cells.icc.tabbypdf.web.services.FindTablesService;

import java.io.IOException;

/**
 * Контроллер, отвечающий за загрузку pdf и обнаружение таблиц.
 *
 * @author aaltaev
 * @since 0.1
 */
@RestController
@RequestMapping("**/api/upload")
public class PdfUploadController {

    private static final Logger logger = Logger.getLogger(PdfUploadController.class);

    @Autowired
    private FileSaveService fileSaveService;
    @Autowired
    private FindTablesService findTablesService;

    /**
     * Загрузка pdf и обнаружение таблиц.
     * @param file PDF-файл
     * @return {@link ResponseEntity}&lt;{@link JSONObject}&gt; - JSON, содержащий id файла и координаты таблиц
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<TableLocations> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            logger.info(String.format("saving pdf: %s", file.getName()));
            FileReference savedFile = fileSaveService.saveMultipartFile(file);
            logger.info(String.format("pdf successfully saved, id=%s", savedFile.getId()));

            logger.info("trying to find tables");
            TableLocations json = findTablesService.find(savedFile);
            logger.info("tables are found");

            json.setId(savedFile.getId());
            return new ResponseEntity<>(json, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("could not save file or find tables:", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
