package pro.furry.furbot.controller;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.service.*;
import pro.furry.furbot.type.ReceiveQueryType;
import pro.furry.furbot.type.ReceiveType;
import pro.furry.furbot.util.PublicUtil;
import pro.furry.furbot.pojo.ReceiveParameter;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
@Slf4j
@Controller
public class GroupController {
    @Value("${app.version}")
    private String appVersion;
    @Value("${app.build.time}")
    private String appBuildTime;

    private PixivService pixivService;
    private RedisService redisService;
    private SauceService sauceService;
    private SuAdminService suAdminService;
    private GroupSettingService groupSettingService;

    @Autowired
    public void setPixivService(PixivService pixivService) {
        this.pixivService = pixivService;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setSauceService(SauceService sauceService) {
        this.sauceService = sauceService;
    }

    @Autowired
    public void setSuAdminService(SuAdminService suAdminService) {
        this.suAdminService = suAdminService;
    }

    @Autowired
    public void setGroupSettingService(GroupSettingService groupSettingService) {
        this.groupSettingService = groupSettingService;
    }

    @Receive(type = ReceiveType.Group, msg = "/帮助")
    public void showFunMenu(MessageEvent event) {
        event.getSubject().sendMessage("received Group");
    }

    @Receive(type = ReceiveType.Group, msg = "/搜图", query = ReceiveQueryType.Contain)
    public void searchPicture(MessageEvent event) {
        MessageChain chain = event.getMessage();
        Image image = null;
        for (SingleMessage message : chain) {
            if (message instanceof Image) {
                image = (Image) message;
                break;
            }
        }
        if (image == null) {
            event.getSubject().sendMessage("请随命令附带一张要搜索的图片");
            return;
        }
        if (image.isEmoji()) {
            event.getSubject().sendMessage("图片格式不正确");
            return;
        }

        sauceService.searchSauce(event, image);
    }

    @Receive(type = ReceiveType.Group, msg = "/来张涩图", query = ReceiveQueryType.EqualOrFront)
    public void sendIllustration(MessageEvent event, ReceiveParameter parameter) {
        String[] parameters = parameter.getParameters();
        if (parameters == null || parameters.length == 0) {
            pixivService.sendRandPicture(event);
            return;
        }
        if (parameters.length > 1) {
            event.getSubject().sendMessage("只能有一个参数");
            return;
        }
        Long pid = PublicUtil.parseLong(parameters[0]);
        if (pid == null) {
            event.getSubject().sendMessage("参数只能是数字");
            return;
        }
        pixivService.sendSpecificPicture(event, pid);
    }

    @Receive(type = ReceiveType.Group, msg = "/添加画师", query = ReceiveQueryType.Front)
    public void setPixivMember(MessageEvent event, ReceiveParameter parameter) {
        String[] parameterStrings = parameter.getParameters();
        if (parameterStrings.length != 1) {
            event.getSubject().sendMessage("格式错误！");
            return;
        }
        Long pixivId = PublicUtil.parseLong(parameterStrings[0]);
        if (pixivId == null) {
            event.getSubject().sendMessage("格式错误！");
            return;
        }
        pixivService.addPixivMember(event, pixivId);
    }

    @Receive(type = ReceiveType.Group, msg = "/画师列表")
    public void listPixivMemberOne(MessageEvent event) {
        pixivService.listPixivMember(event, 1);
    }

    @Receive(type = ReceiveType.Group, msg = "/画师列表", query = ReceiveQueryType.Front)
    public void listPixivMember(MessageEvent event, ReceiveParameter parameter) {
        String[] parameterStrings = parameter.getParameters();
        if (parameterStrings.length != 1){
            event.getSubject().sendMessage("格式错误！");
            return;
        }
        Integer page = PublicUtil.parseInt(parameterStrings[0]);
        if (page == null) {
            event.getSubject().sendMessage("格式错误！");
            return;
        }
        pixivService.listPixivMember(event, page);
    }

    @Receive(type = ReceiveType.Group, msg = "Y")
    public void confirmAction(MessageEvent event) {
        String key = redisService.getUnconfirmedAction(event.getBot().getId(), event.getSender().getId());
        if (key == null) return;
        log.info("Find redis key: " + key);
        if (redisService.isUnconfirmedPMember(key, event.getBot().getId(), event.getSender().getId())) {
            pixivService.confirmPixivMember(event, key);
        }
    }

    @Receive(type = ReceiveType.Group, msg = "N")
    public void cancelAction(MessageEvent event) {
        String key = redisService.getUnconfirmedAction(event.getBot().getId(), event.getSender().getId());
        if (key == null) return;
        if (redisService.isUnconfirmedPMember(key, event.getBot().getId(), event.getSender().getId())) {
            pixivService.cancelPixivMember(event, key);
        }
    }

    @Receive(type = ReceiveType.Group, msg = "/留言", query = ReceiveQueryType.Front)
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
                .append("您有一条新的留言 来自\n群聊：")
                .append(String.valueOf(event.getSubject().getId()))
                .append("用户：")
                .append(String.valueOf(event.getSender().getId()))
                .append("\n---------------\n")
                .append(event.getMessage());
        contact.sendMessage(chainBuilder.build());
    }

    @Receive(type = ReceiveType.Group, msg = "/设置", query = ReceiveQueryType.Front)
    public void setSetting(MessageEvent event, ReceiveParameter receiveParameter) {
        String[] parameters = receiveParameter.getParameters();
        if (parameters.length != 2) {
            event.getSubject().sendMessage("格式错误！");
            return;
        }
        if (suAdminService.isSuperAdmin(event.getSender().getId())) {
            groupSettingService.changeGroupSetting(event, parameters[0], parameters[1]);
        } else {
            event.getSubject().sendMessage("权限不足");
        }
    }

    @Receive(type = ReceiveType.Group, msg = "/关于")
    public void showAbout(MessageEvent event) {
        event.getSubject().sendMessage(new PlainText("关于FurBot\n" +
                "当前版本 " + appVersion +"\n" +
                "编译时间 " + appBuildTime +"\n" +
                "项目地址 https://github.com/s235784/FurBot"));
    }
}
