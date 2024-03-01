package com.employee.management.service.impl;

import com.employee.management.DTO.*;
import com.employee.management.converters.DateTimeConverter;
import com.employee.management.converters.Mapper;
import com.employee.management.service.PDFService;
import com.employee.management.exception.CompanyException;
import com.employee.management.exception.ResCodes;
import com.employee.management.models.*;
import com.employee.management.repository.*;
import com.employee.management.service.AdminService;
import com.employee.management.service.EmailSenderService;
import com.employee.management.util.CtcCalculator;
import com.employee.management.util.EmailBodyBuilder;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PayrollRepository payrollRepository;
    private final StatusRepository statusRepository;
    private final HikeRepository hikeRepository;
    private final DateTimeConverter dateTimeConverter;
    private final Mapper mapper;
    private final EmailSenderService emailSenderService;
    private final PasswordEncoder passwordEncoder;
    private final PDFService pdfService;
    private final EmailBodyBuilder emailBodyBuilder;
    private final CtcCalculator calculator;

    @Autowired
    public AdminServiceImpl(EmployeeRepository employeeRepository, RoleRepository roleRepository,
                       PayrollRepository payrollRepository, StatusRepository statusRepository,
                       HikeRepository hikeRepository, DateTimeConverter dateTimeConverter,
                       Mapper mapper, EmailSenderService emailSenderService,
                       PasswordEncoder passwordEncoder, PDFService pdfService,
                            EmailBodyBuilder emailBodyBuilder,CtcCalculator calculator) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.payrollRepository = payrollRepository;
        this.statusRepository = statusRepository;
        this.hikeRepository = hikeRepository;
        this.dateTimeConverter = dateTimeConverter;
        this.mapper = mapper;
        this.emailSenderService = emailSenderService;
        this.passwordEncoder = passwordEncoder;
        this.pdfService = pdfService;
        this.emailBodyBuilder = emailBodyBuilder;
        this.calculator=calculator;
    }

    private String getTodayDateFormatted(){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return today.format(formatter);
    }
    private int getEmployeeCount(){
        List<Employee>employees=employeeRepository.findAll();
        return employees.size();
    }
    @Override
    public EmployeeDTO addNewEmployee(EmployeeDTO employeeDTO){
        Employee employee=mapper.convertToEmployeeEntity(employeeDTO);
        Role role=roleRepository.findById(2L).get();
        String password=employee.getPassword();
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.getRoles().add(role);
        employee.setStatus(statusRepository.findById(1L).get());
        Employee savedEmployee = employeeRepository.save(employee);
        emailSenderService.sendSimpleEmail(employee.getEmail(),"Account Created",emailBodyBuilder.getBodyForAccountCreationMail(savedEmployee.getEmployeeName(),savedEmployee.getEmployeeID(),password));

        return mapper.convertToEmployeeDTO(savedEmployee);
    }

    @Override
    public AdminDashBoardData loadData(){
        AdminDashBoardData adminDashBoardData=new AdminDashBoardData();

        YearMonth currentYearMonth = YearMonth.now();
        YearMonth previousYearMonth = currentYearMonth.minusMonths(1);
        String previousMonthFormatted = previousYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + previousYearMonth.getYear();

        List<Payroll>payrolls=payrollRepository.getPayDetails(previousMonthFormatted)
                .orElseThrow(()->new CompanyException(ResCodes.SALARY_DETAILS_NOT_FOUND));

        Double averageSalary=payrolls.stream()
                .mapToDouble(Payroll::getGrossEarnings)
                .average()
                .orElse(0.0);

        adminDashBoardData.setAverageSalary(averageSalary);
        adminDashBoardData.setTodayDate(getTodayDateFormatted());
        adminDashBoardData.setNoOfEmployees(getEmployeeCount());
        return adminDashBoardData;
    }

    @Override
    public List<HikeEntityDTO> hikeRecommendations(){
        List<HikeEntity> hikeRec=hikeRepository.findAllByStatusFalse();
       return  hikeRec.stream()
               .filter(Objects::nonNull)
               .map(mapper::convertToHikeEntityDto)
               .toList();
    }

    @Override
    public List<EmployeeDTO> fetchAllActiveEmployees(){
        List<Employee>employees=employeeRepository.findAll();
        return  employees.stream()
                .filter(Objects::nonNull)
                .filter(employee -> employee.getStatus().getName().equals("active"))
                .map(mapper::convertToEmployeeDTO)
                .toList();
    }

    @Override
    public EmployeeDTO editEmployee(String empId,EmployeeDTO employeeDTO){
        Employee employee =employeeRepository.findById(empId)
                .orElseThrow(()-> new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        employee.setDesignation(employeeDTO.getDesignation());
        employee.setLocation(employeeDTO.getLocation());
        employee.setBankName(employeeDTO.getBankName());
        employee.setAccountNo(employeeDTO.getAccountNo());
        employee.setEmployeeName(employeeDTO.getEmployeeName());
        Employee savedEmployee = employeeRepository.save(employee);
        return mapper.convertToEmployeeDTO(savedEmployee);
    }
    @Override
    public String fetchEmployeeDesignation(String empId){
        Employee employee =employeeRepository.findById(empId)
                .orElseThrow(()-> new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        return employee.getDesignation();
    }

    @Override
    public String changeEmployeeStatus(String empId, String empStatus){

        Employee employee=employeeRepository.findById(empId)
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        Status status=statusRepository.findByName(empStatus.toLowerCase())
                .orElseThrow(()->new CompanyException(ResCodes.INVALID_STATUS));
        employee.setStatus(status);
        employeeRepository.save(employee);
        return "Employee status changed successfully";
    }

    @Override
    public PayrollDTO addPayroll(PayrollDTO payrollDTO,String empId){
      Employee employee=employeeRepository.findById(empId)
              .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
      Payroll a=payrollRepository.getPayPeriodDetails(payrollDTO.getPayPeriod(),employee).orElse(null);
      if(a==null) {
          Payroll payroll = mapper.convertToPayroll(payrollDTO);
          payroll.setEmployee(employee);
          Payroll savedPayroll = payrollRepository.save(payroll);
          return mapper.convertToPayRollDTO(savedPayroll);
      }else{
          throw new CompanyException(ResCodes.DUPLICATE_PAYROLL_DETAILS);
      }
    }
    @Override
    public PayrollDTO addMonthlyPayRoll(AddMonthlyPayRollRequest request){
        Employee employee=employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        Payroll payrollExistForThisMonth=payrollRepository
                .getPayPeriodDetails(request.getPayPeriod(),employee).orElse(null);
        if(payrollExistForThisMonth!=null)
            throw new CompanyException(ResCodes.DUPLICATE_PAYROLL_DETAILS);
       CtcData ctcData= calculator.compensationDetails(employee.getGrossSalary());
        Payroll payroll=new Payroll();
        payroll.setEmployee(employee);

        payroll.setTotalLopDays(request.getLopDays() != null?Integer.parseInt(request.getLopDays()):0);
        payroll.setPayDate(dateTimeConverter.stringToLocalDateTimeConverter(request.getPayDate()));
        payroll.setPayPeriod(request.getPayPeriod());
        Payroll savedPayRoll = payrollRepository.save(mapper.mapCtcDataToPayroll(ctcData, payroll));
        return mapper.convertToPayRollDTO(savedPayRoll);
    }
    @Override
    public List<AvgSalaryGraphResponse> getSalaryGraphDataForPastSixMonths(){
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate sixMonthsAgo = currentDate.minusMonths(6);
        String sixMonthAgo = sixMonthsAgo.format(formatter);

        List<Payroll> sixMonthData=payrollRepository.findByPayPeriodRange(sixMonthAgo);
        Map<String, Double> averageSalaryByPayPeriod = sixMonthData.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Payroll::getPayPeriod,
                        Collectors.averagingDouble(Payroll::getTotalNetPayable)));
        return  averageSalaryByPayPeriod.entrySet().stream()
                .map(entry -> new AvgSalaryGraphResponse(entry.getKey(), String.format("%.2f", entry.getValue())))
                .toList();

    }

    @Override
    public String updatePfDetails(PfNumberUpdateRequest request) {
        Employee employee=employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        if(request.getUanNumber()!=null && request.getPfNumber()!=null) {
            employee.setUanNumber(request.getUanNumber());
            employee.setPfNumber(request.getPfNumber());
            employeeRepository.save(employee);
            return "Successfully Updated";
        }
        throw new CompanyException(ResCodes.EMPTY_FIELDS);
    }
    @Override
    public String updateHikeDetails(HikeUpdateRequest request){
        Employee employee=employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        Employee approvedBy=employeeRepository.findById(request.getApprovedBy())
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        HikeEntity hike=hikeRepository.findByStatusAndEmployee(false,employee)

                .orElseThrow(()->new CompanyException(ResCodes.HIKE_DATA_NOT_FOUND));

        if(!hike.getIsApproved()) {
            hike.setIsApproved(true);
            hike.setIsPromoted(request.getNewPosition() != null && !request.getNewPosition().equals("None"));

            hike.setHikePercentage(Double.valueOf(request.getPercentage()));
            hike.setApprovedBy(approvedBy);
            hike.setNewSalary((hike.getPrevSalary() * (hike.getHikePercentage() / 100)) + hike.getPrevSalary());
            hike.setApprovedDate(new Date());
            hike.setEffectiveDate(dateTimeConverter.stringToLocalDateTimeConverter(request.getEffectiveDate()));
            hike.setReason(request.getReason());
            hike.setNewPosition(hike.getIsPromoted()?request.getNewPosition():null);
            HikeEntity savedHike = hikeRepository.save(hike);
            try{
                sendHikeLetterMail(pdfService.generateHikeLetter(mapper.convertToEmployeeDTO(employee),savedHike),employee.getEmail());
            }catch (Exception e){
                System.out.println(e);
            }
            return "Mail sent Successfully";
        }
        throw new CompanyException(ResCodes.HIKE_APPROVED_ALREADY);
    }

    @Override
    public byte[] previewHikeDetails(HikeUpdateRequest request){
        Employee employee=employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        Employee approvedBy=employeeRepository.findById(request.getApprovedBy())
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
        HikeEntity hike=hikeRepository.findByStatusAndEmployee(false,employee)
                .orElseThrow(()->new CompanyException(ResCodes.HIKE_APPROVED_ALREADY));
        HikeEntity previewHike=new HikeEntity();
        previewHike.setEmployee(hike.getEmployee());

        previewHike.setIsApproved(hike.getIsApproved());
        previewHike.setPrevSalary(hike.getPrevSalary());
        previewHike.setPrevPosition(hike.getPrevPosition());
        if(!previewHike.getIsApproved()) {
            previewHike.setIsApproved(true);
            previewHike.setIsPromoted(request.getNewPosition() != null && !request.getNewPosition().equals("None"));

            previewHike.setHikePercentage(Double.valueOf(request.getPercentage()));
            previewHike.setApprovedBy(approvedBy);
            previewHike.setNewSalary((hike.getPrevSalary() * (previewHike.getHikePercentage() / 100)) + hike.getPrevSalary());
            previewHike.setApprovedDate(new Date());
            previewHike.setEffectiveDate(dateTimeConverter.stringToLocalDateTimeConverter(request.getEffectiveDate()));
            previewHike.setReason(request.getReason());

            previewHike.setNewPosition(hike.getIsPromoted()?request.getNewPosition():null);

            try{
               return pdfService.generateHikeLetter(mapper.convertToEmployeeDTO(employee),previewHike);
            }catch (Exception e){
                System.out.println(e);
            }
        }
        throw new CompanyException(ResCodes.HIKE_APPROVED_ALREADY);
    }


    private void sendHikeLetterMail(byte [] pdf,String to) throws MessagingException, IOException {
        emailSenderService.sendEmailWithAttachment(to,"Salary Hike Updation ","Update",pdf);
    }

}
