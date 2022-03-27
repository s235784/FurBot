package pro.furry.furbot.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
@Getter
@AllArgsConstructor
public enum RefererType {
    PIXIV("https://www.pixiv.net/");

    private final String referer;
}
