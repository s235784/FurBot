package pro.furry.furbot.util;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Controller;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.exception.LocalException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
@Slf4j
public class ReceiveReflectUtil {
    private static List<Object[]> receiveMethods;

    public static void scanReceiveMethods() {
        log.info("Start to scan Receive");
        receiveMethods = new ArrayList<>();
        Reflections reflections = new Reflections("pro.furry.furbot.controller");
        Set<Class<?>> classSet = reflections.get(SubTypes.of(TypesAnnotated.with(Controller.class)).asClass());
        for (Class<?> clazz : classSet) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Receive.class)) {
                    log.info("Scanned " + method.getName() + "()");
                    receiveMethods.add(new Object[]{method, clazz});
                }
            }
        }
    }

    public static List<Object[]> getReceiveMethods() throws LocalException {
        if (receiveMethods == null) {
            throw new LocalException("反射调用列表为null");
        }
        return receiveMethods;
    }
}
