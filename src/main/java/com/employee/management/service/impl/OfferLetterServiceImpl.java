package com.employee.management.service.impl;

import com.employee.management.DTO.CtcData;
import com.employee.management.DTO.OfferLetterDTO;
import com.employee.management.converters.Mapper;
import com.employee.management.models.OfferLetterEntity;
import com.employee.management.repository.OfferLetterRepository;
import com.employee.management.service.OfferLetterService;
import com.employee.management.util.CtcCalculator;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OfferLetterServiceImpl implements OfferLetterService {
    @Autowired
    OfferLetterRepository offerLetterRepository;
    @Autowired
    Mapper mapper;
    @Autowired
    CtcCalculator calculator;

    @Override
    public OfferLetterDTO issueNewOfferLetter(OfferLetterDTO offerLetterDTO){
        OfferLetterEntity offerLetter=mapper.convertToOfferLetterEntity(offerLetterDTO);
        OfferLetterEntity savedOfferLetter = offerLetterRepository.save(offerLetter);
        return mapper.convertToOfferLetterDto(savedOfferLetter);
    }

    @Override
    public CtcData preview(String grossSalary){
        return calculator.compensationDetails(mapper.convertStringToDoubleAmount(grossSalary));
    }

    @Override
    public OfferLetterDTO get(Long id) {
        return mapper.convertToOfferLetterDto(offerLetterRepository.findById(id).get());
    }
}
