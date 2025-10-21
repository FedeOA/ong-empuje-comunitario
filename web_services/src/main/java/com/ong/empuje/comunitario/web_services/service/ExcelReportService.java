package com.ong.empuje.comunitario.web_services.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ong.empuje.comunitario.web_services.model.Category;
import com.ong.empuje.comunitario.web_services.model.Donation;
import com.ong.empuje.comunitario.web_services.repository.CategoryRepository;
import com.ong.empuje.comunitario.web_services.repository.DonationRepository;

@Service
public class ExcelReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelReportService.class);
    
    private final DonationRepository donationRepository;
    private final CategoryRepository categoryRepository;

    public ExcelReportService(DonationRepository donationRepository, CategoryRepository categoryRepository){
        this.donationRepository = donationRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public byte[] generateDonationExcelReport(Integer categoryId, LocalDateTime startDate, LocalDateTime endDate, Boolean deleted){
        logger.info("Generando reporte Excel");

        try(Workbook workbook = new XSSFWorkbook()){
            List<Donation> donations = donationRepository.findByFilters(categoryId, startDate, endDate, deleted);
            Map<Integer, List<Donation>> donationsByCategory = donations.stream().collect(Collectors.groupingBy(Donation::getCategoryId));
            
            //hoja resumen
            createSumarySheet(workbook, donations, donationsByCategory);
            //hoja categoria
            for(Map.Entry<Integer, List<Donation>> entry : donationsByCategory.entrySet()){
                Integer catId = entry.getKey();
                List<Donation> categoryDonations = entry.getValue();
                createCategorySheet(workbook,catId,categoryDonations);
            }
            //hoja vacia
            if(donationsByCategory.isEmpty()){
                createEmptySheet(workbook);
            }

            return workbookToByArray(workbook);

        }catch(Exception e){
            logger.error("Error generando reporte Excel: ", e.getMessage(), e);
            throw new RuntimeException("Error generando reporte Excel: "+ e.getMessage(),e);
        }
    }

    //hoja resumen
    private void createSumarySheet(Workbook workbook, List<Donation> allDonations, Map<Integer, List<Donation>> donationsByCategory){
        Sheet sheet = workbook.createSheet("Resumen General");
        CellStyle headerStyle = createHeaerStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle boldStyle = createBoldStryle(workbook);
        int i;
        
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("INFORME DE DONACIONES - EMPUJE COMUNITARIO");
        titleCell.setCellStyle(boldStyle);

        //filtros
        int rowNum = 2;
        Row filtersRow = sheet.createRow(rowNum++);
        filtersRow.createCell(0).setCellValue("Filtros aplicados");
        filtersRow.getCell(0).setCellStyle(boldStyle);

        //Resumen por categoria
        rowNum++;
        Row summaryHeader = sheet.createRow(rowNum++);
        String[] summaryHeaders = {"Categoria","Cantidad Total","Registros","Activos","Eliminados"};
        for(i = 0; i<summaryHeaders.length; i++){
            Cell cell = summaryHeader.createCell(i);
            cell.setCellValue(summaryHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        //datos
        for(Map.Entry<Integer, List<Donation>> entry : donationsByCategory.entrySet()){
            Integer categoryId = entry.getKey();
            List<Donation> categoryDonations = entry.getValue();
            Category category = categoryRepository.findById(categoryId).orElse(null);
            String categoryName = category != null ? category.getName() :"Desconocida";

            long activeCount = categoryDonations.stream().filter(d -> !d.getDeleted()).count();
            long deletedCount = categoryDonations.stream().filter(Donation::getDeleted).count();
            int totalQuantity = categoryDonations.stream().filter(d -> d.getQuantity() != null).mapToInt(Donation::getQuantity).sum();

            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(categoryName);
            dataRow.createCell(1).setCellValue(totalQuantity);
            dataRow.createCell(2).setCellValue(categoryDonations.size());
            dataRow.createCell(3).setCellValue(activeCount);
            dataRow.createCell(4).setCellValue(deletedCount);

            for(i = 0; i < 5; i++){
                dataRow.getCell(i).setCellStyle(dataStyle);
            }

        }

        //Totales
        if(!donationsByCategory.isEmpty()){
            rowNum++;
            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("TOTAL GENERAL");
            totalRow.getCell(0).setCellStyle(boldStyle);

            int totalQuantity = allDonations.stream().filter(d -> d.getQuantity() != null).mapToInt(Donation::getQuantity).sum();
            long totalActive = allDonations.stream().filter(d -> !d.getDeleted()).count();
            long totalDeleted = allDonations.stream().filter(Donation::getDeleted).count();

            totalRow.createCell(1).setCellValue(totalQuantity);
            totalRow.createCell(2).setCellValue(allDonations.size());
            totalRow.createCell(3).setCellValue(totalActive);
            totalRow.createCell(4).setCellValue(totalDeleted);

            for(i = 1; i<5; i++){
                totalRow.getCell(i).setCellStyle(boldStyle);
            }
        }
        for(i = 0; i < 5; i++){
            sheet.autoSizeColumn(i);
        }
    }


    //hoja por categoria
    private void createCategorySheet(Workbook workbook, Integer categoryId, List<Donation> donations){
        Category category = categoryRepository.findById(categoryId).orElse(null);
        String categoryName = category != null ? category.getName() : "Desconocida";
        String sheetName = sanitizeSheetName(categoryName);
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaerStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Fecha Alta", "Descripción", "Cantidad", "Estado", "Usuario Alta",
            "Usuario Modificación", "Fecha Modificación"
        };

        int i;
        for(i = 0; i < headers.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        //Datos
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int rowNum = 1;
        for(Donation donation : donations){
            Row row = sheet.createRow(rowNum++);

            if(donation.getCreatedAt() != null){
                row.createCell(0).setCellValue(donation.getCreatedAt().format(dateFormatter));
            }
            row.createCell(1).setCellValue(donation.getDescription());
            if(donation.getQuantity() != null){
                row.createCell(2).setCellValue(donation.getQuantity());
            }
            row.createCell(3).setCellValue(donation.getDeleted());
            if(donation.getUpdatedAt() != null){
                row.createCell(6).setCellValue(donation.getUpdatedAt().format(dateFormatter));
            }

            for(i = 0; i < 7; i++){
                if(row.getCell(i) != null){
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }
        for(i = 0;i < 7; i++){
            sheet.autoSizeColumn(i);
        }
    }
    
    
    //hoja vacia
    private void createEmptySheet(Workbook workbook){
        Sheet sheet = workbook.createSheet("Sin datos");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("No se encontraron donaciones con los filtros aplicados");
        sheet.autoSizeColumn(0);
    }


    //sin errores al crear hoja
    private String sanitizeSheetName(String name){
        if(name == null) return "Desconocida";
        String sanitized = name.replaceAll("[\\\\/*\\[\\]:?]", "_");

        if(sanitized.length() > 31){
            sanitized = sanitized.substring(0,31);
        }
        
        return sanitized.isEmpty() ? "Categoria" : sanitized;
    }


    //estilos
    private CellStyle createHeaerStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createBoldStryle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    //Workbook a byte array
    private byte[] workbookToByArray(Workbook workbook)throws IOException{
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
