package pro.furry.furbot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.type.ReceiveType;

/**
 * @author NuoTian
 * @date 2022/3/22
 */
@Slf4j
@Controller
public class UserController {
    @Receive(type = ReceiveType.User, msg = "test")
    public void send() {
        log.info("test");
    }
}
