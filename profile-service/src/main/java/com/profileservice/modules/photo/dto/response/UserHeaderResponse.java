package com.profileservice.modules.photo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHeaderResponse implements Serializable {
    private String id;
    private String username;
    private String photoUrl;

}