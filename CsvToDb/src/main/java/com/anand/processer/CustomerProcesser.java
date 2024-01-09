package com.anand.processer;

import org.springframework.batch.item.ItemProcessor;

import com.anand.entity.Customer;

public class CustomerProcesser implements ItemProcessor<Customer, Customer> {

	@Override
	public Customer process(Customer item) throws Exception {
		return item;
	}

}
