package com.pos.customer.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CustomerDto {

    private UUID id;
    private String name;
    private String phone;
    private String email;
    private Integer loyaltyPoints;
    private LocalDateTime createdAt;
}
