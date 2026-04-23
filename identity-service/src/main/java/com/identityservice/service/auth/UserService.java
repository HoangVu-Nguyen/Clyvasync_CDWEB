package com.identityservice.service.auth;

import com.identityservice.entity.auth.entity.UserCredential;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public interface UserService {
    Optional<UserCredential> findOptionalByEmail(String email);
}
