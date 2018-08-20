package com.example.bench;

import java.io.Closeable;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

public class LoggingSystemAllocations {

	public static void main(String[] args) throws Exception {
		while (true) {
			long start = System.currentTimeMillis();
			LauncherState state = new LauncherState();
			state.setMainClass(TestApplication.class);
			state.shared();
			for (int i = 0; i < 1000; i++) {
				state.run();
			}
			state.close();
			long end = System.currentTimeMillis();
			System.out.println("Duration: " + (end - start));
		}
	}
	
	public static class TestApplication implements Runnable, Closeable {
		private LoggingApplicationListener listener;
		private SpringApplication application;
		private static ConfigurableEnvironment environment = new StandardEnvironment();
		@Override
		public void close() throws IOException {
			listener.onApplicationEvent(new ApplicationFailedEvent(application,
					new String[0], null, new RuntimeException()));
		}
		public void run() {
			listener = new LoggingApplicationListener();
			application = new SpringApplication(TestApplication.class);
			listener.onApplicationEvent(
					new ApplicationStartingEvent(application, new String[0]));
			ConfigurableEnvironment environment = TestApplication.environment;
			listener.onApplicationEvent(new ApplicationEnvironmentPreparedEvent(
					application, new String[0], environment));
		}
	}

}