package com.example.func;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.boot.autoconfigure.gson.GsonProperties;
import org.springframework.boot.autoconfigure.http.HttpEncodingProperties;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.reactor.core.ReactorCoreProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.HackingObjectProvider;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.EnableWebFluxConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.WebFluxConfig;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.support.HandlerFunctionAdapter;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.function.server.support.ServerResponseResultHandler;
import org.springframework.web.reactive.result.SimpleHandlerAdapter;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
import org.springframework.web.reactive.result.view.ViewResolutionResultHandler;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.i18n.LocaleContextResolver;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

public class FuncApplication implements Runnable, Closeable {

	public static final String MARKER = "Benchmark app started";

	private GenericApplicationContext context;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		FuncApplication bean = new FuncApplication();
		bean.run();
		System.err.println(
				"Started HttpServer: " + (System.currentTimeMillis() - t0) + "ms");
		if (Boolean.getBoolean("demo.close")) {
			bean.close();
		}
	}

	@Override
	public void close() throws IOException {
		if (context != null) {
			context.close();
		}
	}

	@Override
	public void run() {
		create();
		System.err.println(MARKER);
	}

	private ConfigurableApplicationContext create() {
		AnnotationConfigReactiveWebServerApplicationContext context = new AnnotationConfigReactiveWebServerApplicationContext();
		this.context = context;
		registerDemoApplication();
		context.register(PropertiesConfiguration.class);
		// context.register(LazyInitBeanFactoryPostProcessor.class);
		registerPropertyPlaceholderAutoConfiguration();
		registerReactiveWebServerFactoryAutoConfiguration();
		registerErrorWebFluxAutoConfiguration();
		registerWebFluxAutoConfiguration();
		registerHttpHandlerAutoConfiguration();
		registerGsonAutoConfiguration();
		registerHttpMessageConvertersAutoConfiguration();
		registerReactorCoreAutoConfiguration();
		registerRestTemplateAutoConfiguration();
		registerWebClientAutoConfiguration();
		context.refresh();
		return context;
	}

	private void registerPropertyPlaceholderAutoConfiguration() {
		context.registerBean(PropertySourcesPlaceholderConfigurer.class,
				() -> PropertyPlaceholderAutoConfiguration
						.propertySourcesPlaceholderConfigurer());
	}

	private void registerReactiveWebServerFactoryAutoConfiguration() {
		ReactiveWebServerFactoryAutoConfiguration config = new ReactiveWebServerFactoryAutoConfiguration();
		context.registerBean(ReactiveWebServerFactoryCustomizer.class,
				() -> config.reactiveWebServerFactoryCustomizer(
						context.getBean(ServerProperties.class)));
		context.registerBean(NettyReactiveWebServerFactory.class,
				() -> new NettyReactiveWebServerFactory());
	}

	private void registerErrorWebFluxAutoConfiguration() {
		context.registerBean(ErrorAttributes.class, () -> new DefaultErrorAttributes(
				context.getBean(ServerProperties.class).getError().isIncludeException()));
		context.registerBean(ErrorWebExceptionHandler.class, () -> {
			return errorWebFluxAutoConfiguration()
					.errorWebExceptionHandler(context.getBean(ErrorAttributes.class));
		});
	}

	private ErrorWebFluxAutoConfiguration errorWebFluxAutoConfiguration() {
		ServerProperties serverProperties = context.getBean(ServerProperties.class);
		ResourceProperties resourceProperties = context.getBean(ResourceProperties.class);
		ServerCodecConfigurer serverCodecs = context.getBean(ServerCodecConfigurer.class);
		return new ErrorWebFluxAutoConfiguration(serverProperties, resourceProperties,
				new BeanFactoryListProvider<>(context, ViewResolver.class), serverCodecs,
				context);
	}

	private void registerWebFluxAutoConfiguration() {
		context.registerBean(EnableWebFluxConfigurationWrapper.class,
				() -> new EnableWebFluxConfigurationWrapper(
						new EnableWebFluxConfiguration(
								context.getBean(WebFluxProperties.class))));
		context.registerBean(HandlerFunctionAdapter.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.handlerFunctionAdapter());
		context.registerBean(WebHttpHandlerBuilder.LOCALE_CONTEXT_RESOLVER_BEAN_NAME,
				LocaleContextResolver.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.localeContextResolver());
		context.registerBean(RequestMappingHandlerAdapter.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.requestMappingHandlerAdapter());
		context.registerBean(RequestMappingHandlerMapping.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.requestMappingHandlerMapping());
		context.registerBean(HandlerMapping.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.resourceHandlerMapping());
		context.registerBean(ResponseBodyResultHandler.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.responseBodyResultHandler());
		context.registerBean(ResponseEntityResultHandler.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.responseEntityResultHandler());
		context.registerBean(WebExceptionHandler.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.responseStatusExceptionHandler());
		context.registerBean(RouterFunctionMapping.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.routerFunctionMapping());
		context.registerBean(WebHttpHandlerBuilder.SERVER_CODEC_CONFIGURER_BEAN_NAME,
				ServerCodecConfigurer.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.serverCodecConfigurer());
		context.registerBean(ServerResponseResultHandler.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.serverResponseResultHandler());
		context.registerBean(SimpleHandlerAdapter.class, () -> context
				.getBean(EnableWebFluxConfigurationWrapper.class).simpleHandlerAdapter());
		context.registerBean(ViewResolutionResultHandler.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.viewResolutionResultHandler());
		context.registerBean(ReactiveAdapterRegistry.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.webFluxAdapterRegistry());
		context.registerBean(RequestedContentTypeResolver.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.webFluxContentTypeResolver());
		context.registerBean(FormattingConversionService.class,
				() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
						.webFluxConversionService());
		context.registerBean(Validator.class, () -> context
				.getBean(EnableWebFluxConfigurationWrapper.class).webFluxValidator());
		context.registerBean(WebHttpHandlerBuilder.WEB_HANDLER_BEAN_NAME,
				DispatcherHandler.class, () -> context
						.getBean(EnableWebFluxConfigurationWrapper.class).webHandler());
		context.registerBean(WebFluxConfigurer.class,
				() -> new WebFluxConfig(context.getBean(ResourceProperties.class),
						context.getBean(WebFluxProperties.class), context,
						new BeanFactoryListProvider<>(context,
								HandlerMethodArgumentResolver.class),
						new BeanFactoryListProvider<>(context, CodecCustomizer.class),
						new HackingObjectProvider(),
						new BeanFactoryListProvider<>(context, ViewResolver.class)));
	}

	private void registerHttpHandlerAutoConfiguration() {
		context.registerBean(HttpHandler.class,
				() -> WebHttpHandlerBuilder.applicationContext(context).build());
	}

	private void registerDemoApplication() {
		context.registerBean(RouterFunction.class, () -> userEndpoints());
	}

	private void registerGsonAutoConfiguration() {
		GsonAutoConfiguration config = new GsonAutoConfiguration();
		context.registerBean(GsonBuilder.class, () -> config.gsonBuilder(new ArrayList<>(
				context.getBeansOfType(GsonBuilderCustomizer.class).values())));
		context.registerBean(Gson.class,
				() -> config.gson(context.getBean(GsonBuilder.class)));
		context.registerBean(GsonBuilderCustomizer.class, () -> config
				.standardGsonBuilderCustomizer(context.getBean(GsonProperties.class)));
	}

	private void registerHttpMessageConvertersAutoConfiguration() {
		context.registerBean(HttpMessageConverters.class, () -> {
			HttpMessageConvertersAutoConfiguration config = new HttpMessageConvertersAutoConfiguration(
					new BeanFactoryListProvider<>(context,
							new ParameterizedTypeReference<HttpMessageConverter<?>>() {
							}));
			return config.messageConverters();
		});
		context.registerBean(StringHttpMessageConverter.class,
				this::stringHttpMessageConverter);
		context.registerBean(GsonHttpMessageConverter.class,
				() -> new GsonHttpMessageConverter(context.getBean(Gson.class)));
	}

	StringHttpMessageConverter stringHttpMessageConverter() {
		StringHttpMessageConverter converter = new StringHttpMessageConverter(
				context.getBean(HttpEncodingProperties.class).getCharset());
		converter.setWriteAcceptCharset(false);
		return converter;
	}

	private void registerReactorCoreAutoConfiguration() {
		context.registerBean(ReactorConfiguration.class,
				() -> new ReactorConfiguration());
	}

	private void registerRestTemplateAutoConfiguration() {
		RestTemplateAutoConfiguration config = new RestTemplateAutoConfiguration(
				new BeanFactoryObjectProvider<>(context, HttpMessageConverters.class),
				new BeanFactoryListProvider<>(context, RestTemplateCustomizer.class));
		context.registerBean(RestTemplateBuilder.class,
				() -> config.restTemplateBuilder());
	}

	private void registerWebClientAutoConfiguration() {
		context.registerBean(WebClient.Builder.class, () -> {
			WebClientAutoConfiguration config = new WebClientAutoConfiguration(
					new BeanFactoryListProvider<>(context, WebClientCustomizer.class));
			return config.webClientBuilder();
		});
	}

	@EnableConfigurationProperties({ ServerProperties.class, ResourceProperties.class,
			WebFluxProperties.class, GsonProperties.class, HttpEncodingProperties.class,
			ReactorCoreProperties.class })
	@Import({
			ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class })
	public static class PropertiesConfiguration {
	}

}

class ReactorConfiguration {

	@Autowired
	protected void initialize(ReactorCoreProperties properties) {
		if (properties.getStacktraceMode().isEnabled()) {
			Hooks.onOperatorDebug();
		}
	}

}

class EnableWebFluxConfigurationWrapper {
	private final EnableWebFluxConfiguration config;

	public EnableWebFluxConfigurationWrapper(EnableWebFluxConfiguration config) {
		this.config = config;
	}

	public EnableWebFluxConfiguration getConfig() {
		return this.config;
	}

	public DispatcherHandler webHandler() {
		return this.config.webHandler();
	}

	public WebExceptionHandler responseStatusExceptionHandler() {
		return this.config.responseStatusExceptionHandler();
	}

	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		return this.config.requestMappingHandlerMapping();
	}

	public RequestedContentTypeResolver webFluxContentTypeResolver() {
		return this.config.webFluxContentTypeResolver();
	}

	public RouterFunctionMapping routerFunctionMapping() {
		return this.config.routerFunctionMapping();
	}

	public HandlerMapping resourceHandlerMapping() {
		return this.config.resourceHandlerMapping();
	}

	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		return this.config.requestMappingHandlerAdapter();
	}

	public FormattingConversionService webFluxConversionService() {
		return this.config.webFluxConversionService();
	}

	public Validator webFluxValidator() {
		return this.config.webFluxValidator();
	}

	public ServerCodecConfigurer serverCodecConfigurer() {
		return this.config.serverCodecConfigurer();
	}

	public LocaleContextResolver localeContextResolver() {
		return this.config.localeContextResolver();
	}

	public ReactiveAdapterRegistry webFluxAdapterRegistry() {
		return this.config.webFluxAdapterRegistry();
	}

	public HandlerFunctionAdapter handlerFunctionAdapter() {
		return this.config.handlerFunctionAdapter();
	}

	public SimpleHandlerAdapter simpleHandlerAdapter() {
		return this.config.simpleHandlerAdapter();
	}

	public ResponseEntityResultHandler responseEntityResultHandler() {
		return this.config.responseEntityResultHandler();
	}

	public ResponseBodyResultHandler responseBodyResultHandler() {
		return this.config.responseBodyResultHandler();
	}

	public ViewResolutionResultHandler viewResolutionResultHandler() {
		return this.config.viewResolutionResultHandler();
	}

	public ServerResponseResultHandler serverResponseResultHandler() {
		return this.config.serverResponseResultHandler();
	}

}
