package com.employee.management.DTO;

import lombok.Data;

@Data
public class HikeUpdateRequest {
    private String employeeId;
    private String percentage;
    private String newPosition;
    private String reason;
    private String approvedBy;
    private String effectiveDate;
}
