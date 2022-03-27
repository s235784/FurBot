package pro.furry.furbot.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author NuoTian
 * @date 2022/3/27
 */
@Getter
@AllArgsConstructor
public enum GlobalSettingType {
    Super_Admin_QQ("super_admin_qq");
    private final String settingName;
}
