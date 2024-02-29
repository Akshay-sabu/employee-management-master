package com.employee.management.converters;

import com.employee.management.DTO.CtcData;
import com.employee.management.DTO.EmployeeDTO;
import com.employee.management.DTO.OfferLetterDTO;
import com.employee.management.DTO.PaySlip;
import com.employee.management.models.HikeEntity;
import com.employee.management.util.CtcCalculator;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.type.PdfVersionEnum;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PDFService {
   private Mapper mapper;
   private CtcCalculator calculator;
   PDFService(Mapper mapper,CtcCalculator calculator){
       this.mapper=mapper;
       this.calculator=calculator;
   }

    public byte[] generatePaySlipPdf(PaySlip paySlip, String amountInWords) throws JRException {
        InputStream template = getClass().getResourceAsStream("/templates/pay-slip.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(template);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("payroll", paySlip.getPayrollDTO());
        parameters.put("employee", paySlip.getEmployeeDTO());
        parameters.put("amount", amountInWords);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
    public byte[] generateHikeLetter(EmployeeDTO employee, HikeEntity hike) throws JRException, IOException {

        JasperReport template1 =hike.getIsPromoted()? JasperCompileManager.
                compileReport(new ClassPathResource("templates/hikeLetterPages/hike-letter-with-promotion.jrxml").
                        getInputStream())
                :
                JasperCompileManager.
                        compileReport(new ClassPathResource("templates/hikeLetterPages/hike-letter.jrxml").
                                getInputStream())
                ;

        JasperReport template2 = JasperCompileManager.compileReport(new ClassPathResource("templates/hikeLetterPages/hike-letter-page-two.jrxml").getInputStream());

        System.err.println("compiled ");

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd");
        String formattedDate = currentDate.format(formatter);

        Map<String, Object> parameters1 = new HashMap<>();
        parameters1.put("employee", employee);
        parameters1.put("hikeDetails", mapper.convertToHikeEntityDto(hike));
        parameters1.put("hikeAmount", mapper.formatAmountWithCommas((hike.getNewSalary() - hike.getPrevSalary())));
        parameters1.put("currentDate", formattedDate);


        CtcCalculator calculator = new CtcCalculator();
        Map<String, Object> parameters2 = new HashMap<>();
        parameters2.put("employee", employee);
        parameters2.put("prevSalaryDetails", calculator.compensationDetails(hike.getPrevSalary()));
        parameters2.put("newSalaryDetails", calculator.compensationDetails(hike.getNewSalary()));

        JasperPrint jasperPrint1 = JasperFillManager.fillReport(template1, parameters1, new JREmptyDataSource());
        JasperPrint jasperPrint2 = JasperFillManager.fillReport(template2, parameters2, new JREmptyDataSource());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();

        List<JasperPrint> jasperPrints = new ArrayList<>();
        jasperPrints.add(jasperPrint1);
        jasperPrints.add(jasperPrint2);

        exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrints));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setPdfVersion(PdfVersionEnum.VERSION_1_7);
        configuration.setCreatingBatchModeBookmarks(true);
        configuration.setOverrideHints(true);
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        return outputStream.toByteArray();
    }

    public byte[] generateMergedOfferReport(OfferLetterDTO offerLetterDTO) throws IOException, JRException {
        CtcData data=calculator.compensationDetails(mapper.convertStringToDoubleAmount(offerLetterDTO.getCtc()));

        JasperReport report1 = JasperCompileManager.compileReport(new ClassPathResource("/templates/offerLetterPages/pageone.jrxml").getInputStream());
        JasperReport report2 = JasperCompileManager.compileReport(new ClassPathResource("/templates/offerLetterPages/pagetwo.jrxml").getInputStream());
        JasperReport report3 = JasperCompileManager.compileReport(new ClassPathResource("/templates/offerLetterPages/pagethree.jrxml").getInputStream());
        JasperReport report4 = JasperCompileManager.compileReport(new ClassPathResource("/templates/offerLetterPages/pagefour.jrxml").getInputStream());
        Map<String, Object> paramsForReport= new HashMap<>();
        paramsForReport.put("offer",offerLetterDTO);
        paramsForReport.put("ctc",data);


        JasperPrint jasperPrint1 = JasperFillManager.fillReport(report1, paramsForReport, new JREmptyDataSource());
        JasperPrint jasperPrint2 = JasperFillManager.fillReport(report2, null, new JREmptyDataSource());
        JasperPrint jasperPrint3 = JasperFillManager.fillReport(report3, null, new JREmptyDataSource());
        JasperPrint jasperPrint4 = JasperFillManager.fillReport(report4, paramsForReport, new JREmptyDataSource());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        List<JasperPrint> jasperPrints = new ArrayList<>();
        jasperPrints.add(jasperPrint1);
        jasperPrints.add(jasperPrint2);
        jasperPrints.add(jasperPrint3);
        jasperPrints.add(jasperPrint4);
        exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrints));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();

        return outputStream.toByteArray();
    }
    public ResponseEntity<byte[]> generatePdfPreviewResponse(byte[] pdfBytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline").build());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
