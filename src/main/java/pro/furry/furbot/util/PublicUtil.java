package pro.furry.furbot.util;

import pro.furry.furbot.exception.LocalException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static boolean parseBoolean(String s) {
        return Boolean.parseBoolean(s);
    }

    public static int getRandInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public static byte[] getBytesAndClose(InputStream inputStream) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            inputStream.close();
            output.close();
            return output.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
