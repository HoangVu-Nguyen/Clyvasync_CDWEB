package com.identityservice.security.password;

public interface PasswordService {
    boolean isStrongPassword(String password);
    String hashPassword(String password);
    boolean matches(String rawPassword, String encodedPassword);
}
