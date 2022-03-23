package pro.furry.furbot.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.PlainText;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.type.ReceiveType;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
@Slf4j
@Service
public class GroupService {
    @Value("${app.version}")
    private String appVersion;
    @Value("${app.build.time}")
    private String appBuildTime;

    @Receive(type = ReceiveType.Group, msg = "/help")
    public void showFunMenu(MessageEvent event) {
        event.getSubject().sendMessage("received Group");
    }

    @Receive(type = ReceiveType.Group, msg = "/about")
    public void showAbout(MessageEvent event) {
        event.getSubject().sendMessage(new PlainText("关于FurBot\n" +
                "当前版本 " + appVersion +"\n" +
                "编译时间 " + appBuildTime +"\n" +
                "项目地址 https://"));
    }
}
