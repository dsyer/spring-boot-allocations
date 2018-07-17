package com.example.bench;

import java.io.IOException;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class SimpleMetadataReaderAllocations {

	public static void main(String[] args) throws IOException {
		ClassPathResource resource = new ClassPathResource(
				ClassUtils.convertClassNameToResourcePath(
						WebMvcAutoConfiguration.class.getName()) + ".class");
		SimpleMetadataReaderFactory factory = new SimpleMetadataReaderFactory();
		while (true) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < 10000; i++) {
				factory.getMetadataReader(resource).getAnnotationMetadata();
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