package com.example.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.entity.Todo;
import com.example.form.TodoCreationForm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nablarch.common.dao.EntityList;
import nablarch.common.dao.UniversalDao;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
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
        return new HttpResponse()
                .setContentType("application/json")
                .write(mapper.writeValueAsString(value));
    }

    public HttpResponse postCreate(final HttpRequest request, final ExecutionContext context)
            throws JsonProcessingException {

        final ValidationContext<TodoCreationForm> validationContext = ValidationUtil
                .validateAndConvertRequest(TodoCreationForm.class, request, "validate");

        validationContext.abortIfInvalid();

        final TodoCreationForm form = validationContext.createObject();
        final Todo entity = new Todo();
        entity.setContent(form.getContent());
        entity.setDone(false);
        UniversalDao.insert(entity);
        return new HttpResponse()
                .setContentType("application/json")
                .write(mapper.writeValueAsString(entity));
    }
}
