package ru.cells.icc.tabbypdf.web.services;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.cells.icc.tabbypdf.web.data.repository.FileReferenceRepository;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Сервис для работы с файлами.
 *
 * @author aaltaev
 * @since 0.1
 */
@Service
public class FileSaveServiceImpl implements FileSaveService {

    @Value("${file.save.path}")
    private String fileSavePath;

    @Autowired
    private FileReferenceRepository repository;

    @Override
    public FileReference saveMultipartFile(MultipartFile file) throws IOException {

        byte[] content = file.getBytes();
        try (
                FileOutputStream stream = new FileOutputStream(new File(fileSavePath + file.getOriginalFilename()));
                BufferedOutputStream bStream = new BufferedOutputStream(stream)
        ) {
            bStream.write(content);
        }
        return getFileReference(file.getOriginalFilename());
    }

    @Override
    public FileReference saveText(String text, String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileSavePath + fileName);
        fileWriter.write(text);
        fileWriter.flush();
        fileWriter.close();
        return getFileReference(fileName);
    }

    @Override
    public FileReference saveExcel(XSSFWorkbook workbook, String fileName) throws IOException {
        workbook.write(new FileOutputStream(fileSavePath + fileName));
        workbook.close();
        return getFileReference(fileName);
    }

    @Override
    public FileReference archiveFiles(List<FileReference> files, String archiveName) throws IOException {
        String zipFile = fileSavePath + archiveName;
        byte[] buffer = new byte[1024];
        try (
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            for (FileReference ref : files) {
                File refFile = new File(fileSavePath + ref.getName());
                try (FileInputStream fis = new FileInputStream(refFile)) {
                    zos.putNextEntry(new ZipEntry(refFile.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
        return getFileReference(zipFile);
    }

    @Override
    public FileReference findById(Long id) {
        return repository.findOne(id);
    }

    private FileReference getFileReference(String fileName) {
        FileReference ref = new FileReference(fileName);
        repository.save(ref);
        return ref;
    }
}
