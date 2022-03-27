package pro.furry.furbot.controller;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.PlainText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import pro.furry.furbot.annotation.Receive;
import pro.furry.furbot.service.PixivService;
import pro.furry.furbot.service.RedisService;
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

    @Autowired
    public void setPixivService(PixivService pixivService) {
        this.pixivService = pixivService;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Receive(type = ReceiveType.Group, msg = "/help")
    public void showFunMenu(MessageEvent event) {
        event.getSubject().sendMessage("received Group");
    }

    @Receive(type = ReceiveType.Group, msg = "/来张涩图")
    public void sendIllustration(MessageEvent event) {
        pixivService.sendRandPicture(event);
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

    @Receive(type = ReceiveType.Group, msg = "/about")
    public void showAbout(MessageEvent event) {
        event.getSubject().sendMessage(new PlainText("关于FurBot\n" +
                "当前版本 " + appVersion +"\n" +
                "编译时间 " + appBuildTime +"\n" +
                "项目地址 https://github.com/s235784/FurBot"));
    }
}
