package pro.furry.furbot.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.furry.furbot.handler.GlobalEventHandler;

import java.io.File;

/**
 * @author NuoTian
 * @date 2022/3/20
 */
@Slf4j
@Service
public class QQBotService {
    @Value("${bot.number}")
    private Long number;
    @Value("${bot.password}")
    private String password;

    public void startBot() {
        log.info("Start QQBot Service");
        if (number == null || password == null) {
            log.error("账号或密码未配置！");
            return;
        }

        Bot bot = BotFactory.INSTANCE.newBot(number, password, new BotConfiguration() {{
            setHeartbeatStrategy(HeartbeatStrategy.STAT_HB); // 心跳策略
            setProtocol(MiraiProtocol.ANDROID_PAD); // 登录协议
            setCacheDir(new File("data/cache")); // 缓存目录
            fileBasedDeviceInfo("data/device.json"); // 设备信息
            enableContactCache(); // 启用列表缓存(默认关闭)
        }});
        bot.login();
        bot.getEventChannel().registerListenerHost(new GlobalEventHandler());
        // EventChannel<Event> groupChannel = GlobalEventChannel.INSTANCE.filter(event -> event instanceof GroupMessageEvent);
        // groupChannel.registerListenerHost(new GlobalEventHandler());
    }
}
