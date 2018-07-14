package ru.cells.icc.tabbypdf.web.controllers.responseentities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableLocations {
    private Long id;
    private Long resultId;
    private List<PageJson> pages = new ArrayList<>();

    public PageJson addPage(int pageNumber) {
        PageJson pageJson = new PageJson(pageNumber);
        pages.add(pageJson);
        return pageJson;
    }

    @Data
    @NoArgsConstructor
    public static class PageJson {

        private int pageNumber;
        private List<TableLocation> tables = new ArrayList<>();

        public PageJson(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public TableLocation addTableLocation(double left, double right, double top, double bottom) {
            TableLocation tableLocation = new TableLocation(left, right, top, bottom);
            tables.add(tableLocation);
            return tableLocation;
        }

        @Data
        @NoArgsConstructor
        public static class TableLocation {
            private double left;
            private double right;
            private double top;
            private double bottom;

            public TableLocation(double left, double right, double top, double bottom) {
                this.left = left;
                this.right = right;
                this.top = top;
                this.bottom = bottom;
            }

            private Long htmlId;
            private Long excelId;

            private String html;
        }
    }
}
