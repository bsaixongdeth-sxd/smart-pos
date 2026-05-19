package com.pos.supplier.service;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.supplier.dto.SupplierDto;
import com.pos.supplier.entity.Supplier;
import com.pos.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<SupplierDto> findAll() {
        return supplierRepository.findByActiveTrue().stream().map(this::toDto).toList();
    }

    public SupplierDto findById(UUID id) {
        return toDto(supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id)));
    }

    @Transactional
    public SupplierDto create(SupplierDto request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setContactName(request.getContactName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        return toDto(supplierRepository.save(supplier));
    }

    private SupplierDto toDto(Supplier s) {
        SupplierDto dto = new SupplierDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setContactName(s.getContactName());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());
        dto.setActive(s.getActive());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
