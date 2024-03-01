package com.employee.management.util;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class Util {
    public int getNumberOfDaysInMonth(String period) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            YearMonth yearMonth = YearMonth.parse(period, formatter);
            return yearMonth.lengthOfMonth();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }
}
