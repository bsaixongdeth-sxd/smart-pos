package com.pos.customer.service;

import com.pos.common.dto.PageResponse;
import com.pos.common.exception.BusinessException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.customer.dto.CreateCustomerRequest;
import com.pos.customer.dto.CustomerDto;
import com.pos.customer.entity.Customer;
import com.pos.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public PageResponse<CustomerDto> search(String query, Pageable pageable) {
        return new PageResponse<>(customerRepository.search(query, pageable).map(this::toDto));
    }

    public CustomerDto findById(UUID id) {
        return toDto(customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id)));
    }

    @Transactional
    public CustomerDto create(CreateCustomerRequest request) {
        if (request.getPhone() != null &&
                customerRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("Phone number already registered");
        }
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        return toDto(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDto update(UUID id, CreateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        if (request.getName() != null) customer.setName(request.getName());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        return toDto(customerRepository.save(customer));
    }

    @Transactional
    public void addLoyaltyPoints(UUID id, int points) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customerRepository.save(customer);
    }

    public Customer getCustomerEntity(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private CustomerDto toDto(Customer c) {
        CustomerDto dto = new CustomerDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setPhone(c.getPhone());
        dto.setEmail(c.getEmail());
        dto.setLoyaltyPoints(c.getLoyaltyPoints());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
