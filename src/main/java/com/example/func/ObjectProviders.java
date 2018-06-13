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

import java.lang.reflect.Constructor;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;

/**
 * @author Dave Syer
 *
 */
public class ObjectProviders {

	public static <T> ObjectProvider<T> single(ApplicationContext context,
			Class<?> target, int index, Class<?>... params) {
		return new LazyObjectProvider<>(context, target, index, params);
	}

	public static <T> ObjectProvider<List<T>> list(ApplicationContext context,
			Class<?> target, int index, Class<?>... params) {
		return new LazyObjectProvider<>(context, target, index, params);
	}

	static class LazyObjectProvider<T> implements ObjectProvider<T> {

		private ApplicationContext context;
		private Class<?> target;
		private int index;
		private Class<?>[] params;
		private ObjectProvider<T> delegate;

		public LazyObjectProvider(ApplicationContext context, Class<?> target, int index,
				Class<?>[] params) {
			this.context = context;
			this.target = target;
			this.index = index;
			this.params = params;
		}

		@Override
		public T getObject() throws BeansException {
			if (delegate == null) {
				delegate = provider(context, target, index, params);
			}
			return delegate.getObject();
		}

		@Override
		public T getObject(Object... args) throws BeansException {
			if (delegate == null) {
				delegate = provider(context, target, index, params);
			}
			return delegate.getObject(args);
		}

		@Override
		public T getIfAvailable() throws BeansException {
			if (delegate == null) {
				delegate = provider(context, target, index, params);
			}
			return delegate.getIfAvailable();
		}

		@Override
		public T getIfUnique() throws BeansException {
			if (delegate == null) {
				delegate = provider(context, target, index, params);
			}
			return delegate.getIfUnique();
		}

		private ObjectProvider<T> provider(ApplicationContext context, Class<?> target,
				int index, Class<?>[] params) {
			Constructor<?> constructor;
			try {
				constructor = target.getConstructor(params);
			}
			catch (Exception e) {
				throw new IllegalStateException("Cannot resolve constructor", e);
			}
			MethodParameter methodParameter = new MethodParameter(constructor, index);
			@SuppressWarnings("unchecked")
			ObjectProvider<T> provider = (ObjectProvider<T>) context
					.getAutowireCapableBeanFactory()
					.resolveDependency(new DependencyDescriptor(methodParameter, false),
							ErrorWebExceptionHandler.class.getName());
			return provider;
		}

	}
}
