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

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ParameterizedTypeReference;

/**
 * @author Dave Syer
 *
 */
public class BeanFactoryListProvider<T> implements ObjectProvider<List<T>> {
	
	private final ListableBeanFactory beanFactory;
	private Class<T> type;

	public BeanFactoryListProvider(ListableBeanFactory beanFactory, Class<T> type) {
		this.beanFactory = beanFactory;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	public BeanFactoryListProvider(ListableBeanFactory beanFactory, ParameterizedTypeReference<T> type) {
		this.beanFactory = beanFactory;
		this.type = (Class<T>) ((ParameterizedType) type.getType()).getRawType();
	}

	@Override
	public List<T> getObject() throws BeansException {
		return new ArrayList<>(beanFactory.getBeansOfType(type).values());
	}

	@Override
	public List<T> getObject(Object... args) throws BeansException {
		return getObject();
	}

	@Override
	public List<T> getIfAvailable() throws BeansException {
		return beanFactory.getBeanNamesForType(type, true, false).length > 0 ? getObject() : null;
	}

	@Override
	public List<T> getIfUnique() throws BeansException {
		return getIfAvailable();
	}

}
