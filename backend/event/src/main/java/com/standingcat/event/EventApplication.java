package com.standingcat.event;

import com.standingcat.event.model.User;
import com.standingcat.event.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EventApplication {

	@Value("${admin.password}")
	private String adminPassword;

	public static void main(String[] args) {

		SpringApplication.run(EventApplication.class, args);
		System.out.println("Hello World");
	}

	//this bean is to create an initial admin account. Configure the password in the env.
	@Bean
	CommandLineRunner initAdmin(UserService userService) {
		return args -> {
			if (userService.findByUsername("firstAdmin").isEmpty()) {
				User admin = new User();
				admin.setUsername("firstAdmin");
				admin.setEmail("firstadmin@example.com");
				admin.setPassword(adminPassword);

				userService.createAdminUser(admin);
				System.out.println("Admin user created!");
			} else {
				System.out.println("Admin already exists, skipping creation.");
			}
		};
	}

}
