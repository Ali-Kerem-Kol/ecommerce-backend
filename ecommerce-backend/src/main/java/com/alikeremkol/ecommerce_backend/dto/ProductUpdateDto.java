package com.alikeremkol.ecommerce_backend.dto;

import com.alikeremkol.ecommerce_backend.model.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateDto {

    private Long id;

    private String name;
    private String brand;
    private BigDecimal price;
    private int inventory;
    private String description;


    private Category category;
}
