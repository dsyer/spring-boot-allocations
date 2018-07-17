package com.example.bench;

import java.io.IOException;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.ResolvableType;

public class ResolvableTypeAllocations {

	public static void main(String[] args) throws IOException {
		while (true) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < 100000; i++) {
				ResolvableType test = ResolvableType.forClass(TestListener.class).as(ApplicationListener.class);
				ResolvableType testGeneric = test.hasGenerics() ? test.getGeneric() : null;
				ResolvableType un = ResolvableType.forClass(UnListener.class).as(ApplicationListener.class);
				ResolvableType unGeneric = un.hasGenerics() ? un.getGeneric() : null;
				assert testGeneric != unGeneric;
			}
			long end = System.currentTimeMillis();
			System.out.println("Duration: " + (end - start));
		}
	}

	@Configuration
	static class TestListener implements ApplicationListener<ContextRefreshedEvent> {
		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
		}
	}

	@SuppressWarnings("rawtypes")
	@Configuration
	static class UnListener implements ApplicationListener {
		@Override
		public void onApplicationEvent(ApplicationEvent event) {
		}
	}

}