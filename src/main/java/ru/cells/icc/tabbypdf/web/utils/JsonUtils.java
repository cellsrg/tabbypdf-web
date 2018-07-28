package ru.cells.icc.tabbypdf.web.utils;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.json.simple.JSONObject;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations;
import ru.cells.icc.tabbypdf.web.controllers.responseentities.TableLocations.PageJson.TableLocation;
import ru.icc.cells.tabbypdf.entities.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON-утилиты
 *
 * @author aaltaev
 * @since 0.1
 */
public class JsonUtils {

    /**
     * Returns rectangle with converted tableLocation coordinates to PDF's page size.
     * @param tableLocation contains table location coordinates
     * @param pageSize      PDF page size
     * @return rectangle with converted tableLocation coordinates to PDF's page size.
     */
    public static Rectangle getRectangle(TableLocation tableLocation, PDRectangle pageSize) {
        float height = pageSize.getHeight();
        float width = pageSize.getWidth();

        double top   = tableLocation.getTop() * height;
        double btm   = tableLocation.getBottom() * height;
        double left  = tableLocation.getLeft() * width;
        double right = tableLocation.getRight() * width;

        return new Rectangle(left, btm, right, top);
    }
}
