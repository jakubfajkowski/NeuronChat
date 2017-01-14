package common.util;

import java.util.Date;

public class Log {
    public static void print(String message) {
        System.out.println(String.format("[%s] %s.", new Date(), message));
    }

    public static void print(String format, Object... args) {
        String message = String.format(format, args);
        System.out.println(String.format("[%s] %s.", new Date(), message));
    }
}
