package pro.furry.furbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pro.furry.furbot.service.QQBotService;
import pro.furry.furbot.util.ReceiveReflectUtil;
import pro.furry.furbot.util.SpringContextUtil;

@MapperScan("pro.furry.furbot.mapper")
@SpringBootApplication
public class FurBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(FurBotApplication.class, args);
        ReceiveReflectUtil.scanReceiveMethods();
        SpringContextUtil.getBean(QQBotService.class).startBot();
    }
}