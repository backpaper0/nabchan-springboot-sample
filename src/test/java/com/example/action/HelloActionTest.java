package com.example.action;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class HelloActionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void message() {
        final String actual = restTemplate.getForObject("/Hello/message", String.class);
        assertEquals("Hello, world!", actual);
    }
}
