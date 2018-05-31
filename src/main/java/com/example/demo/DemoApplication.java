package com.example.demo;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
public class DemoApplication implements Runnable, Closeable {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	private ConfigurableApplicationContext context;

	private Closeable instance;

	private URLClassLoader loader;

	private ClassLoader orig;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < Integer.getInteger("demo.count", 1) - 1; i++) {
			try (DemoApplication app = new DemoApplication()) {
				app.isolated();
			}
		}
		new DemoApplication().run();
	}

	public void isolated() throws Exception {
		Class<?> mainClass = loadMainClass(getClass());
		instance = (Closeable) mainClass.getConstructor().newInstance();
		((Runnable) instance).run();
	}

	@Override
	public void close() throws IOException {
		CachedIntrospectionResults.clearClassLoader(getClass().getClassLoader());
		if (instance != null) {
			instance.close();
		}
		if (context != null) {
			context.close();
		}
		if (orig != null) {
			ClassUtils.overrideThreadContextClassLoader(orig);
		}
		if (loader != null) {
			try {
				loader.close();
				loader = null;
			}
			catch (Exception e) {
				log.error("Failed to close context", e);
			}
		}
		System.gc();
	}

	@Override
	public void run() {
		context = new SpringApplicationBuilder(DemoApplication.class)
				.run("--server.port=0", "--spring.jmx.enabled=false");
	}

	private Class<?> loadMainClass(Class<?> type) throws ClassNotFoundException {
		URL[] urls = ((URLClassLoader) getClass().getClassLoader()).getURLs();
		loader = new URLClassLoader(urls, getClass().getClassLoader().getParent());
		orig = ClassUtils.overrideThreadContextClassLoader(loader);
		return loader.loadClass(type.getName());
	}

}
