/*
 * Copyright 2025-current the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.bench;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class ClassPathExtractor {

	static List<URL> getUrls() {
		ClassLoader classLoader = ClassPathExtractor.class.getClassLoader();
		if (classLoader instanceof URLClassLoader) {
			return getUrlsFrom(((URLClassLoader) classLoader).getURLs());
		} else {
			return getUrlsFrom(Stream.of(ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator))
					.map(ClassPathExtractor::toUrl)
					.toArray(URL[]::new));
		}
	}

	static List<URL> getUrlsFrom(URL... urls) {
		List<URL> resourceJarUrls = new ArrayList<>();
		for (URL url : urls) {
			addUrl(resourceJarUrls, url);
		}
		return resourceJarUrls;
	}

	private static URL toUrl(String classPathEntry) {
		try {
			return new File(classPathEntry).toURI().toURL();
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("URL could not be created from '" + classPathEntry + "'", ex);
		}
	}

	private static File toFile(URL url) {
		try {
			return new File(url.toURI());
		} catch (URISyntaxException ex) {
			throw new IllegalStateException("Failed to create File from URL '" + url + "'");
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static void addUrl(List<URL> urls, URL url) {
		try {
			if (!"file".equals(url.getProtocol())) {
				addUrlConnection(urls, url, url.openConnection());
			} else {
				File file = toFile(url);
				if (file != null) {
					addUrlFile(urls, url, file);
				} else {
					addUrlConnection(urls, url, url.openConnection());
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static void addUrlFile(List<URL> urls, URL url, File file) {
		urls.add(url);
	}

	private static void addUrlConnection(List<URL> urls, URL url, URLConnection connection) {
		urls.add(url);
	}

}
