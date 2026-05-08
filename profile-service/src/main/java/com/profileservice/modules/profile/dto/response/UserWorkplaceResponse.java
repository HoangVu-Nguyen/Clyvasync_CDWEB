package com.profileservice.modules.profile.dto.response;

import com.commoncore.enums.privacy.Privacy;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkplaceResponse implements Serializable {
    private String  id;
    private String companyName;
    private String position;
    private String city;
    private Privacy privacy;
    private String description;
    private Boolean isCurrent;
}