package com.example.boot;

import java.io.Closeable;
import java.io.IOException;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.reactor.core.ReactorCoreAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;

public class BootApplication implements Runnable, Closeable,
		ApplicationContextInitializer<GenericApplicationContext> {

	private ConfigurableApplicationContext context;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		BootApplication bean = new BootApplication();
		bean.run();
		System.err.println(
				"Started HttpServer: " + (System.currentTimeMillis() - t0) + "ms");
		if (Boolean.getBoolean("demo.close")) {
			bean.close();
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
		this.context = create();
	}

	private ConfigurableApplicationContext create() {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(
				AutoConfiguration.class).run();
		return context;
	}

	@Override
	public void initialize(GenericApplicationContext applicationContext) {
		registerDemoApplication(applicationContext);
	}

	private void registerDemoApplication(GenericApplicationContext context) {
		context.registerBean(RouterFunction.class, () -> userEndpoints());
	}

	@Import({ // LazyInitBeanFactoryPostProcessor.class,
			PropertyPlaceholderAutoConfiguration.class,
			ReactiveWebServerFactoryAutoConfiguration.class,
			CodecsAutoConfiguration.class, ErrorWebFluxAutoConfiguration.class,
			WebFluxAutoConfiguration.class, HttpHandlerAutoConfiguration.class,
			ConfigurationPropertiesAutoConfiguration.class, GsonAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class,
			ProjectInfoAutoConfiguration.class, ReactorCoreAutoConfiguration.class,
			ReactiveSecurityAutoConfiguration.class, RestTemplateAutoConfiguration.class,
			EmbeddedWebServerFactoryCustomizerAutoConfiguration.class,
			WebClientAutoConfiguration.class })
	// @EnableAutoConfiguration
	@SpringBootConfiguration
	public static class AutoConfiguration {
	}

}
