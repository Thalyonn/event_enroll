package com.standingcat.event.scripts;

import com.standingcat.event.EventApplication;
import com.standingcat.event.model.User;
import com.standingcat.event.service.UserService;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;

//Note: this is a hybrid script. Either password is passed as 3rd arg or passed in env file.
//env file is likely to be safer as cli args can be visible in monitors or shell history
//IMPORTANT: This doesn't work properly yet. Since at this moment the database is running H2 in-memory
public class CreateAdmin {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java CreateAdmin <username> <email> [password]");
            return;
        }

        String username = args[0];
        String email = args[1];

        String password;
        if (args.length >= 3) {
            password = args[2];
        } else {
            password = System.getenv("ADMIN_PASSWORD");
        }
        //password is passed as the 3rd argument or set ADMIN_PASSWORD env var.
        //do note though: Env vars are probably going to be safer
        if (password == null || password.isEmpty()) {
            System.err.println("password not provided.");
            return;
        }

        SpringApplication app = new SpringApplication(EventApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        ApplicationContext context = app.run(args);
        //ApplicationContext context = SpringApplication.run(EventApplication.class);

        UserService userService = context.getBean(UserService.class);

        if (userService.findByUsername(username).isEmpty()) {
            User admin = new User();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPassword(password);
            userService.createAdminUser(admin);
            System.out.println("Admin user created: " + username + " (" + email + ")");
        } else {
            System.out.println("User with username '" + username + "' already exists.");
        }

        ((ConfigurableApplicationContext) context).close();
    }
}