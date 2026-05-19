package com.pos.product.service;

import com.pos.common.dto.PageResponse;
import com.pos.common.exception.BusinessException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.CreateProductRequest;
import com.pos.product.dto.ProductDto;
import com.pos.product.dto.UpdateProductRequest;
import com.pos.product.entity.Product;
import com.pos.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public PageResponse<ProductDto> search(String query, Pageable pageable) {
        return new PageResponse<>(productRepository.search(query, pageable).map(this::toDto));
    }

    public ProductDto findById(UUID id) {
        return toDto(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }

    public ProductDto findByBarcode(String barcode) {
        return toDto(productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product with barcode: " + barcode)));
    }

    @Transactional
    public ProductDto create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU already exists: " + request.getSku());
        }
        Product product = new Product();
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setName(request.getName());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        if (request.getName() != null) product.setName(request.getName());
        if (request.getBarcode() != null) product.setBarcode(request.getBarcode());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getActive() != null) product.setActive(request.getActive());
        return toDto(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
    }

    public ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setSku(p.getSku());
        dto.setBarcode(p.getBarcode());
        dto.setName(p.getName());
        dto.setCategoryId(p.getCategoryId());
        dto.setPrice(p.getPrice());
        dto.setCostPrice(p.getCostPrice());
        dto.setActive(p.getActive());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    public Product getProductEntity(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
