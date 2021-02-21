package com.qa.jstf.agent.utils;

import java.util.function.Predicate;

public class WaitUtils {

    public static <T> boolean wait(Predicate<T> predicate, T t,  int timeout) {

        for (int i = 0; i < timeout; i++) {
            if (true == predicate.test(t)) {
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (timeout == i+1 && false == predicate.test(t)) {
                return false;
            }
        }

        return true;
    }
}
