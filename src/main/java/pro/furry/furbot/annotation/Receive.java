package pro.furry.furbot.annotation;

import pro.furry.furbot.type.ReceiveType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Receive {
    ReceiveType type() default ReceiveType.None;
    String msg() default "";
}
