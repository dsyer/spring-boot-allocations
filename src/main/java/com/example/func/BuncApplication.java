/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.func;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

/**
 * Functional bean definitions. With Spring Boot, but without
 * {@link ConfigurationClassPostProcessor}.
 * 
 * @author Dave Syer
 *
 */
public class BuncApplication extends FuncApplication {

	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		BuncApplication bean = new BuncApplication();
		bean.run();
		System.err.println(
				"Started HttpServer: " + (System.currentTimeMillis() - t0) + "ms");
		if (Boolean.getBoolean("demo.close")) {
			bean.close();
		}
	}

	@Override
	public void run() {
		SpringApplication application = new SpringApplication(BuncApplication.class) {
			@Override
			protected void load(ApplicationContext context, Object[] sources) {
				// We don't want the annotation bean definition reader
				// super.load(context, sources);
			}
		};
		application.addInitializers(this);
		application.setApplicationContextFactory(ApplicationContextFactory
				.of(() -> new ReactiveWebServerApplicationContext()));
		application.run();
		System.err.println(MARKER);
	}

}
