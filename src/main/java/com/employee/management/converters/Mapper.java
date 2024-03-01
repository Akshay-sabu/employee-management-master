package com.employee.management.converters;

import com.employee.management.DTO.*;
import com.employee.management.exception.CompanyException;
import com.employee.management.exception.ResCodes;
import com.employee.management.models.*;
import com.employee.management.util.Formatters;
import com.employee.management.util.PasswordGenerator;
import com.employee.management.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

@Component
public class Mapper {
    @Autowired
    PasswordGenerator passwordGenerator;
    @Autowired
    DateTimeConverter dateConverter;
    @Autowired
    Formatters formatter;
    @Autowired
    Util util;
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
            employeeDTO.setGrossSalary(formatter.formatAmountWithCommas(employee.getGrossSalary()));
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
        dto.setBasic(formatter.formatAmountWithCommas(payroll.getBasic()));
        dto.setHouseRentAllowance(formatter.formatAmountWithCommas(payroll.getHouseRentAllowance()));
        dto.setMedicalAllowance(formatter.formatAmountWithCommas(payroll.getMedicalAllowance()));
        dto.setOtherAllowance(formatter.formatAmountWithCommas(payroll.getOtherAllowance()));
        dto.setGrossEarnings(formatter.formatAmountWithCommas(payroll.getGrossEarnings()));
        dto.setProvidentFund(formatter.formatAmountWithCommas(payroll.getProvidentFund()));
        dto.setTotalDeductions(formatter.formatAmountWithCommas(payroll.getTotalDeductions()));
        dto.setTotalNetPayable(formatter.formatAmountWithCommas((double) Math.round(payroll.getTotalNetPayable())));
        dto.setProfessionalTax(formatter.formatAmountWithCommas(payroll.getProfessionalTax()));
        dto.setTotalDaysPaid(payroll.getTotalPaidDays());
        dto.setIncomeTax(formatter.formatAmountWithCommas(payroll.getIncomeTax()));
        dto.setTotalLopDays(payroll.getTotalLopDays());
        dto.setLeaveDeduction(formatter.formatAmountWithCommas(payroll.getLeaveDeduction()));
        return dto;
    }

    public Payroll convertToPayroll(PayrollDTO payrollDTO){
        Payroll payroll=new Payroll();
        if(payrollDTO !=null) {
            payroll.setPayDate(dateConverter.stringToLocalDateTimeConverter(payrollDTO.getPayDate()));
            payroll.setPayPeriod(payrollDTO.getPayPeriod());
            payroll.setBasic(formatter.convertStringToDoubleAmount(payrollDTO.getBasic()));
            payroll.setHouseRentAllowance(formatter.convertStringToDoubleAmount(payrollDTO.getHouseRentAllowance()));
            payroll.setMedicalAllowance(formatter.convertStringToDoubleAmount(payrollDTO.getMedicalAllowance()));
            payroll.setOtherAllowance(formatter.convertStringToDoubleAmount(payrollDTO.getOtherAllowance()));
            payroll.setGrossEarnings(formatter.convertStringToDoubleAmount(payrollDTO.getGrossEarnings()));
            payroll.setLeaveDeduction(formatter.convertStringToDoubleAmount(payrollDTO.getLeaveDeduction()));
            payroll.setProfessionalTax(formatter.convertStringToDoubleAmount(payrollDTO.getProfessionalTax()));
            payroll.setProvidentFund(formatter.convertStringToDoubleAmount(payrollDTO.getProvidentFund()));
            payroll.setTotalDeductions(formatter.convertStringToDoubleAmount(payrollDTO.getTotalDeductions()));
            payroll.setTotalNetPayable(formatter.convertStringToDoubleAmount(payrollDTO.getTotalNetPayable()));
            payroll.setTotalPaidDays(payrollDTO.getTotalDaysPaid());
            payroll.setTotalLopDays(payrollDTO.getTotalLopDays());
            payroll.setIncomeTax(formatter.convertStringToDoubleAmount(payrollDTO.getIncomeTax()));
            return payroll;
        }
        return payroll;
    }
    public Payroll mapCtcDataToPayroll(CtcData ctcData,Payroll payroll){
        if(ctcData!=null){
            payroll.setBasic(formatter.convertStringToDoubleAmount(ctcData.getMonthlyBasic()));
            payroll.setHouseRentAllowance(formatter.convertStringToDoubleAmount(ctcData.getMonthlyHRA()));
            payroll.setMedicalAllowance(formatter.convertStringToDoubleAmount(ctcData.getMonthlyMedAllowance()));
            payroll.setOtherAllowance(formatter.convertStringToDoubleAmount(ctcData.getMonthlyOtherAllowance()));
            payroll.setGrossEarnings(formatter.convertStringToDoubleAmount(ctcData.getMonthlyGrossCtc()));

            payroll.setIncomeTax(formatter.convertStringToDoubleAmount(ctcData.getMonthlyIncomeTax()));
            payroll.setProfessionalTax(formatter.convertStringToDoubleAmount(ctcData.getMonthlyProfessionalTax()));
            payroll.setProvidentFund(formatter.convertStringToDoubleAmount(ctcData.getMonthlyProvidentFund()));
            double regularNetPayable= payroll.getGrossEarnings()-(payroll.getIncomeTax()+payroll.getProfessionalTax()+payroll.getProvidentFund());
            long leaveDeductions=Math.round (regularNetPayable/30)*payroll.getTotalLopDays();
            payroll.setLeaveDeduction((double) leaveDeductions);
            payroll.setTotalDeductions(formatter.convertStringToDoubleAmount(ctcData.getMonthlyTotalDeduction())+payroll.getLeaveDeduction());
            payroll.setTotalNetPayable(formatter.convertStringToDoubleAmount(ctcData.getMonthlyGrossCtc())-payroll.getTotalDeductions());
            payroll.setTotalPaidDays(util.getNumberOfDaysInMonth(payroll.getPayPeriod())-payroll.getTotalLopDays());
            return payroll;
        }
        return null;
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

        employee.setGrossSalary(formatter.convertStringToDoubleAmount(employeeDTO.getGrossSalary()));
        employee.setPfNumber(employeeDTO.getPfNumber());
        employee.setUanNumber(employeeDTO.getUanNumber());
        return employee;
    }

    public OfferLetterEntity convertToOfferLetterEntity(OfferLetterDTO offerLetterDTO){
        OfferLetterEntity offerLetter=new OfferLetterEntity();
        if(offerLetterDTO!=null){
            offerLetter.setCtc(formatter.convertStringToDoubleAmount(offerLetterDTO.getCtc()));
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
        hikeEntityDTO.setNewSalary(formatter.formatAmountWithCommas(hike.getNewSalary()));
        hikeEntityDTO.setPrevSalary(formatter.formatAmountWithCommas(hike.getPrevSalary()));
        hikeEntityDTO.setStatus(hike.getIsApproved());
        hikeEntityDTO.setNewPosition(hike.getNewPosition());
        hikeEntityDTO.setPrevPosition(hike.getPrevPosition());
        if(hike.getApprovedBy() !=null)
            hikeEntityDTO.setApprovedBy(hike.getApprovedBy().getEmployeeID());
        return hikeEntityDTO;
    }

    private boolean validateEmployeeDto(EmployeeDTO employeeDTO) {
        return Stream.of(employeeDTO.getEmployeeName(), employeeDTO.getDesignation(),
                        employeeDTO.getLocation(), employeeDTO.getBankName(),
                        employeeDTO.getAccountNo())
                .allMatch(field -> field != null && !field.isEmpty());
    }
}
