package com.georgev22.voterewards.command.annotation;

import com.georgev22.voterewards.command.ExecutionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Execution {
    ExecutionType value() default ExecutionType.SYNC;
}