package ru.cells.icc.tabbypdf.web.services;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations.PageJson;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations.PageJson.TableLocation;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.icc.cells.tabbypdf.App;
import ru.icc.cells.tabbypdf.entities.Page;
import ru.icc.cells.tabbypdf.entities.Rectangle;
import ru.icc.cells.tabbypdf.entities.table.Table;
import ru.icc.cells.tabbypdf.extraction.PdfDataExtractor;
import ru.icc.cells.tabbypdf.recognition.SimpleTableRecognizer;
import ru.icc.cells.tabbypdf.recognition.TableOptimizer;
import ru.icc.cells.tabbypdf.utils.processing.TextChunkProcessorConfiguration;
import ru.icc.cells.tabbypdf.writers.TableToExcelWriter;
import ru.icc.cells.tabbypdf.writers.TableToHtmlWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для извлечения таблиц из PDF.
 *
 * @author aaltaev
 * @since 0.1
 */
@Service
public class ExtractTablesServiceImpl implements ExtractTablesService {

    private static final Logger logger = Logger.getLogger(ExtractTablesServiceImpl.class);

    @Autowired
    private FileSaveService fileSaveService;

    @Value("${file.save.path}")
    private String fileSavePath;

    @Override
    public TableLocations extract(TableLocations json) throws IOException, TransformerException, ParserConfigurationException {
        logger.info("processing JSON");

        List<FileReference> files = new ArrayList<>();

        Long id = json.getId();

        FileReference ref = fileSaveService.findById(id);

        logger.info(String.format("trying to process pdf file %s", ref.getName()));

        PdfDataExtractor extractor = new PdfDataExtractor.Factory()
                .getPdfBoxTextExtractor(fileSavePath + ref.getName());

        PDDocument doc = PDDocument.load(new File(fileSavePath + ref.getName()));

        List<PageJson> pagesJson = json.getPages();

        List<Integer> pageNumbers = pagesJson
                .stream()
                .map(PageJson::getPageNumber)
                .collect(Collectors.toList());

        List<Page> pageContent = extractor.getPageContent();

        logger.info(String.format("processing %d tables", pageNumbers.size()));
        for (int i = 0; i < pageNumbers.size(); i++) {
            logger.info(String.format("processing page %d of %d", i + 1, pageNumbers.size()));

            Integer pageNumber = pageNumbers.get(i);

            PDRectangle pageSize = doc.getPage(i).getMediaBox();

            Page page = pageContent.get(Integer.parseInt(pageNumber.toString()));

            PageJson pageJson = pagesJson.get(pageNumber);

            logger.info(String.format("processing %d tables", pageJson.getTables().size()));
            for (int tableNumber = 0; tableNumber < pageJson.getTables().size(); tableNumber++) {
                logger.info(String.format("processing table %d of %d", tableNumber + 1, pageJson.getTables().size()));
                TableLocation tableLocation = pageJson.getTables().get(tableNumber);
                Table table = extractFromRegion(pageNumber, page.getRegion(getRectangle(tableLocation, pageSize)));

                List<Table> tableList = Collections.singletonList(table);

                logger.info("saving table to html");
                String html = new TableToHtmlWriter().write(tableList).get(0);

                logger.info("saving table to excel");
                XSSFWorkbook excelWorkbook = new TableToExcelWriter().write(tableList);

                FileReference savedHtmlRef = fileSaveService.saveText(
                        html,
                        ref.getName() + "_page_" + pageNumber + "_table_" + tableNumber + ".html"
                );
                FileReference savedExcelRef = fileSaveService.saveExcel(
                        excelWorkbook,
                        ref.getName() + "_page_" + pageNumber + "_table_" + tableNumber + ".xlsx"
                );
                files.add(savedExcelRef);
                files.add(savedHtmlRef);

                tableLocation.setHtmlId(savedHtmlRef.getId());
                tableLocation.setExcelId(savedExcelRef.getId());
                tableLocation.setHtml(html);
            }
        }
        if (files.size() > 0) {
            logger.info(String.format("packing %d files to archive", files.size()));
            FileReference archive = fileSaveService.archiveFiles(files, ref.getName() + "_results.zip");
            json.setResultId(archive.getId());
        }
        return json;
    }

    private static Rectangle getRectangle(TableLocation tableLocation,
                                          PDRectangle pageSize) {
        float height = pageSize.getHeight();
        float width = pageSize.getWidth();

        double top   = tableLocation.getTop() * height;
        double btm   = tableLocation.getBottom() * height;
        double left  = tableLocation.getLeft() * width;
        double right = tableLocation.getRight() * width;

        return new Rectangle(left, btm, right, top);
    }

    private static Table extractFromRegion(Object pageNumber, Page region)
            throws IOException, TransformerException, ParserConfigurationException {
        TextChunkProcessorConfiguration recognitionConfig = App.getRecognizingConfiguration();
        SimpleTableRecognizer recognizer = new SimpleTableRecognizer(recognitionConfig);

        Table table = recognizer.recognize(region);
        TableOptimizer optimizer = new TableOptimizer();
        optimizer.optimize(table);
        table.setPageNumber(Integer.parseInt(pageNumber.toString()));

        return table;
    }
}
