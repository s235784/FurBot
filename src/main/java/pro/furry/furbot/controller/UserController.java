package pro.furry.furbot.controller;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.pojo.ReceiveParameter;
import pro.furry.furbot.service.AdminService;
import pro.furry.furbot.service.SettingService;
import pro.furry.furbot.type.ReceiveQueryType;
import pro.furry.furbot.type.ReceiveType;
import pro.furry.furbot.util.PublicUtil;

/**
 * @author NuoTian
 * @date 2022/3/22
 */
@Slf4j
@Controller
public class UserController {
    private AdminService adminService;
    private SettingService settingService;

    @Autowired
    public void setSuAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Autowired
    public void setSettingService(SettingService settingService) {
        this.settingService = settingService;
    }

    @Receive(type = ReceiveType.User, msg = "/设置", query = ReceiveQueryType.Front)
    public void setSettingSuperAdmin(UserMessageEvent event, ReceiveParameter parameter) {
        if (adminService.isSuperAdmin(event.getSender().getId())) {
            settingService.setSetting(event, parameter.getParameters());
        } else {
            event.getSubject().sendMessage("权限不足");
        }
    }

    @Receive(type = ReceiveType.User, msg = "/留言", query = ReceiveQueryType.Front)
    public void sendMessageToAdmin(UserMessageEvent event, ReceiveParameter parameter) {
        Long admin = adminService.getSuperAdmin();
        if (admin == null) {
            event.getSubject().sendMessage("向管理员发送消息时发生错误：管理员账号未配置");
            return;
        }
        Contact contact = event.getBot().getFriend(admin);
        if (contact == null) {
            event.getSubject().sendMessage("向管理员发送消息时发生错误：找不到管理员");
            return;
        }
        log.info("收到留言消息, 来自：" + event.getSender().getId());
        log.info("内容：" + event.getMessage().contentToString());
        MessageChainBuilder chainBuilder = new MessageChainBuilder()
                .append("您有一条新的留言 来自\n用户：")
                .append(String.valueOf(event.getSender().getId()))
                .append("\n---------------\n")
                .append(event.getMessage());
        contact.sendMessage(chainBuilder.build());
    }

    @Receive(type = ReceiveType.User, msg = "/回复", query = ReceiveQueryType.Front)
    public void replyMessage(UserMessageEvent event, ReceiveParameter parameter) {
        Long admin = adminService.getSuperAdmin();
        if (admin == null) {
            event.getSubject().sendMessage("管理员账号未配置");
            return;
        }
        if (event.getSubject().getId() != admin) {
            event.getSubject().sendMessage("无权限");
            return;
        }
        String[] parameters = parameter.getParameters();
        if (parameters.length != 2) {
            event.getSubject().sendMessage("参数数量不正确");
            return;
        }
        Long replyId = PublicUtil.parseLong(parameters[0]);
        if (replyId == null) {
            event.getSubject().sendMessage("参数格式错误");
            return;
        }
        Contact contact = event.getBot().getStranger(replyId);
        if (contact == null) {
            event.getSubject().sendMessage("无法向该用户发送消息");
            return;
        }
        log.info("向用户发送回复, 用户：" + parameters[0]);
        log.info("内容：" + parameters[1]);
        MessageChainBuilder chainBuilder = new MessageChainBuilder()
                .append("来自管理员的消息\n---------------\n")
                .append(parameters[1]);
        contact.sendMessage(chainBuilder.build());
    }
}
