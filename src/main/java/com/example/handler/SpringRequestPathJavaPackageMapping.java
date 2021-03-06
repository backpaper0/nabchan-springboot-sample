package com.example.handler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.Request;
import nablarch.fw.handler.RequestPathJavaPackageMapping;

public class SpringRequestPathJavaPackageMapping extends RequestPathJavaPackageMapping
        implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected Handler<Request<?>, Object> createHandlerFor(final Object delegate,
            final ExecutionContext ctx) {
        return super.createHandlerFor(applicationContext.getBean(delegate.getClass()), ctx);
    }
}
