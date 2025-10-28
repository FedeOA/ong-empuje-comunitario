package com.ong.empuje.comunitario.web_services.controller;

import java.time.LocalDateTime;
import static java.time.format.DateTimeFormatter.ofPattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ong.empuje.comunitario.web_services.dto.in.DonationExcelRequestDTO;
import com.ong.empuje.comunitario.web_services.service.ExcelReportService;


@RestController
@RequestMapping("/api/reports")
public class ExcelReportController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelReportController.class);
    private final ExcelReportService excelReportService;

    public ExcelReportController(ExcelReportService excelReportService){
        this.excelReportService = excelReportService;
    }

    @PostMapping(value = "/donations/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateDonationExcelReport(@RequestBody DonationExcelRequestDTO request) {
        
        logger.info("Solicitando reporte Excel de donaciones: ");

        try {
            byte[] excelBytes = excelReportService.generateDonationExcelReport(
                request.getCategoryId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDeleted()
            );

            String filename = String.format("reporte_donaciones");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);

            logger.info("Reporte Excel generado existosamente");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generando reporte Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
    }
    
}
