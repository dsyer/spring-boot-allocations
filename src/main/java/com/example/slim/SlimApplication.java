package com.example.slim;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
public class SlimApplication implements Runnable, Closeable {

	private ConfigurableApplicationContext context;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

	public static void main(String[] args) throws Exception {
		SlimApplication last = new SlimApplication();
		last.run();
		if (Boolean.getBoolean("demo.close")) {
			last.close();
		}
	}

	@Override
	public void close() throws IOException {
		if (context != null) {
			context.close();
		}
	}

	@Override
	public void run() {
		SpringApplication application = new SpringApplication(Object.class) {
			@Override
			protected void load(ApplicationContext context, Object[] sources) {
				// We don't want the annotation bean definition reader
			}
		};
		Map<String, Object> properties = new HashMap<>();
		properties.put("server.port", "0");
		properties.put("spring.jmx.enabled", "false");
		application.setDefaultProperties(properties);
		application.addInitializers(new SlimApplicationContextInitializer());
		context = application.run();
	}

}
