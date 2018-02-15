package com.example.nabchanspringbootsample.service;

import org.springframework.stereotype.Service;

@Service
public class DefaultHelloService implements HelloService {

    @Override
    public String getMessage(final String name) {
        return String.format("Hello, %s!", name);
    }
}
