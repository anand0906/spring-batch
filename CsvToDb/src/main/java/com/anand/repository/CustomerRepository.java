package com.anand.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anand.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
