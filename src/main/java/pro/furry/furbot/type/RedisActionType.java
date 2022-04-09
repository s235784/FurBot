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
    Add_Pixiv_Member("APM"),
    Delete_Pixiv_Member("DPM"),
    Group_Got_Picture("GGP"),
    Group_Searched_Picture("GSP");

    private final String actionName;
}
