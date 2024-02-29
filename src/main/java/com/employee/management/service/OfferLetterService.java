package com.employee.management.service;

import com.employee.management.DTO.CtcData;
import com.employee.management.DTO.OfferLetterDTO;
import net.sf.jasperreports.engine.JRException;

import java.io.IOException;

public interface OfferLetterService {
    OfferLetterDTO issueNewOfferLetter(OfferLetterDTO offerLetterDTO);
    CtcData preview(String grossSalary);

    OfferLetterDTO get(Long id);
}
