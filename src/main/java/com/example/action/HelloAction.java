package com.example.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.service.HelloService;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

@Component
public class HelloAction {

    @Autowired
    private HelloService service;

    public HttpResponse getMessage(final HttpRequest request, final ExecutionContext context) {
        final String message = service.getMessage("world");
        return new HttpResponse().write(message);
    }
}
