package pro.furry.furbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pro.furry.furbot.pojo.QQBot;
import pro.furry.furbot.util.ReceiveReflectUtil;
import pro.furry.furbot.util.SpringContextUtil;

@SpringBootApplication
public class FurBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(FurBotApplication.class, args);
        ReceiveReflectUtil.scanReceiveMethods();
        SpringContextUtil.getBean(QQBot.class).startBot();
    }
}