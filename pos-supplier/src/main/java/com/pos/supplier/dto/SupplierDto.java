package com.pos.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SupplierDto {

    private UUID id;

    @NotBlank(message = "Name is required")
    private String name;

    private String contactName;
    private String phone;
    private String email;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
}
