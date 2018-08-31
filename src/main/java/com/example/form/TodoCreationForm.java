package com.example.form;

import nablarch.core.validation.ValidateFor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
import nablarch.core.validation.validator.Required;

public class TodoCreationForm {

    private String content;

    public String getContent() {
        return content;
    }

    @Domain(ExampleDomain.CONTENT)
    @Required
    public void setContent(final String content) {
        this.content = content;
    }

    @ValidateFor("validate")
    public static void validate(final ValidationContext<TodoCreationForm> context) {
        ValidationUtil.validateAll(context);
    }
}
