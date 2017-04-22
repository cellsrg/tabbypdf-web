package ru.cells.icc.tabbypdf.web.services;

import com.itextpdf.text.pdf.PdfReader;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.cells.icc.tabbypdf.web.utils.JsonUtils;
import ru.icc.cells.tabbypdf.App;
import ru.icc.cells.tabbypdf.common.Page;
import ru.icc.cells.tabbypdf.common.Rectangle;
import ru.icc.cells.tabbypdf.common.table.Table;
import ru.icc.cells.tabbypdf.recognizers.SimpleTableRecognizer;
import ru.icc.cells.tabbypdf.recognizers.TableOptimizer;
import ru.icc.cells.tabbypdf.utils.content.PdfContentExtractor;
import ru.icc.cells.tabbypdf.utils.processing.TextChunkProcessorConfiguration;
import ru.icc.cells.tabbypdf.writers.TableToExcelWriter;
import ru.icc.cells.tabbypdf.writers.TableToHtmlWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public JSONObject extract(JSONObject json) throws IOException, TransformerException, ParserConfigurationException {
        logger.info("processing JSON");

        List<FileReference> files = new ArrayList<>();

        Long id = (Long) json.get("id");

        JSONObject resultJson = new JSONObject();
        resultJson.put("id", id);
        JSONObject resultPages = new JSONObject();
        resultJson.put("pages", resultPages);

        FileReference ref = fileSaveService.findById(id);

        PdfContentExtractor extractor = null;
        PdfReader reader = null;
        try {
            logger.info(String.format("trying to process pdf file %s", ref.getName()));
            extractor = new PdfContentExtractor(fileSavePath + ref.getName());
            reader = new PdfReader(fileSavePath + ref.getName());

            JSONObject pagesJson = (JSONObject) json.get("pages");
            List pageNumbers = JsonUtils.getSortedKeys(pagesJson);

            logger.info(String.format("processing %d pages", pageNumbers.size()));
            for (int i = 0; i < pageNumbers.size(); i++) {
                logger.info(String.format("processing page %d of %d", i + 1, pageNumbers.size()));

                Object pageNumber = pageNumbers.get(i);

                JSONObject resultPage = new JSONObject();
                resultPages.put(pageNumber, resultPage);

                com.itextpdf.text.Rectangle pageSize = reader.getPageSize(Integer.parseInt(pageNumber.toString()) + 1);

                Page page = extractor.getPageContent(Integer.parseInt(pageNumber.toString()));

                JSONObject pageJson = (JSONObject) pagesJson.get(pageNumber);
                List rectangleIdsJson = JsonUtils.getSortedKeys(pageJson);

                logger.info(String.format("processing %d tables", rectangleIdsJson.size()));
                for (int j = 0; j < rectangleIdsJson.size(); j++) {
                    logger.info(String.format("processing table %d of %d", j + 1, rectangleIdsJson.size()));
                    Object rectangleId = rectangleIdsJson.get(j);
                    Table table = extractFromRegion(
                            pageNumber,
                            page.getRegion(getRectangle(pageJson, rectangleId, pageSize))
                    );

                    List<Table> tableList = Collections.singletonList(table);

                    logger.info("saving table to html");
                    String html = new TableToHtmlWriter().write(tableList).get(0);

                    logger.info("saving table to excel");
                    XSSFWorkbook excelWorkbook = new TableToExcelWriter().write(tableList);

                    FileReference savedHtmlRef = fileSaveService.saveText(
                            html,
                            ref.getName() + "_page_" + pageNumber + "_table_" + rectangleId.toString() + ".html"
                    );
                    FileReference savedExcelRef = fileSaveService.saveExcel(
                            excelWorkbook,
                            ref.getName() + "_page_" + pageNumber + "_table_" + rectangleId.toString() + ".xlsx"
                    );
                    files.add(savedExcelRef);
                    files.add(savedHtmlRef);

                    JSONObject resultPageJson = new JSONObject();
                    resultPageJson.put("htmlId", savedHtmlRef.getId());
                    resultPageJson.put("excelId", savedExcelRef.getId());
                    resultPageJson.put("html", html);
                    resultPage.put(rectangleId, resultPageJson);
                }
            }
            if (files.size() > 0) {
                logger.info(String.format("packing %d files to archive", files.size()));
                FileReference archive = fileSaveService.archiveFiles(files, ref.getName() + "_results.zip");
                resultJson.put("resultId", archive.getId());
            }
            return resultJson;
        } finally {
            if (extractor != null) {
                extractor.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static Rectangle getRectangle(JSONObject pageJson, Object rectangleId,
                                          com.itextpdf.text.Rectangle pageSize) {
        JSONObject rectangleJson = (JSONObject) pageJson.get(rectangleId);
        float height = Math.abs(pageSize.getTop() - pageSize.getBottom());
        float width = Math.abs(pageSize.getLeft() - pageSize.getRight());

        float top = JsonUtils.getFloat(rectangleJson, "top") * height;
        float btm = JsonUtils.getFloat(rectangleJson, "bottom") * height;
        float left = JsonUtils.getFloat(rectangleJson, "left") * width;
        float right = JsonUtils.getFloat(rectangleJson, "right") * width;
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
