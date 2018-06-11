package com.example.demo;

import java.io.Closeable;
import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
public class DemoApplication implements Runnable, Closeable {

	private ConfigurableApplicationContext context;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

	public static void main(String[] args) throws Exception {
		DemoApplication last = new DemoApplication();
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
		context = new SpringApplicationBuilder(DemoApplication.class)
				.properties("--server.port=0", "--spring.jmx.enabled=false").run();
	}

}
