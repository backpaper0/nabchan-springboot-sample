package com.example.nabchanspringbootsample;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import nablarch.fw.Handler;
import nablarch.fw.handler.RequestPathJavaPackageMapping;
import nablarch.fw.web.handler.HttpResponseHandler;
import nablarch.fw.web.servlet.WebFrontController;

@SpringBootApplication
public class NabchanSpringbootSampleApplication implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public static void main(final String[] args) {
        SpringApplication.run(NabchanSpringbootSampleApplication.class, args);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public WebFrontController webFrontController() {
        final WebFrontController wfc = new WebFrontController();
        final Collection<Handler<?, ?>> handlers = new ArrayList<>();
        //        handlers.add(new HttpCharacterEncodingHandler());
        //        handlers.add(new GlobalErrorHandler());
        //        handlers.add(new SecureHandler());
        handlers.add(new HttpResponseHandler());
        handlers.add(requestPathJavaPackageMapping());
        wfc.setHandlerQueue(handlers);
        return wfc;
    }

    @Bean
    public RequestPathJavaPackageMapping requestPathJavaPackageMapping() {
        final SpringRequestPathJavaPackageMapping mapping = new SpringRequestPathJavaPackageMapping();
        mapping.setApplicationContext(applicationContext);
        mapping.setBasePackage("com.example.nabchanspringbootsample.action");
        mapping.setBasePath("/");
        mapping.setClassNameSuffix("Action");
        return mapping;
    }
}
