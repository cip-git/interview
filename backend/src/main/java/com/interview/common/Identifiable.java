package com.interview.common;

@FunctionalInterface
public interface Identifiable<ID> {
    ID getId();
}