package pro.furry.furbot.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * @author NuoTian
 * @date 2022/4/17
 */
@Getter
@AllArgsConstructor
public class MethodContext {
    private Method method;
    private Class<?> aClass;
}
