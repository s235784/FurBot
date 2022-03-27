package pro.furry.furbot.util;

import java.util.Random;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
public class PublicUtil {
    public static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseLong(Object s) {
        try {
            return Long.parseLong(String.valueOf(s));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int getRandInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
}
