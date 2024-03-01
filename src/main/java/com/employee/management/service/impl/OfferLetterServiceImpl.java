package com.employee.management.service.impl;

import com.employee.management.DTO.CtcData;
import com.employee.management.DTO.OfferLetterDTO;
import com.employee.management.converters.Mapper;
import com.employee.management.models.OfferLetterEntity;
import com.employee.management.repository.OfferLetterRepository;
import com.employee.management.service.OfferLetterService;
import com.employee.management.util.CtcCalculator;
import com.employee.management.util.Formatters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class OfferLetterServiceImpl implements OfferLetterService {
    @Autowired
    OfferLetterRepository offerLetterRepository;
    @Autowired
    Mapper mapper;
    @Autowired
    CtcCalculator calculator;
    @Autowired
    Formatters formatters;

    @Override
    public OfferLetterDTO issueNewOfferLetter(OfferLetterDTO offerLetterDTO){
        OfferLetterEntity offerLetter=mapper.convertToOfferLetterEntity(offerLetterDTO);
        OfferLetterEntity savedOfferLetter = offerLetterRepository.save(offerLetter);
        return mapper.convertToOfferLetterDto(savedOfferLetter);
    }

    @Override
    public CtcData preview(String grossSalary){
        return calculator.compensationDetails(formatters.convertStringToDoubleAmount(grossSalary));
    }

    @Override
    public OfferLetterDTO get(Long id) {
        return mapper.convertToOfferLetterDto(offerLetterRepository.findById(id).get());
    }
}
