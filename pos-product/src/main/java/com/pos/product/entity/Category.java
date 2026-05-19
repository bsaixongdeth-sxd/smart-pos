package com.pos.product.entity;

import com.pos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "parent_id")
    private UUID parentId;
}
