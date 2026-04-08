package com.payoda.smartlock.utils;

import java.util.regex.Pattern;

public class Validator {

    private static final String emailPattern = "[A-Z0-9].[A-Z0-9_.]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";


    private static final String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@#!%*?&.,()])[A-Za-z\\d$@!%#.,*?&()]{8,16}";

    public static boolean IsEmail(String data) {
        return Pattern.compile(emailPattern, Pattern.CASE_INSENSITIVE).matcher(data).matches();
    }

    public static boolean IsPassword(String data) {
        return Pattern.compile(passwordPattern, Pattern.CASE_INSENSITIVE).matcher(data).matches();
    }

}
