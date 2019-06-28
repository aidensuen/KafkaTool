package com.github.aidensuen.kafkatool.common.collection;


import java.util.Objects;
import java.util.Stack;

public class FixedStack<T> extends Stack<T> {
    private static final int DEFAULT_STACK_SIZE = 100;
    private static final int FIRST_INDEX = 0;
    private int maxSize;

    private FixedStack() {
        this(100);
    }

    private FixedStack(int maxSize) {
        this.maxSize = maxSize;
    }

    public T push(T object) {
        while (this.size() >= this.maxSize) {
            this.remove(0);
        }

        return super.push(object);
    }

    public static <T> FixedStack<T> newFixedStack() {
        return new FixedStack();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FixedStack<?> that = (FixedStack<?>) o;
        return maxSize == that.maxSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxSize);
    }
}