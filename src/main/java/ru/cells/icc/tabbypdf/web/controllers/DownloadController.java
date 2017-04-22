package ru.cells.icc.tabbypdf.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.cells.icc.tabbypdf.web.services.FileSaveService;

import javax.servlet.http.HttpServletResponse;

/**
 * Контроллер для обработки запросов по скачиванию файлов.
 *
 * @author aaltaev
 * @since 0.1
 */
@RestController
@RequestMapping("**/api/download")
public class DownloadController {

    private static final Logger logger = Logger.getLogger(DownloadController.class);

    @Autowired
    private FileSaveService fileSaveService;

    /**
     * Скачивание файла.
     * @param id Идентификатор файла
     * @param response Ответ
     * @return {@link FileSystemResource} - запрашиваемый файл, или {@code null}, если файл не найден
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/zip")
    public FileSystemResource download(@PathVariable String id, HttpServletResponse response) {
        logger.info(String.format("trying to find file with id=%s", id));
        FileReference fileRef = fileSaveService.findById(Long.parseLong(id));

        if (fileRef != null) {
            String[] split = fileRef.getName().split("[/\\\\]");
            String fileName = split[split.length - 1];

            logger.info(String.format("file with id=%s was found: %s", id, fileName));

            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            return new FileSystemResource(fileRef.getName());
        } else {
            logger.error(String.format("file with id=%s was not found", id));

            return null;
        }
    }
}
