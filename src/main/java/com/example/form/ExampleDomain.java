package com.example.form;

import java.lang.annotation.Annotation;
import java.util.List;

import nablarch.core.validation.domain.DomainDefinition;
import nablarch.core.validation.domain.DomainValidationHelper;
import nablarch.core.validation.validator.Length;

public enum ExampleDomain implements DomainDefinition {

    @Length(max = 10)
    CONTENT;

    @Override
    public Annotation getConvertorAnnotation() {
        return DomainValidationHelper.getConvertorAnnotation(this);
    }

    @Override
    public List<Annotation> getValidatorAnnotations() {
        return DomainValidationHelper.getValidatorAnnotations(this);
    }
}
