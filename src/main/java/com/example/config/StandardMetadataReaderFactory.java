/*
 * Copyright 2002-2018 the original author or authors.
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

package com.example.config;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class StandardMetadataReaderFactory implements MetadataReaderFactory {

	@Override
	public MetadataReader getMetadataReader(String className) throws IOException {
		ClassPathResource resource = new ClassPathResource(
				ClassUtils.convertClassNameToResourcePath(className));
		return getMetadataReader(resource, className);
	}

	private StandardMetadataReader getMetadataReader(ClassPathResource resource,
			String className) {
		if (ClassUtils.isPresent(className, null)) {
			return new StandardMetadataReader(resource,
					ClassUtils.resolveClassName(className, null));
		}
		throw new IllegalArgumentException("Cannot load class: " + className);
	}

	@Override
	public MetadataReader getMetadataReader(Resource resource) throws IOException {
		if (resource instanceof ClassPathResource) {
			ClassPathResource cp = (ClassPathResource) resource;
			String className = ClassUtils.convertResourcePathToClassName(cp.getPath());
			return getMetadataReader(cp, className);
		}
		throw new IllegalArgumentException("Cannot find class name for: " + resource);
	}

}

class StandardMetadataReader implements MetadataReader {

	private static Log logger = LogFactory.getLog(StandardMetadataReader.class);

	private ClassPathResource resource;

	private StandardAnnotationMetadata metadata;

	public StandardMetadataReader(ClassPathResource resource, Class<?> type) {
		this.resource = resource;
		this.metadata = new StandardAnnotationMetadata(type);
		logger.info("Metadata: " + type + " = " + metadata);
	}

	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public ClassMetadata getClassMetadata() {
		return metadata;
	}

	@Override
	public AnnotationMetadata getAnnotationMetadata() {
		return metadata;
	}

}
