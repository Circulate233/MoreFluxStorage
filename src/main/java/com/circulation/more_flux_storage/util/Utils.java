package com.circulation.more_flux_storage.util;

public abstract class Utils {

    private static boolean trigger;

    public static <T> T trigger(T t) {
        trigger = true;
        return t;
    }

    public static boolean trigger() {
        var o = trigger;
        trigger = false;
        return o;
    }

}
