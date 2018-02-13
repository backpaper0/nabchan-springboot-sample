package com.example.nabchanspringbootsample.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.nabchanspringbootsample.entity.Todo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nablarch.common.dao.EntityList;
import nablarch.common.dao.UniversalDao;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

@Component
public class TodoAction {

    @Autowired
    private ObjectMapper mapper;

    public HttpResponse getList(final HttpRequest request, final ExecutionContext context)
            throws JsonProcessingException {
        final EntityList<Todo> entityList = UniversalDao.findAll(Todo.class);
        final Object value = entityList;
        return new HttpResponse().write(mapper.writeValueAsString(value));
    }

    public HttpResponse postCreate(final HttpRequest request, final ExecutionContext context)
            throws JsonProcessingException {
        final String content = request.getParam("content")[0];
        final Todo entity = new Todo();
        entity.setContent(content);
        entity.setDone(false);
        UniversalDao.insert(entity);
        return new HttpResponse().write(mapper.writeValueAsString(entity));
    }
}
