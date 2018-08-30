package com.example.nabchanspringbootsample.action;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import nablarch.common.web.session.SessionEntry;
import nablarch.common.web.session.SessionUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

@Component
public class SessionAction {

    public HttpResponse postEntry(final HttpRequest request, final ExecutionContext context) {

        final BiFunction<HttpRequest, String, Optional<String>> extractor = (req, name) -> Arrays
                .stream(req.getParam(name)).findFirst();

        final String name = extractor.apply(request, "name").get();
        final String value = extractor.apply(request, "value").get();
        final long sleep = extractor.apply(request, "sleep").map(Long::parseLong).orElse(5L);

        SessionUtil.put(context, name, value);

        try {
            TimeUnit.SECONDS.sleep(sleep);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        return new HttpResponse().write("OK");
    }

    public HttpResponse getAll(final HttpRequest request, final ExecutionContext context) {

        final Map<String, Object> values = context.getSessionStoreMap().entrySet().stream()
                .map(entry -> {
                    final String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof SessionEntry) {
                        value = ((SessionEntry) value).getValue();
                    }
                    final Map.Entry<String, Object> newEntry = new AbstractMap.SimpleEntry<>(key,
                            value);
                    return newEntry;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new HttpResponse().write(values.toString());
    }
}
