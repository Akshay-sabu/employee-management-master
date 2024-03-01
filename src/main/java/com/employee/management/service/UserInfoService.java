package com.employee.management.service;

import com.employee.management.DTO.EmployeeDetails;
import com.employee.management.exception.CompanyException;
import com.employee.management.exception.ResCodes;
import com.employee.management.models.Employee;
import com.employee.management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
public class UserInfoService implements UserDetailsService {
    @Autowired
    EmployeeRepository employeeRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Employee> employee = employeeRepository.findById(username);
      return  employee.map(EmployeeDetails::new)
                .orElseThrow(()->new CompanyException(ResCodes.EMPLOYEE_NOT_FOUND));
    }
}
