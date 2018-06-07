/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.core.type.classreading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.example.config.CopyMetadataReader;

import org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Caching implementation of the {@link MetadataReaderFactory} interface,
 * caching a {@link MetadataReader} instance per Spring {@link Resource} handle
 * (i.e. per ".class" file).
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 2.5
 */
public class CachingMetadataReaderFactory extends SimpleMetadataReaderFactory {

	private Kryo kryo = new Kryo();

	{
		kryo.addDefaultSerializer(ClassLoader.class,
				new SimpleMetadataReaderSerializer());
	}


	private final Map<Resource, MetadataReader> cache = new ConcurrentReferenceHashMap<>();

	/**
	 * Create a new {@link ConcurrentReferenceCachingMetadataReaderFactory} instance for
	 * the default class loader.
	 */
	public CachingMetadataReaderFactory() {
	}

	/**
	 * Create a new {@link ConcurrentReferenceCachingMetadataReaderFactory} instance for
	 * the given resource loader.
	 * @param resourceLoader the Spring ResourceLoader to use (also determines the
	 * ClassLoader to use)
	 */
	public CachingMetadataReaderFactory(
			ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	/**
	 * Create a new {@link ConcurrentReferenceCachingMetadataReaderFactory} instance for
	 * the given class loader.
	 * @param classLoader the ClassLoader to use
	 */
	public CachingMetadataReaderFactory(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	public MetadataReader getMetadataReader(Resource resource) throws IOException {
		MetadataReader metadataReader = this.cache.get(resource);
		if (metadataReader == null) {
			metadataReader = createMetadataReader(resource);
			this.cache.put(resource, metadataReader);
		}
		return metadataReader;
	}

	/**
	 * Create the meta-data reader.
	 * @param resource the source resource.
	 * @return the meta-data reader
	 * @throws IOException on error
	 */
	protected MetadataReader createMetadataReader(Resource resource) throws IOException {
		if (resource instanceof ClassPathResource) {
			return serializableReader((ClassPathResource) resource);
		}
		return super.getMetadataReader(resource);
	}

	private MetadataReader serializableReader(ClassPathResource resource) {
		File file = new File("/tmp/cache", resource.getPath());
		if (file.exists()) {
			try (Input stream = new Input(new FileInputStream(file))) {
				return (MetadataReader) kryo.readClassAndObject(stream);
			}
			catch (Exception e) {
				throw new IllegalStateException("Could not deserialize", e);
			}
		}
		file.getParentFile().mkdirs();
		try (Output stream = new Output(new FileOutputStream(file))) {
			MetadataReader reader = super.getMetadataReader(resource);
			if (reader
					.getAnnotationMetadata() instanceof AnnotationMetadataReadingVisitor) {
				reader = new CopyMetadataReader((ClassPathResource) reader.getResource(),
						(AnnotationMetadataReadingVisitor) reader
								.getAnnotationMetadata());
			}
			kryo.writeClassAndObject(stream, reader);
			return reader;
		}
		catch (Exception e) {
			throw new IllegalStateException("Could not serialize", e);
		}
	}
	/**
	 * Clear the entire MetadataReader cache, removing all cached class metadata.
	 */
	public void clearCache() {
		this.cache.clear();
	}

}


class SimpleMetadataReaderSerializer extends Serializer<ClassLoader> {

	@Override
	public void write(Kryo kryo, Output output, ClassLoader object) {
	}

	@Override
	public ClassLoader read(Kryo kryo, Input input, Class<ClassLoader> type) {
		return getClass().getClassLoader();
	}

}
