package pro.furry.furbot.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
@Getter
@AllArgsConstructor
public enum ErrorType {
    COMMON_ERROR("错误");

    private final String msg;
}
