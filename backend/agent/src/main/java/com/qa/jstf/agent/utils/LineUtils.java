package com.qa.jstf.agent.utils;

public class LineUtils {

    public static boolean isLine(Integer x1, Integer y1, Integer x2, Integer y2, Integer x3, Integer y3) {
        int x2_1 = x2 - x1;
        int y2_1 = y2 - y1;
        int x3_2 = x3 - x2;
        int y3_2 = y3 - y2;

        if (x2_1 == 0 && x3_2 == 0) {
            return true;
        }

        if (x2_1 != 0 && x3_2 != 0) {
            return x2_1 * y3_2 == x3_2 * y2_1;
        }

        return false;
    }
}
