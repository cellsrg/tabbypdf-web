package ru.cells.icc.tabbypdf.web.services;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.icc.cells.tabbypdf.App;
import ru.icc.cells.tabbypdf.common.Page;
import ru.icc.cells.tabbypdf.common.TableBox;
import ru.icc.cells.tabbypdf.common.TextBlock;
import ru.icc.cells.tabbypdf.detectors.TableDetector;
import ru.icc.cells.tabbypdf.detectors.TableDetectorConfiguration;
import ru.icc.cells.tabbypdf.utils.content.PdfContentExtractor;
import ru.icc.cells.tabbypdf.utils.processing.TextChunkProcessor;

import java.io.IOException;
import java.util.List;

/**
 * Сервис для поиска таблиц в PDF-файле
 *
 * @author aaltaev
 * @since 0.1
 */
@Service
public class FindTablesServiceImpl implements FindTablesService {

    private static final Logger logger = Logger.getLogger(FindTablesServiceImpl.class);

    @Value("${file.save.path}")
    private String fileSavePath;

    @Override
    public JSONObject find(FileReference file) throws IOException {
        PdfContentExtractor extractor = null;
        try {
            logger.info("trying to open pdf");
            extractor = new PdfContentExtractor(fileSavePath + file.getName());

            JSONObject json = new JSONObject();
            JSONObject pagesJson = new JSONObject();
            json.put("pages", pagesJson);

            logger.info(String.format("processing %d pages", extractor.getNumberOfPages()));
            for (int i = 0; i < extractor.getNumberOfPages(); i++) {
                logger.info(String.format("processing page %d of %d", i + 1, extractor.getNumberOfPages()));
                Page page = extractor.getPageContent(i);
                List<TextBlock> tb = new TextChunkProcessor(page, App.getDetectionConfiguration()).process();
                TableDetectorConfiguration cnf = new TableDetectorConfiguration()
                        .setUseSortedTextBlocks(true)
                        .setMinRegionGapProjectionIntersection(1);
                TableDetector tableDetector = new TableDetector(cnf);
                List<TableBox> tableBoxes = tableDetector.detect(tb);
                logger.info(String.format("found %d tables", tableBoxes.size()));

                JSONObject pageJson = new JSONObject();
                pagesJson.put(i, pageJson);

                for (int j = 0; j < tableBoxes.size(); j++) {
                    TableBox tableBox = tableBoxes.get(j);

                    float height = Math.abs(page.getBottom() - page.getTop());
                    float width = Math.abs(page.getRight() - page.getLeft());

                    JSONObject tableBoxJson = new JSONObject();
                    tableBoxJson.put("left", tableBox.getLeft() / width);
                    tableBoxJson.put("right", tableBox.getRight() / width);
                    tableBoxJson.put("top", tableBox.getTop() / height);
                    tableBoxJson.put("bottom", tableBox.getBottom() / height);
                    pageJson.put(j, tableBoxJson);
                }
            }
            return json;
        } finally {
            if (extractor != null) {
                extractor.close();
            }
        }
    }
}
