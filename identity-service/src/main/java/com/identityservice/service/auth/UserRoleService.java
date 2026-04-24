package com.identityservice.service.auth;

import com.identityservice.enums.auth.RoleName;

import java.util.Set;

public interface UserRoleService {
    Set<String> findRolesByUserId(String userId);
    void assignDefaultRole(String userId, RoleName roleName);

}
