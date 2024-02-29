package com.employee.management.service;

import com.employee.management.DTO.*;

import java.util.List;

public interface AdminService {
    EmployeeDTO addNewEmployee(EmployeeDTO employeeDTO);

    AdminDashBoardData loadData();

    List<HikeEntityDTO> hikeRecommendations();

    List<EmployeeDTO> fetchAllActiveEmployees();

    EmployeeDTO editEmployee(String empId,EmployeeDTO employeeDTO);

    String fetchEmployeeDesignation(String empId);

    String changeEmployeeStatus(String empId, String empStatus);

    PayrollDTO addPayroll(PayrollDTO payrollDTO,String empId);

    List<AvgSalaryGraphResponse> getSalaryGraphDataForPastSixMonths();
    String updatePfDetails(PfNumberUpdateRequest request);

    String updateHikeDetails(HikeUpdateRequest request);

    byte[] previewHikeDetails(HikeUpdateRequest request);
}
