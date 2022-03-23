package pro.furry.furbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.type.ReceiveType;

/**
 * @author NuoTian
 * @date 2022/3/22
 */
@Slf4j
@Service
public class UserService {
    @Receive(type = ReceiveType.User, msg = "test")
    public void send() {
        log.info("test");
    }
}
