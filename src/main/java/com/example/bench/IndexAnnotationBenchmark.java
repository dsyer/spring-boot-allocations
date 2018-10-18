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
package com.example.bench;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import com.example.config.ComponentIndex;
import com.example.config.ComponentIndex.Entry;
import com.example.config.ComponentIndexer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Measurement(iterations = 5)
@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)
public class IndexAnnotationBenchmark {

	@Benchmark
	public void anno(AnnoState state) throws Exception {
		state.setCount(8);
		state.run();
	}

	@Benchmark
	public void svce(ServiceState state) throws Exception {
		state.setCount(8);
		state.run();
	}

	@Benchmark
	public void prop(PropertiesState state) throws Exception {
		state.run();
	}

	public static void main(String[] args) {
		ServiceState state = new ServiceState();
		state.run();
		AnnoState anno = new AnnoState();
		anno.run();
		PropertiesState prop = new PropertiesState();
		prop.run();
	}

	public static void create(MultiValueMap<String, String> map, Class<?> target) {
		new StandardAnnotationMetadata(target)
				.getAnnotationAttributes(ComponentIndex.class.getName(), true)
				.forEach((name, object) -> {
					Entry[] entries = (Entry[]) object;
					for (Entry entry : entries) {
						for (String value : entry.value()) {
							map.add(entry.key(), value);
						}
					}
				});
	}

	@State(Scope.Thread)
	public static class AnnoState {
		private int count = 1;

		public void run() {
			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			for (int i = 0; i < count; i++) {
				Class<?> target = Bogus.class;
				create(map, target);
			}
			if (map.isEmpty()) {
				throw new IllegalStateException();
			}
		}

		public void setCount(int count) {
			this.count = count;
		}

	}

	@State(Scope.Thread)
	public static class ServiceState {

		private int count = 1;

		public void setCount(int count) {
			this.count = count;
		}

		public void run() {
			ServiceLoader<ComponentIndexer> loaded = ServiceLoader
					.load(ComponentIndexer.class);
			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			for (ComponentIndexer indexer : loaded) {
				for (Class<?> target : indexer.indexes()) {
					for (int i = 0; i < count; i++) {
						create(map, target);
					}
				}
			}
			if (map.isEmpty()) {
				throw new IllegalStateException();
			}
		}

	}

	@State(Scope.Thread)
	public static class PropertiesState {

		private String target = "META-INF/foo.components";

		public void setTarget(String target) {
			this.target = target;
		}

		public void run() {
			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			ClassLoader classLoader = getClass().getClassLoader();
			try {
				Enumeration<URL> urls = classLoader.getResources(target);
				if (!urls.hasMoreElements()) {
					return;
				}
				List<Properties> result = new ArrayList<>();
				while (urls.hasMoreElements()) {
					URL url = urls.nextElement();
					Properties properties = PropertiesLoaderUtils
							.loadProperties(new UrlResource(url));
					result.add(properties);
				}
				result.forEach(p -> p.forEach((k, v) -> {
					String key = (String) k;
					String[] values = StringUtils
							.commaDelimitedListToStringArray((String) v);
					for (String value : values) {
						map.add(key, value);
					}
				}));
			}
			catch (IOException ex) {
				throw new IllegalStateException(
						"Unable to load indexes from location [" + target + "]", ex);
			}
			if (map.isEmpty()) {
				throw new IllegalStateException();
			}
		}

	}

	// @formatter:off
	@ComponentIndex({
		@Entry(key="foo0", value= {"bar"}),
		@Entry(key="foo1", value= {"bar0", "bar1"}),
		@Entry(key="foo2", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo3", value= {"bar0", "bar1", "bar2", "bar3"}),
		@Entry(key="foo4", value= {"bar0"}),
		@Entry(key="foo5", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo6", value= {"bar0"}),
		@Entry(key="foo7", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo8", value= {"bar0"}),
		@Entry(key="foo9", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo10", value= {"bar"}),
		@Entry(key="foo11", value= {"bar0", "bar1"}),
		@Entry(key="foo12", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo13", value= {"bar0", "bar1", "bar2", "bar3"}),
		@Entry(key="foo14", value= {"bar0"}),
		@Entry(key="foo15", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo16", value= {"bar0"}),
		@Entry(key="foo17", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo18", value= {"bar0"}),
		@Entry(key="foo19", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo20", value= {"bar"}),
		@Entry(key="foo21", value= {"bar0", "bar1"}),
		@Entry(key="foo22", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo23", value= {"bar0", "bar1", "bar2", "bar3"}),
		@Entry(key="foo24", value= {"bar0"}),
		@Entry(key="foo25", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo26", value= {"bar0"}),
		@Entry(key="foo27", value= {"bar0", "bar1", "bar2"}),
		@Entry(key="foo28", value= {"bar0"}),
		@Entry(key="foo29", value= {"bar0", "bar1", "bar2"})
	})
// @formatter:on
	static class Bogus {
	}

	public static class Indexer implements ComponentIndexer {

		@Override
		public List<Class<?>> indexes() {
			return Collections.singletonList(Bogus.class);
		}

	}
}
