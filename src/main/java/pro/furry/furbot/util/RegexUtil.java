package pro.furry.furbot.util;

import java.util.regex.Pattern;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
public class RegexUtil {
    public static boolean matchNumberAfterText(String text, String frontText) {
        String pattern = frontText + "\\s[0-9]*";
        return Pattern.matches(pattern, text);
    }

    public static boolean matchTextAfterText(String text, String frontText) {
        String pattern = frontText + "\\s[\\s\\S]*";
        return Pattern.matches(pattern, text);
    }

}
