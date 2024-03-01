package com.employee.management.controller;
import com.employee.management.DTO.AuthRequest;
import com.employee.management.DTO.ChangePasswordRequest;
import com.employee.management.DTO.ForgetPasswordRequest;
import com.employee.management.exception.CompanyException;
import com.employee.management.exception.ResCodes;
import com.employee.management.service.EmployeeService;
import com.employee.management.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class Auth {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    JWTService jwtService;
    @Autowired
    EmployeeService employeeService;
    @PostMapping("/login")
    public String login(@RequestBody AuthRequest authRequest){
        try {
            Authentication authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmpId(), authRequest.getPassword()));
            if (authenticate.isAuthenticated())
                return jwtService.generateToken(authRequest.getEmpId());
            else throw new CompanyException(ResCodes.INVALID_ID_AND_PASSWORD);
        }catch (AuthenticationException e){
            throw new CompanyException(ResCodes.INVALID_ID_AND_PASSWORD);
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request){
        return new ResponseEntity<>(employeeService.changePassword(request), HttpStatus.OK);
    }
    @GetMapping("/reset-mail/{empId}")
    public ResponseEntity<String>sendResetMail(@PathVariable String empId){
        return new ResponseEntity<>(employeeService.resetPasswordMail(empId),HttpStatus.OK);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String>sendResetMail(@RequestBody ForgetPasswordRequest passwordRequest){
        return new ResponseEntity<>(employeeService.forgetPassword(passwordRequest),HttpStatus.OK);
    }
}
