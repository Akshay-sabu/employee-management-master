package com.employee.management.controller;

import com.employee.management.DTO.*;
import com.employee.management.converters.AmountToWordsConverter;
import com.employee.management.service.PDFService;
import com.employee.management.service.AdminService;
import com.employee.management.service.EmployeeService;
import com.employee.management.service.PayRollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    AdminService adminService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    PDFService pdfService;
    @Autowired
    PayRollService payRollService;

    @Autowired
    private AmountToWordsConverter amountToWordsConverter;

    private final SseEmitter emitter=new SseEmitter();

    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> addEmployee(@RequestBody EmployeeDTO employeeDTO){
        return new ResponseEntity<>(adminService.addNewEmployee(employeeDTO), HttpStatus.CREATED);
    }
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdminDashBoardData>loadData(){
        return new ResponseEntity<>(adminService.loadData(),HttpStatus.OK);
    }
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<EmployeeDTO>>fetchAllEmployee(){
        return new ResponseEntity<>(adminService.fetchAllActiveEmployees(),HttpStatus.OK);
    }
    @PutMapping("/edit/{empId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO> editEmployee(@PathVariable("empId") String empId,@RequestBody EmployeeDTO employeeDTO){
        return new ResponseEntity<>(adminService.editEmployee(empId,employeeDTO),HttpStatus.OK);
    }
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmployeeDTO>getEmployee(@RequestParam("empId")String empId){
        return new ResponseEntity<>(employeeService.getEmployee(empId),HttpStatus.FOUND);
    }
    @PutMapping("/change-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String>changeStatusOfEmployee(@RequestParam("empId")String empId,
                                                        @RequestParam("status")String status){
        return new ResponseEntity<>(adminService.changeEmployeeStatus(empId,status),HttpStatus.OK);
    }


    @GetMapping("/get-designation/{id}")
    public ResponseEntity<String >getEmployeeDesignation(@PathVariable("id")String empId){
        return new ResponseEntity<>(adminService.fetchEmployeeDesignation(empId),HttpStatus.OK);
    }

    @GetMapping("/hike-recommendations")
    public ResponseEntity<List<HikeEntityDTO>>fetchHikeRecommendations(){
        return new ResponseEntity<>(adminService.hikeRecommendations(),HttpStatus.OK);
    }
    @PostMapping("/add-payroll")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PayrollDTO> addNewPayRoll(@RequestBody PayrollDTO payrollDTO) {
            PayrollDTO payroll = adminService.addPayroll(payrollDTO, payrollDTO.getEmployeeId());
        System.out.println(payroll);
            return ResponseEntity.status(HttpStatus.CREATED).body(payroll);

    }
    @PostMapping("/add-new-payroll")
    public ResponseEntity<PayrollDTO>addNewPayrollWithMinimalData(@RequestBody AddMonthlyPayRollRequest request){
        return new ResponseEntity<>(adminService.addMonthlyPayRoll(request),HttpStatus.OK);
    }
    @GetMapping("/salary-graph")
    public ResponseEntity<List<AvgSalaryGraphResponse>> fetchSixMonthData(){
        return new ResponseEntity<>(adminService.getSalaryGraphDataForPastSixMonths(),HttpStatus.OK);
    }
    @PostMapping("/update-pf-no")
    public ResponseEntity<String> updatePFDetails(@RequestBody PfNumberUpdateRequest request){
        System.out.println(request);
        return new ResponseEntity<>(adminService.updatePfDetails(request),HttpStatus.OK);
    }
    @PostMapping("/update-hike")
    public ResponseEntity<String>updateHike(@RequestBody HikeUpdateRequest request){
        return new ResponseEntity<>(adminService.updateHikeDetails(request),HttpStatus.OK);
    }
    @PostMapping("/preview-hike")
    public ResponseEntity<byte[]>reviewHike(@RequestBody HikeUpdateRequest request){
        byte[] pdfBytes = adminService.previewHikeDetails(request);
        return pdfService.generatePdfPreviewResponse(pdfBytes);
    }
}
