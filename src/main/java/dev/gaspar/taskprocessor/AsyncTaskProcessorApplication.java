package dev.gaspar.taskprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AsyncTaskProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsyncTaskProcessorApplication.class, args);
	}

}
