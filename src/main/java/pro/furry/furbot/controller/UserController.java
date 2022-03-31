package pro.furry.furbot.controller;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.service.SuAdminService;
import pro.furry.furbot.type.ReceiveQueryType;
import pro.furry.furbot.type.ReceiveType;

/**
 * @author NuoTian
 * @date 2022/3/22
 */
@Slf4j
@Controller
public class UserController {
    private SuAdminService suAdminService;

    @Autowired
    public void setSuAdminService(SuAdminService suAdminService) {
        this.suAdminService = suAdminService;
    }

    @Receive(type = ReceiveType.User, msg = "/留言", query = ReceiveQueryType.Front)
    public void send(MessageEvent event) {
        Long admin = suAdminService.getSuperAdmin();
        if (admin == null) {
            event.getSubject().sendMessage("向管理员发送消息时发生错误：管理员账号未配置");
            return;
        }
        Contact contact = event.getBot().getFriend(admin);
        if (contact == null) {
            event.getSubject().sendMessage("向管理员发送消息时发生错误：找不到管理员");
            return;
        }
        MessageChainBuilder chainBuilder = new MessageChainBuilder()
                .append("您有一条新的留言 来自\n用户：")
                .append(String.valueOf(event.getSender()))
                .append("\n---------------\n")
                .append(event.getMessage());
        contact.sendMessage(chainBuilder.build());
    }
}
