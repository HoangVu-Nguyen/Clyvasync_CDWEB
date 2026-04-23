package com.identityservice.service.auth;

import java.util.Set;

public interface UserRoleService {
    Set<String> findRolesByUserId(String userId);
}
