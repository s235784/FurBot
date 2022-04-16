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
import pro.furry.furbot.exception.LocalException;
import pro.furry.furbot.pojo.MethodContext;
import pro.furry.furbot.service.SettingService;
import pro.furry.furbot.type.GroupSettingType;
import pro.furry.furbot.type.ReceiveType;
import pro.furry.furbot.pojo.ReceiveParameter;
import pro.furry.furbot.util.ReceiveReflectUtil;
import pro.furry.furbot.util.RegexUtil;
import pro.furry.furbot.util.SpringContextUtil;

import java.lang.reflect.InvocationTargetException;
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
        exception.printStackTrace();
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MessageEvent event) throws Exception {
        for (MethodContext methodContext : ReceiveReflectUtil.getReceiveMethods()) {
            Method method = methodContext.getMethod();
            String content = event.getMessage().contentToString();
            Receive annotation = method.getAnnotation(Receive.class);
            switch (annotation.query()) {
                case Equal:
                    if (content.equals(annotation.msg()))
                        checkReceiveType(methodContext.getAClass(), method, event, annotation);
                    break;
                case Front:
                    if (RegexUtil.matchTextAfterText(content, annotation.msg()))
                        checkReceiveType(methodContext.getAClass(), method, event, annotation);
                    break;
                case Behind:
                    // todo
                    break;
                case Contain:
                    if (RegexUtil.matchTextContainText(content, annotation.msg()))
                        checkReceiveType(methodContext.getAClass(), method, event, annotation);
                    break;
                case EqualOrFront:
                    if (content.equals(annotation.msg()) ||
                            RegexUtil.matchTextAfterText(content, annotation.msg()))
                        checkReceiveType(methodContext.getAClass(), method, event, annotation);
                    break;
            }
        }
        return ListeningStatus.LISTENING; // 继续监听事件 ListeningStatus.STOPPED 停止监听
    }

    private void checkReceiveType(Class<?> clazz, Method method, MessageEvent event, Receive annotation)
            throws Exception {
        if (event.getSubject() instanceof Group && annotation.type() == ReceiveType.Group) {
            if (isEnableBot(event.getSubject().getId()))
                invokeMethod(clazz, method, event, annotation);
        } else if (event.getSubject() instanceof User && annotation.type() == ReceiveType.User) {
            invokeMethod(clazz, method, event, annotation);
        }
    }

    private void invokeMethod(Class<?> clazz, Method method, MessageEvent event, Receive annotation)
            throws Exception {
        log.info("Invoke {}(), Id: {}",method.getName() , event.getSubject().getId());
        Parameter[] parameters = method.getParameters();
        final int length = parameters.length;
        if (length > 0) {
            Object[] args = new Object[length];
            for (int index = 0; index < length; index++) {
                Parameter parameter = parameters[index];
                Class<?> aClazz = (Class<?>) parameter.getParameterizedType();
                if (MessageEvent.class.isAssignableFrom(aClazz)) {
                    args[index] = event;
                } else if (ReceiveParameter.class.isAssignableFrom(aClazz)) {
                    String context = event.getMessage().contentToString();
                    args[index] = ReceiveParameter.getParameterFromContext(context, annotation.msg());
                } else {
                    args[index] = null;
                }
            }
            try {
                method.invoke(SpringContextUtil.getBean(clazz), args);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof LocalException) {
                    log.info(e.getCause().getMessage());
                    event.getSubject().sendMessage(e.getCause().getMessage());
                } else {
                    event.getSubject().sendMessage("程序运行时发生意外错误，请联系管理员");
                    throw e;
                }
            }
        } else {
            try {
                method.invoke(SpringContextUtil.getBean(clazz));
            } catch (LocalException e) {
                if (e.getCause() instanceof LocalException) {
                    log.info(e.getCause().getMessage());
                    event.getSubject().sendMessage(e.getCause().getMessage());
                } else {
                    event.getSubject().sendMessage("程序运行时发生意外错误，请联系管理员");
                    throw e;
                }
            }
        }
    }

    private boolean isEnableBot(Long gId) {
        SettingService settingService = SpringContextUtil.getBean(SettingService.class);
        return settingService.getGroupSettingAsBoolean(gId, GroupSettingType.Enable_Bot);
    }
}
