package pro.furry.furbot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Controller;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.exception.LocalException;
import pro.furry.furbot.pojo.MethodContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
@Slf4j
public class ReceiveReflectUtil {
    private static List<MethodContext> receiveMethods;

    public static void scanReceiveMethods() {
        log.info("Start to scan Receive Annotation");
        receiveMethods = new ArrayList<>();
        Map<String, Object> controllers = SpringContextUtil.getBeansWithAnnotation(Controller.class);
        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            Object value = entry.getValue();
            Class<?> aClass = AopUtils.getTargetClass(value);
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Receive.class)) {
                    log.info("Scanned {}()", method.getName());
                    receiveMethods.add(new MethodContext(method, aClass));
                }
            }
        }
    }

    public static List<MethodContext> getReceiveMethods() throws LocalException {
        if (receiveMethods == null) {
            throw new LocalException("反射调用列表为null");
        }
        return receiveMethods;
    }
}
