package com.pos.auth.dto;

import com.pos.auth.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserDto {

    private UUID id;
    private String username;
    private String fullName;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
