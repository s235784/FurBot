package pro.furry.furbot.handler;

import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.type.ReceiveType;
import pro.furry.furbot.util.ReceiveReflectUtil;
import pro.furry.furbot.util.SpringContextUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
@Slf4j
public class GlobalEventHandler extends SimpleListenerHost {

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        // 处理事件处理时抛出的异常
        log.error(exception.toString());
        exception.printStackTrace();
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MessageEvent event) throws Exception {
        log.info("onMessage");
        for (Object[] objects : ReceiveReflectUtil.getReceiveMethods()) {
            Method method = (Method) objects[0];
            Receive annotation = method.getAnnotation(Receive.class);
            if (event.getMessage().contentToString().equals(annotation.msg())) {
                if (event.getSubject() instanceof Group && annotation.type() == ReceiveType.Group) {
                    invokeMethod((Class<?>) objects[1], method, event);
                } else if (event.getSubject() instanceof User && annotation.type() == ReceiveType.User) {
                    invokeMethod((Class<?>) objects[1], method, event);
                }
            }

        }
        return ListeningStatus.LISTENING; // 继续监听事件 ListeningStatus.STOPPED 停止监听
    }

    private void invokeMethod(Class<?> clazz, Method method, MessageEvent event) throws Exception {
        log.info("Invoke " + method.getName() + "()");
        Parameter[] parameters = method.getParameters();
        final int length = parameters.length;
        if (length > 0) {
            Object[] args = new Object[length];
            for (int index = 0; index < length; index++) {
                Parameter parameter = parameters[index];
                if (parameter.getParameterizedType().getClass().isInstance(MessageEvent.class)) {
                    args[index] = event;
                } else {
                    args[index] = null;
                }
            }
            method.invoke(SpringContextUtil.getBean(clazz), args);
        } else {
            method.invoke(SpringContextUtil.getBean(clazz));
        }
    }
}
