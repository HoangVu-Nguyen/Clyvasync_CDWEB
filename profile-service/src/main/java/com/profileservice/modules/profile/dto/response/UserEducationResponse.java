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
@NoArgsConstructor  // BẮT BUỘC: Để MyBatis tạo object trống trước khi map
@AllArgsConstructor // Cần thiết để @Builder hoạt động
public class UserEducationResponse implements Serializable {
    private String id;
    private String schoolName;
    private String degree;
    private String major;
    private String type;
    private Privacy privacy;
}