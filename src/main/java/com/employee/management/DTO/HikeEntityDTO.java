package com.employee.management.DTO;

import lombok.Data;

@Data
public class HikeEntityDTO {
    private Long id;
    private String employeeId;
    private String prevSalary;
    private String newSalary;
    private String hikePercentage;
    private String reason;
    private String approvedBy;
    private String approvedDate;
    private String prevPosition;
    private String newPosition;
    private Boolean status;
    private String effectiveDate;
}
