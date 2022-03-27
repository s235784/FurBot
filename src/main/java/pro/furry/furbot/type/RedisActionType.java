package pro.furry.furbot.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author NuoTian
 * @date 2022/3/25
 */
@Getter
@AllArgsConstructor
public enum RedisActionType {
    Unknown_Action("*"),
    Unconfirmed_Pixiv_Member("UCPM"),
    Group_Got_Picture("GGP");

    private final String actionName;
}
