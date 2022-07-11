package org.nwolfhub.userbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class UserbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserbotApplication.class, args);
		UpdateListener.initialize();
	}

}
