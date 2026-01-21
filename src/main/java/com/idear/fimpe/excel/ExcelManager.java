package com.idear.fimpe.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelManager {

    private CellStyle headerStyle, contentStyle, titleStyle;
    private int indexRow;
    private Workbook workbook;
    private Sheet sheet;

    public ExcelManager(Workbook workbook){
        this.workbook = workbook;
        setTitleCellStyle();
        setCellStyleForHeader();
        setCellStyleForContent();
    }

    private void setTitleCellStyle(){
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle = workbook.createCellStyle();
        titleStyle.setFont(font);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
    }

    /**
     * Crea el estilo para los encabezados de las tablas
     */
    private void setCellStyleForHeader(){
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK1.getIndex());
        headerStyle = workbook.createCellStyle();
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle = addCellBorders(headerStyle);

    }

    private void setCellStyleForContent(){
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());
        contentStyle = workbook.createCellStyle();
        contentStyle.setFont(font);
        contentStyle = addCellBorders(contentStyle);
    }



    private CellStyle addCellBorders(CellStyle cellStyle){
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());

        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

        return cellStyle;
    }

    /**
     * Agrega el titulo a la tabla y fuciona las celdas
     * @param title Titulo de tabla
     * @param mergeLength Numero de celdas a fucionar
     */
    public void addTableTitle(String title, int mergeLength){
        Row row = sheet.createRow(indexRow);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(indexRow, indexRow, 0, mergeLength - 1));
    }

    /**
     * Agrega ek titulo a la tabla
     * @param title Titulo de tabla
     */
    public void addTableTitle(String title){
        Row row = sheet.createRow(indexRow);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(titleStyle);
        indexRow++;
    }

    /**
     * Crea un subtitulo para un grupo de columnas, fusiona varias celdas
     * @param row Fila en al cual estará el subtitulo
     * @param beginCell Celda en la que empieza el subtitulo
     * @param endCell Celda en la que finaliza el subtitulo
     * @param value Valor o contenido del subtitulo
     */
    public void createSubTitle(Row row, int beginCell, int endCell, String value) {
        for (int i = beginCell; i <= endCell; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(headerStyle);
            if (i == beginCell) {
                cell.setCellValue(value);
            }
        }
        sheet.addMergedRegion(new CellRangeAddress(indexRow, indexRow, beginCell, endCell));
    }

    /**
     * Obtiene una celda
     * @param row Fila de la cual saldrá la celda
     * @param column Posicion sobre la fila que ocupará la celda
     * @param cellStyle Estilo que tendrá la celda
     * @return Una celda
     */
    public Cell getCell(Row row, int column, CellStyle cellStyle){
        Cell cell = row.createCell(column);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    public int getIndexRow(){
        indexRow += 1;
        return indexRow;
    }

    public CellStyle getHeaderStyle() {
        return headerStyle;
    }

    public CellStyle getContentStyle() {
        return contentStyle;
    }

    public CellStyle getTitleStyle() {
        return titleStyle;
    }

    /**
     * Establece el tamaño automatico para las columnas de la hoja
     * @param columns Numero de columnas a las cuales se le aplicará el ancho automatico
     */
    public void autoSize(int columns){
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void setSheet(Sheet sheet){
        this.sheet = sheet;
    }
}
