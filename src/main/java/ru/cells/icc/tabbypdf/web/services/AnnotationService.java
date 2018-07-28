package ru.cells.icc.tabbypdf.web.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations.PageJson;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations.PageJson.TableLocation;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;
import ru.icc.cells.tabbypdf.entities.Page;
import ru.icc.cells.tabbypdf.entities.TextChunk;
import ru.icc.cells.tabbypdf.extraction.PdfDataExtractor;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static ru.cells.icc.tabbypdf.web.utils.JsonUtils.getRectangle;

@Service
public class AnnotationService {

    private static final String[] TABLES_CSV_HEADERS = {"page", "table", "left", "bottom", "right", "top"};
    private static final String[] TEXT_CSV_HEADERS   = {"page", "table", "left", "bottom", "right", "top", "text"};

    @Autowired
    private FileSaveService fileSaveService;

    @Value("${file.save.path}")
    private String path;

    /**
     * Returns zip file with annotations identifier.
     * @param data table locations
     * @return zip file with annotations identifier
     */
    public FileReference getAnnotatedTableLocations(TableLocations data) {
        return annotate(data);
    }

    private FileReference annotate(TableLocations data) {
        try {
            StringWriter  tableWriter  = new StringWriter();
            StringWriter  textWriter   = new StringWriter();
            CSVPrinter    tablePrinter = CSVFormat.DEFAULT.withHeader(TABLES_CSV_HEADERS).print(tableWriter);
            CSVPrinter    textPrinter  = CSVFormat.DEFAULT.withHeader(TEXT_CSV_HEADERS).print(textWriter);
            FileReference pdfRef       = fileSaveService.findById(data.getId());

            print(data, tablePrinter, textPrinter, pdfRef);

            return fileSaveService.archiveFiles(
                Arrays.asList(
                    fileSaveService.saveText(close(tableWriter, tablePrinter), pdfRef.getName() + "_tables.csv"),
                    fileSaveService.saveText(close(textWriter, textPrinter), pdfRef.getName() + "_text_positions.csv")
                ),
                pdfRef.getName() + "_annotated.zip"
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void print(TableLocations data, CSVPrinter tablePrinter, CSVPrinter textPrinter, FileReference ref)
            throws IOException {
        PDDocument doc          = PDDocument.load(new File(path + ref.getName()));
        List<Page> pagesContent = new PdfDataExtractor
                .Factory()
                .getPdfBoxTextExtractor(path + ref.getName())
                .getPageContent();

        List<PageJson> pages = data.getPages();
        for (int pageNumber = 0; pageNumber < pages.size(); pageNumber++) {
            List<TableLocation> tables   = pages.get(pageNumber).getTables();
            Page                page     = pagesContent.get(pageNumber);
            PDRectangle         pageSize = doc.getPage(pageNumber).getMediaBox();

            for (int tableNumber = 0; tableNumber < tables.size(); tableNumber++) {
                TableLocation table = tables.get(tableNumber);
                printTable(tablePrinter, pageNumber, tableNumber, table);

                for (TextChunk textPosition : page.getRegion(getRectangle(table, pageSize)).getOriginChunks()) {
                    printTextPosition(textPrinter, pageNumber, tableNumber, textPosition);
                }
            }
        }
    }

    private void printTable(CSVPrinter printer, int pageNumber, int tableNumber, TableLocation table)
            throws IOException {
        printer.print(pageNumber);
        printer.print(tableNumber);
        printer.print(table.getLeft());
        printer.print(table.getBottom());
        printer.print(table.getRight());
        printer.print(table.getTop());
        printer.println();
    }

    private void printTextPosition(CSVPrinter printer, int pageNumber, int tableNumber, TextChunk textPosition)
            throws IOException {
        printer.print(pageNumber);
        printer.print(tableNumber);
        printer.print(textPosition.getLeft());
        printer.print(textPosition.getBottom());
        printer.print(textPosition.getRight());
        printer.print(textPosition.getTop());
        printer.print(textPosition.getText());
        printer.println();
    }

    private String close(StringWriter tableWriter, CSVPrinter tablePrinter) throws IOException {
        tablePrinter.flush();
        tablePrinter.close();
        tableWriter.flush();
        tableWriter.close();
        return tableWriter.toString();
    }
}
