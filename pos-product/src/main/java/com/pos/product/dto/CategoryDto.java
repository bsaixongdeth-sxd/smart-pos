package com.pos.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryDto {

    private UUID id;
    private String name;
    private UUID parentId;
}
