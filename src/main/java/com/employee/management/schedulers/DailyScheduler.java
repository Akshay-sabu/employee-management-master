package com.employee.management.schedulers;

import com.employee.management.exception.CompanyException;
import com.employee.management.exception.ResCodes;
import com.employee.management.models.Employee;
import com.employee.management.models.HikeEntity;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.HikeRepository;
import com.employee.management.service.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.time.ZoneId;
import java.util.Objects;

@Component

public class DailyScheduler {
    private final EmployeeRepository employeeRepository;
    private final HikeRepository hikeRepository;

    public DailyScheduler(EmployeeRepository employeeRepository, HikeRepository hikeEntityRepository) {
        this.employeeRepository = employeeRepository;
        this.hikeRepository = hikeEntityRepository;
    }
    @Autowired
    EmailSenderService emailSenderService;


//    @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(fixedRate = 60000)
    public void hikeTableUpdater(){
        List<Employee> employees=employeeRepository.findByStatusName("active");
        LocalDate today = LocalDate.now();
        employees.stream()
                .filter(this::isOneWeekBeforeHikeDate)
                .forEach(employee -> {
                    HikeEntity hikeEntity=new HikeEntity();
                    hikeEntity.setEmployee(employee);
                    hikeEntity.setPrevSalary(employee.getGrossSalary());
                    hikeEntity.setPrevPosition(employee.getDesignation());
                    hikeEntity.setIsApproved(false);
                    hikeEntity.setIsPromoted(false);
                    updateHikeDateAndSave(employee);
                    hikeRepository.save(hikeEntity);
                    updateAdminAboutHikeViaMail(hikeEntity,employee);
                });
    }
    @Scheduled(fixedRate = 60000)
    public void updateEmployeeGrossSalary(){
        LocalDate localDate = LocalDate.now();
        Date effectiveDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<HikeEntity> hikes=hikeRepository.findByEffectiveDate(effectiveDate);
        hikes.stream()
                .filter(Objects::nonNull)
                .forEach(hike -> {
                    Employee employee=employeeRepository.findById(hike.getEmployee().getEmployeeID())
                            .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
                    employee.setDesignation(hike.getNewPosition());
                    employee.setGrossSalary(hike.getNewSalary());
                    employeeRepository.save(employee);
                });
    }

    private boolean isOneWeekBeforeHikeDate(Employee employee) {
        Date nextHikeDate = employee.getNextHikeDate();
        if (nextHikeDate == null) {
            return false;
        }
        LocalDate hikeDate = nextHikeDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate oneWeekBeforeHikeDate = hikeDate.minusWeeks(1);

        return LocalDate.now().equals(oneWeekBeforeHikeDate);
    }

    private void updateHikeDateAndSave(Employee employee) {
        Date nextHikeDate = new Date(employee.getNextHikeDate().getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(nextHikeDate);
        cal.add(Calendar.YEAR, 1);
        nextHikeDate = cal.getTime();
        employee.setNextHikeDate(nextHikeDate);
        employeeRepository.save(employee);
    }
    private void updateAdminAboutHikeViaMail(HikeEntity hike,Employee employee){
        List<Employee> admins=employeeRepository.findAdminEmployees();
        admins.forEach(
                admin->{
                    emailSenderService.sendSimpleEmail(admin.getEmail(),
                            "Employee Eligible For Hike",
                            bodyForMail(employee,admin)
                            );
                }
        );
    }
    private String bodyForMail(Employee employee,Employee admin){
        return "Dear "+admin.getEmployeeName()+",\n" +
                "\n" +
                "This is an automated notification to inform you that "+employee.getEmployeeName()+" , with Employee ID "+employee.getEmployeeID()+", is eligible for a hike based on their performance and tenure with the company.\n" +
                "\n" +
                "Employee Details:\n" +
                "\n" +
                "Employee Name: "+employee.getEmployeeName()+" \n" +
                "Employee ID: "+employee.getEmployeeID()+" \n" +
                "Designation: "+employee.getDesignation()+" \n" +
                "Location: "+employee.getLocation()+" \n" +
                "Date of Joining: "+employee.getDateOfJoin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() +" \n" +
                "Please review the employee's performance and make the necessary arrangements for the hike as per the company's policies.";
    }



}
