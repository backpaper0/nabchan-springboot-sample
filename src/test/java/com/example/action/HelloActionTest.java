package com.example.action;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HelloActionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void message() {
        final String actual = restTemplate.getForObject("/action/Hello/message", String.class);
        assertEquals("Hello, world!", actual);
    }
}
