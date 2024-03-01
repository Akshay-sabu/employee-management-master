package com.employee.management.controller;

import com.employee.management.DTO.CtcData;
import com.employee.management.DTO.OfferLetterDTO;
import com.employee.management.service.PDFService;
import com.employee.management.service.EmailSenderService;
import com.employee.management.service.OfferLetterService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/offer-letter")
public class OfferLetterController {
    @Autowired
    OfferLetterService offerLetterService;
    @Autowired
    EmailSenderService emailSenderService;
    @Autowired
    PDFService pdfService;
    @PostMapping("/send")
    public ResponseEntity<String> issueOfferLetter(@RequestBody OfferLetterDTO offerLetterDTO) throws JRException, IOException {
        OfferLetterDTO letterDTO=offerLetterService.issueNewOfferLetter(offerLetterDTO);
        try {
            byte[] pdfBytes = pdfService.generateMergedOfferReport(letterDTO);
            emailSenderService.sendEmailWithAttachment(letterDTO.getEmail(),"Offer and Appointment Letter ","Congratulations",pdfBytes);
            return new ResponseEntity<>("Email sent successfully",HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/preview-compensation-details")
    public ResponseEntity<CtcData> preview(@RequestBody OfferLetterDTO offerLetterDTO){
        return new ResponseEntity<>(offerLetterService.preview(offerLetterDTO.getCtc()),HttpStatus.OK);
    }

    @PostMapping("/preview-letter")
    public ResponseEntity<byte[]> previewLetter(@RequestBody OfferLetterDTO offerLetterDTO) throws JRException, IOException {
        byte[] pdfBytes = pdfService.generateMergedOfferReport(offerLetterDTO);
        return pdfService.generatePdfPreviewResponse(pdfBytes);
    }
    @GetMapping("/preview-letter-by-id/{id}")
    public ResponseEntity<byte[]> previewLetterById(@PathVariable("id") Long id) throws JRException, IOException {
        OfferLetterDTO offerLetterDTO=offerLetterService.get(id);
        byte[] pdfBytes = pdfService.generateMergedOfferReport(offerLetterDTO);
          return pdfService.generatePdfPreviewResponse(pdfBytes);
    }


}
