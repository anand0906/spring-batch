package com.anand.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.anand.entity.Customer;
import com.anand.processer.CustomerProcesser;
import com.anand.repository.CustomerRepository;

@Configuration
public class CsvToDbConfig {

	
	@Autowired
	JobRepository jobRepository;

	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	PlatformTransactionManager transactionManager;

	public FlatFileItemReader<Customer> reader() {
		FlatFileItemReader<Customer> fileItemReader = new FlatFileItemReader<>();
		fileItemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
		fileItemReader.setLinesToSkip(1);
		fileItemReader.setName("CustomerCsvFile");
		fileItemReader.setLineMapper(lineMapper());
		return fileItemReader;
	}

	private LineMapper<Customer> lineMapper() {
		DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

		BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setStrict(false);
		fieldSetMapper.setTargetType(Customer.class);

		mapper.setLineTokenizer(lineTokenizer);
		mapper.setFieldSetMapper(fieldSetMapper);
		return mapper;
	}

	@Bean
	public CustomerProcesser processer() {
		return new CustomerProcesser();
	}

	@Bean
	public RepositoryItemWriter<Customer> writer() {
		RepositoryItemWriter<Customer> itemWriter = new RepositoryItemWriter<>();
		itemWriter.setRepository(customerRepository);
		itemWriter.setMethodName("save");
		return itemWriter;
	}

	@Bean
	public Step step1() {

		return new StepBuilder("convert-db",jobRepository).<Customer, Customer>chunk(10,transactionManager).reader(reader())
				.processor(processer()).writer(writer()).build();

	}

	@Bean
	public Job job() {

		return new JobBuilder("CustomerCsvToDb",jobRepository).flow(step1()).end().build();

	}
}
