package com.employee.management.converters;

import com.employee.management.DTO.EmployeeDTO;
import com.employee.management.DTO.HikeEntityDTO;
import com.employee.management.DTO.OfferLetterDTO;
import com.employee.management.DTO.PayrollDTO;
import com.employee.management.exception.CompanyException;
import com.employee.management.exception.ResCodes;
import com.employee.management.models.*;
import com.employee.management.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

@Component
public class Mapper {
    @Autowired
    PasswordGenerator passwordGenerator;
    @Autowired
    DateTimeConverter dateConverter;
    public EmployeeDTO convertToEmployeeDTO(Employee employee){
        EmployeeDTO employeeDTO=new EmployeeDTO();
        if(employee!=null){
            employeeDTO.setRoles(employee.getRoles()
                    .stream()
                    .map(Role::getName)
                    .toList()
            );
            employeeDTO.setEmployeeID(employee.getEmployeeID());
            employeeDTO.setEmployeeName(employee.getEmployeeName());
            employeeDTO.setDesignation(employee.getDesignation());
            employeeDTO.setEmail(employee.getEmail());
            employeeDTO.setLocation(employee.getLocation());
            employeeDTO.setBankName(employee.getBankName());
            employeeDTO.setAccountNo(employee.getAccountNo());
            employeeDTO.setStatus(employee.getStatus().getName());
            employeeDTO.setPfNumber(employee.getPfNumber());
            employeeDTO.setDepartment(employee.getDepartment());
            employeeDTO.setUanNumber(employee.getUanNumber());
            employeeDTO.setDateOfJoin(dateConverter.localDateTimeToStringConverter(employee.getDateOfJoin()));
            employeeDTO.setGrossSalary(formatAmountWithCommas(employee.getGrossSalary()));
            employeeDTO.setNextHikeDate(dateConverter.localDateTimeToStringConverter(employee.getNextHikeDate()));
        }
        return employeeDTO;
    }
    public PayrollDTO convertToPayRollDTO(Payroll payroll) {
        PayrollDTO dto = new PayrollDTO();
        dto.setId(payroll.getId());
        dto.setPayPeriod(payroll.getPayPeriod());
        dto.setPayDate(dateConverter.localDateTimeToStringConverter(payroll.getPayDate()));
        dto.setEmployeeId(payroll.getEmployee().getEmployeeID());
        dto.setBasic(formatAmountWithCommas(payroll.getBasic()));
        dto.setHouseRentAllowance(formatAmountWithCommas(payroll.getHouseRentAllowance()));
        dto.setMedicalAllowance(formatAmountWithCommas(payroll.getMedicalAllowance()));
        dto.setOtherAllowance(formatAmountWithCommas(payroll.getOtherAllowance()));
        dto.setGrossEarnings(formatAmountWithCommas(payroll.getGrossEarnings()));
        dto.setProvidentFund(formatAmountWithCommas(payroll.getProvidentFund()));
        dto.setTotalDeductions(formatAmountWithCommas(payroll.getTotalDeductions()));
        dto.setTotalNetPayable(formatAmountWithCommas((double) Math.round(payroll.getTotalNetPayable())));
        dto.setProfessionalTax(formatAmountWithCommas(payroll.getProfessionalTax()));
        dto.setTotalDaysPaid(payroll.getTotalPaidDays());
        dto.setIncomeTax(formatAmountWithCommas(payroll.getIncomeTax()));
        dto.setTotalLopDays(payroll.getTotalLopDays());
        dto.setLeaveDeduction(formatAmountWithCommas(payroll.getLeaveDeduction()));
        return dto;
    }

    public Payroll convertToPayroll(PayrollDTO payrollDTO){
        Payroll payroll=new Payroll();
        if(payrollDTO !=null) {
            payroll.setPayDate(dateConverter.stringToLocalDateTimeConverter(payrollDTO.getPayDate()));
            payroll.setPayPeriod(payrollDTO.getPayPeriod());
            payroll.setBasic(convertStringToDoubleAmount(payrollDTO.getBasic()));
            payroll.setHouseRentAllowance(convertStringToDoubleAmount(payrollDTO.getHouseRentAllowance()));
            payroll.setMedicalAllowance(convertStringToDoubleAmount(payrollDTO.getMedicalAllowance()));
            payroll.setOtherAllowance(convertStringToDoubleAmount(payrollDTO.getOtherAllowance()));
            payroll.setGrossEarnings(convertStringToDoubleAmount(payrollDTO.getGrossEarnings()));
            payroll.setLeaveDeduction(convertStringToDoubleAmount(payrollDTO.getLeaveDeduction()));
            payroll.setProfessionalTax(convertStringToDoubleAmount(payrollDTO.getProfessionalTax()));
            payroll.setProvidentFund(convertStringToDoubleAmount(payrollDTO.getProvidentFund()));
            payroll.setTotalDeductions(convertStringToDoubleAmount(payrollDTO.getTotalDeductions()));
            payroll.setTotalNetPayable(convertStringToDoubleAmount(payrollDTO.getTotalNetPayable()));
            payroll.setTotalPaidDays(payrollDTO.getTotalDaysPaid());
            payroll.setTotalLopDays(payrollDTO.getTotalLopDays());
            payroll.setIncomeTax(convertStringToDoubleAmount(payrollDTO.getIncomeTax()));
            return payroll;
        }
        return payroll;
    }
    public Employee convertToEmployeeEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        if(!validateEmployeeDto(employeeDTO)){
            throw new CompanyException(ResCodes.INVALID_EMPLOYEE_DETAILS);
        }
        employee.setEmployeeName(employeeDTO.getEmployeeName());
        employee.setDesignation(employeeDTO.getDesignation());
        employee.setLocation(employeeDTO.getLocation());
        employee.setBankName(employeeDTO.getBankName());
        employee.setAccountNo(employeeDTO.getAccountNo());
        employee.setDepartment(employeeDTO.getDepartment());
        employee.setPassword(passwordGenerator.generatePassword(6));
        employee.setEmail(employeeDTO.getEmail());
        employee.setDateOfJoin(dateConverter.stringToLocalDateTimeConverter(employeeDTO.getDateOfJoin()));

        Date nextHikeDate = new Date(employee.getDateOfJoin().getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(nextHikeDate);
        cal.add(Calendar.YEAR, 1);
        nextHikeDate = cal.getTime();
        employee.setNextHikeDate(nextHikeDate);

        employee.setGrossSalary(convertStringToDoubleAmount(employeeDTO.getGrossSalary()));
        employee.setPfNumber(employeeDTO.getPfNumber());
        employee.setUanNumber(employeeDTO.getUanNumber());
        return employee;
    }

    public OfferLetterEntity convertToOfferLetterEntity(OfferLetterDTO offerLetterDTO){
        OfferLetterEntity offerLetter=new OfferLetterEntity();
        if(offerLetterDTO!=null){
            offerLetter.setCtc(convertStringToDoubleAmount(offerLetterDTO.getCtc()));
            offerLetter.setFullName(offerLetterDTO.getFullName());
            offerLetter.setEmail(offerLetterDTO.getEmail());
            offerLetter.setJoiningDate(dateConverter
                    .stringToLocalDateTimeConverter(offerLetterDTO.getJoiningDate()));
            offerLetter.setIssuedDate(new Date());
            offerLetter.setPhoneNumber(offerLetterDTO.getPhoneNumber());
            offerLetter.setDesignation(offerLetterDTO.getDesignation());
            offerLetter.setDepartment(offerLetterDTO.getDepartment());
            return offerLetter;
        }
        throw new RuntimeException("OfferLetter DTO is null");
    }

    public OfferLetterDTO convertToOfferLetterDto(OfferLetterEntity entity){
        OfferLetterDTO offerLetterDTO=new OfferLetterDTO();
        offerLetterDTO.setCtc(String.valueOf(entity.getCtc()));
        offerLetterDTO.setIssuedDate(dateConverter.localDateTimeToStringConverter(entity.getIssuedDate()));
        offerLetterDTO.setFullName(entity.getFullName());
        offerLetterDTO.setEmail(entity.getEmail());
        offerLetterDTO.setDesignation(entity.getDesignation());
        offerLetterDTO.setJoiningDate(dateConverter.localDateTimeToStringConverter(entity.getJoiningDate()));
        offerLetterDTO.setPhoneNumber(entity.getPhoneNumber());
        offerLetterDTO.setDepartment(entity.getDepartment());
        return offerLetterDTO;
    }

    public HikeEntityDTO convertToHikeEntityDto(HikeEntity hike){
        HikeEntityDTO hikeEntityDTO=new HikeEntityDTO();
        hikeEntityDTO.setId(hike.getId());
        hikeEntityDTO.setHikePercentage(String.valueOf(hike.getHikePercentage()));
        hikeEntityDTO.setEffectiveDate(dateConverter.localDateTimeToStringConverter(hike.getEffectiveDate()));
        hikeEntityDTO.setEmployeeId(hike.getEmployee().getEmployeeID());
        hikeEntityDTO.setReason(hike.getReason());
        hikeEntityDTO.setApprovedDate(dateConverter.localDateTimeToStringConverter(hike.getApprovedDate()));
        hikeEntityDTO.setNewSalary(formatAmountWithCommas(hike.getNewSalary()));
        hikeEntityDTO.setPrevSalary(formatAmountWithCommas(hike.getPrevSalary()));
        hikeEntityDTO.setStatus(hike.getIsApproved());
        hikeEntityDTO.setNewPosition(hike.getNewPosition());
        hikeEntityDTO.setPrevPosition(hike.getPrevPosition());
        if(hike.getApprovedBy() !=null)
            hikeEntityDTO.setApprovedBy(hike.getApprovedBy().getEmployeeID());
        return hikeEntityDTO;
    }

    private boolean validateEmployeeDto(EmployeeDTO employeeDTO) {
        return Arrays.asList(employeeDTO.getEmployeeName(), employeeDTO.getDesignation(),
                        employeeDTO.getLocation(), employeeDTO.getBankName(),
                        employeeDTO.getAccountNo())
                .stream()
                .allMatch(field -> field != null && !field.isEmpty());
    }
    public Double convertStringToDoubleAmount(String amount){
        amount=amount.replace(",","");
       return Double.parseDouble(amount);
    }

    public String formatAmountWithCommas(Double amount) {
        if (amount == null) {
            return "";
        }
        if(amount==0){
            return "0";
        }
        String numb = String.valueOf(amount);
        String numberStr;
        String split = null;
        if (numb.contains(".")) {
            String[] num = numb.split("\\.");
            numberStr = num[0];
            split = num[1];
        } else numberStr = numb;
        StringBuilder result = getStringBuilder(numberStr);
        if(split!=null)
            result.append(".").append(split).append("0");
        return result.toString();
    }
    private StringBuilder getStringBuilder(String numberStr) {
        StringBuilder result = new StringBuilder();

        int len = numberStr.length();
        int count = 0;
        for (int i = len - 1; i >= 0; i--) {
            result.insert(0, numberStr.charAt(i));
            count++;
            if (count == 3 && i != 0) {
                result.insert(0, ",");
            }
            if (count == 5 && i != 0) {
                result.insert(0, ",");
            }
            if (count == 7 && i != 0) {
                result.insert(0, ",");
            }
        }
        return result;
    }

}
