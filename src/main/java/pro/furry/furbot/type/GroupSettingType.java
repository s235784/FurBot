package pro.furry.furbot.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * @author NuoTian
 * @date 2022/3/28
 */
@Getter
@AllArgsConstructor
public enum GroupSettingType {
    Show_R18_Content("r18", "show_r18_content", "false", Boolean.class),
    Picture_Time_Limit("time_limit", "picture_time_limit", "300", String.class);
    private final String showName;
    private final String settingName;
    private final String defaultValue;
    private final Class<?> valueType;

    @Nullable
    public static GroupSettingType getInstance(String showName) {
        for (GroupSettingType type : GroupSettingType.values()) {
            if (type.getShowName().equalsIgnoreCase(showName)) {
                return type;
            }
        }
        return null;
    }
}
