package org.cloudfoundry.samples.music;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.cloudfoundry.samples.music.config.SpringApplicationContextInitializer;
import org.cloudfoundry.samples.music.repositories.AlbumRepositoryPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import javax.servlet.*;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .initializers(new SpringApplicationContextInitializer())
                .listeners(new AlbumRepositoryPopulator())
                .application()
                .run(args);
    }

    @Bean
    public FilterRegistrationBean logCurrentSpanContext() {

        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setName("span-logging");
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        filterRegistrationBean.setFilter(new HttpFilter() {

            private final Logger LOG = LoggerFactory.getLogger("span-logging");

            @Override
            protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
                SpanContext spanContext = Span.current().getSpanContext();
                LOG.info("Request to {} with trace id {} and span id {}.", request.getRequestURI(), spanContext.getTraceId(), spanContext.getSpanId());
                chain.doFilter(request, response);
            }
        });
        return filterRegistrationBean;
    }
}
