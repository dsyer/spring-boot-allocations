package com.example.demo;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

public class ConfigFileAllocations {

	public static void main(String[] args) throws IOException {
		ConfigFileApplicationListener listener = new ConfigFileApplicationListener();
		listener.setSearchLocations("file:./src/main/resources/application.properties");
		SpringApplication application = new SpringApplication(TestConfiguration.class);
		ConfigurableEnvironment environment = new StandardEnvironment();
		while (true) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < 100; i++) {
				ApplicationEnvironmentPreparedEvent envPrep = new ApplicationEnvironmentPreparedEvent(application, new String[0], environment );
				listener.onApplicationEvent(envPrep);
				GenericApplicationContext context = new GenericApplicationContext();
				context.setEnvironment(environment);
				ApplicationPreparedEvent appPrep = new ApplicationPreparedEvent(application, new String[0], context);
				listener.onApplicationEvent(appPrep);
			}
			long end = System.currentTimeMillis();
			System.out.println("Duration: " + (end - start));
		}
	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public MyBean myBean() {
			return new MyBean();
		}

	}

	static class MyBean {

	}

}