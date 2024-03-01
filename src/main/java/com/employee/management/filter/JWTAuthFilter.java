package com.employee.management.filter;

import com.employee.management.exception.CompanyException;
import com.employee.management.exception.ResCodes;
import com.employee.management.service.UserInfoService;
import com.employee.management.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JWTService jwtService;
    @Autowired
    UserInfoService userInfoService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader=request.getHeader("Authorization");
        String token=null;
        String empId=null;

        if(authorizationHeader !=null && authorizationHeader.startsWith("Bearer ")){
            token=authorizationHeader.substring(7);
            empId= jwtService.extractUsername(token);
        }

        if(empId !=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails = userInfoService.loadUserByUsername(empId);
            if(jwtService.validateToken(token,userDetails)){
                UsernamePasswordAuthenticationToken authToken=
                        new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }else{
                throw new CompanyException(ResCodes.INVALID_ID_AND_PASSWORD);
            }
        }
        filterChain.doFilter(request,response);
    }
}
