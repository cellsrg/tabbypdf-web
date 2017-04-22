package ru.cells.icc.tabbypdf.web.services;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;

import java.io.IOException;
import java.util.List;

/**
 * Интерфейс, предоставляющий методы для работы с файлами.
 */
public interface FileSaveService {

    /**
     * Сохранение загруженного файла.
     * @param file загруженный файл
     * @return {@link FileReference} для сохраненного файла
     * @throws IOException если не удалось сохранить
     */
    FileReference saveMultipartFile(MultipartFile file) throws IOException;

    /**
     * Сохранение текстового файла.
     * @param text текст
     * @param fileName название файла
     * @return {@link FileReference} для сохраненного файла
     * @throws IOException если не удалось сохранить
     */
    FileReference saveText(String text, String fileName) throws IOException;

    /**
     * Сохранение excel-файла.
     * @param workbook excel-файл
     * @param fileName название файла
     * @return {@link FileReference} для сохраненного файла
     * @throws IOException если не удалось сохранить
     */
    FileReference saveExcel(XSSFWorkbook workbook, String fileName) throws IOException;

    /**
     * Архивация файлов и сохранение архива.
     * @param files файлы для архивации
     * @param archiveName название архива
     * @return {@link FileReference} для сохраненного архива
     * @throws IOException IOException если не удалось сохранить архив
     */
    FileReference archiveFiles(List<FileReference> files, String archiveName) throws IOException;

    /**
     * Поиск файла по id.
     * @param id id файла
     * @return {@link FileReference} для файла
     */
    FileReference findById(Long id);
}
