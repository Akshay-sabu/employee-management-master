package com.employee.management.util;

import org.springframework.stereotype.Component;

@Component
public class EmailBodyBuilder {
    public String getBodyForAccountCreationMail(String name, String empId, String password) {
        return "Hi " + name + ",\n\n" +
                "Welcome Seabed2Crest Technologies Pvt Ltd" + "\n" +
                "Here are your login details:" + "\n" +
                "Employee ID: " + empId + "\n" +
                "Password: " + password + "\n\n" +
                "Please keep this information confidential." + "\n\n" +
                "If you have any questions, feel free to contact us." + "\n\n" +
                "Best regards,\nThe HR Team";
    }
}
