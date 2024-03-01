package com.employee.management.service;

import com.employee.management.DTO.CtcData;
import com.employee.management.DTO.OfferLetterDTO;

public interface OfferLetterService {
    OfferLetterDTO issueNewOfferLetter(OfferLetterDTO offerLetterDTO);
    CtcData preview(String grossSalary);
    OfferLetterDTO get(Long id);
}
