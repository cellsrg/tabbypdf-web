package ru.cells.icc.tabbypdf.web.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations.PageJson;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.icc.cells.tabbypdf.App;
import ru.icc.cells.tabbypdf.detection.TableDetector;
import ru.icc.cells.tabbypdf.detection.TableDetectorConfiguration;
import ru.icc.cells.tabbypdf.entities.Page;
import ru.icc.cells.tabbypdf.entities.TableBox;
import ru.icc.cells.tabbypdf.entities.TextBlock;
import ru.icc.cells.tabbypdf.extraction.PdfDataExtractor;
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
    public TableLocations find(FileReference file) throws IOException {
        logger.info("trying to open pdf");
        PdfDataExtractor extractor = new PdfDataExtractor.Factory()
                .getPdfBoxTextExtractor(fileSavePath + file.getName());

        TableLocations locations = new TableLocations();

        List<Page> pageContent = extractor.getPageContent();
        logger.info(String.format("processing %d tables", pageContent.size()));
        for (int i = 0; i < pageContent.size(); i++) {
            logger.info(String.format("processing page %d of %d", i + 1, pageContent.size()));
            Page page = pageContent.get(i);
            List<TextBlock> tb = new TextChunkProcessor(page, App.getDetectionConfiguration()).process();
            TableDetectorConfiguration cnf = new TableDetectorConfiguration()
                    .setUseSortedTextBlocks(true)
                    .setMinRegionGapProjectionIntersection(1);
            TableDetector tableDetector = new TableDetector(cnf);
            List<TableBox> tableBoxes = tableDetector.detect(tb);
            logger.info(String.format("found %d tables", tableBoxes.size()));

            PageJson pageJson = locations.addPage(i);

            for (TableBox tableBox : tableBoxes) {
                double height = Math.abs(page.getBottom() - page.getTop());
                double width = Math.abs(page.getRight() - page.getLeft());

                pageJson.addTableLocation(
                        tableBox.getLeft() / width,
                        tableBox.getRight() / width,
                        tableBox.getTop() / height,
                        tableBox.getBottom() / height
                );
            }
        }
        return locations;
    }
}
