package com.memoclap.utils;

public class Utils {
    @FunctionalInterface
    public interface ThrowingBiConsumer<T,R, E extends Exception>
    {
        void accept(T o, R o2) throws E;
    }
}
