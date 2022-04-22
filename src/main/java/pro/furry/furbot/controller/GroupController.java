package pro.furry.furbot.controller;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
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
    private AdminService suAdminService;
    private SettingService settingService;

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
    public void setSuAdminService(AdminService suAdminService) {
        this.suAdminService = suAdminService;
    }

    @Autowired
    public void setGroupSettingService(SettingService groupSettingService) {
        this.settingService = groupSettingService;
    }

    @Receive(type = ReceiveType.Group, msg = "/帮助", query = ReceiveQueryType.EqualOrFront)
    public void showFunMenu(GroupMessageEvent event, ReceiveParameter parameter) {
        String[] parameters = parameter.getParameters();
        if (parameters.length == 0) {
            log.info("Start to Send Message");
            MessageChainBuilder chainBuilder = new MessageChainBuilder()
                    .append("--------命令菜单--------\n")
                    .append("发送随机画师图片  /来张涩图\n")
                    .append("发送指定画师图片  /来张涩图 [画师P站ID]\n")
                    .append("向随机列表添加画师  /添加画师 [画师P站ID]\n")
                    .append("向随机列表删除画师  /删除画师 [画师P站ID]\n")
                    .append("查看当前的随机列表  /画师列表\n")
                    .append("更改机器人设置信息  /设置\n")
                    .append("查找图片出处  /搜图 [图片]\n")
                    .append("给管理员留言  /留言 [留言内容]\n")
                    .append("查看关于信息  /关于\n")
                    .append("---------------\n")
                    .append("发送命令时注意不要加上[ ]");
            event.getSubject().sendMessage(chainBuilder.build());
            return;
        }
        if (parameters.length == 1) {
            if ("设置".equals(parameters[0])) {
                MessageChainBuilder chainBuilder = new MessageChainBuilder()
                        .append("--------设置菜单--------\n")
                        .append("是否允许发送R18内容  /设置 r18 [开/关]");
                event.getSubject().sendMessage(chainBuilder.build());
                return;
            }
        }
        event.getSubject().sendMessage("参数不合法");
    }

    @Receive(type = ReceiveType.Group, msg = "/搜图", query = ReceiveQueryType.Contain)
    public void searchPicture(GroupMessageEvent event) {
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
    public void sendIllustration(GroupMessageEvent event, ReceiveParameter parameter) {
        String[] parameters = parameter.getParameters();
        if (parameters.length == 0) {
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
    public void addGroupPixivMember(GroupMessageEvent event, ReceiveParameter parameter) {
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

    @Receive(type = ReceiveType.Group, msg = "/删除画师", query = ReceiveQueryType.Front)
    public void deleteGroupPixivMember(GroupMessageEvent event, ReceiveParameter parameter) {
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
        pixivService.deleteGroupPixivMember(event, pixivId);
    }

    @Receive(type = ReceiveType.Group, msg = "/画师列表", query = ReceiveQueryType.EqualOrFront)
    public void listPixivMember(GroupMessageEvent event, ReceiveParameter parameter) {
        String[] parameterStrings = parameter.getParameters();
        if (parameterStrings.length == 0) {
            pixivService.listPixivMember(event, 1);
            return;
        }
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
    public void confirmAction(GroupMessageEvent event) {
        Long gId = event.getGroup().getId();
        String key = redisService.getUnconfirmedAction(event.getBot().getId(), gId);
        if (key == null) return;
        log.info("Find redis key: {}", key);
        if (redisService.isAddPMember(key, event.getBot().getId(), gId)) {
            pixivService.confirmAddPixivMember(event, key, gId);
        } else if (redisService.isDeletePMember(key, event.getBot().getId(), gId)) {
            pixivService.confirmDeletePixivMember(event, key, gId);
        }
    }

    @Receive(type = ReceiveType.Group, msg = "N")
    public void cancelAction(GroupMessageEvent event) {
        String key = redisService.getUnconfirmedAction(event.getBot().getId(), event.getSender().getId());
        if (key == null) return;
        if (redisService.isAddPMember(key, event.getBot().getId(), event.getSender().getId())) {
            pixivService.cancelAddPixivMember(event, key);
        } else if (redisService.isDeletePMember(key, event.getBot().getId(), event.getSender().getId())) {
            pixivService.cancelDeletePixivMember(event, key);
        }
    }

    @Receive(type = ReceiveType.Group, msg = "/留言", query = ReceiveQueryType.Front)
    public void send(GroupMessageEvent event) {
        Long admin = suAdminService.getSuperAdmin();
        if (admin == null) {
            event.getSubject().sendMessage("向超管发送消息时发生错误：管理员账号未配置");
            return;
        }
        Contact contact = event.getBot().getFriend(admin);
        if (contact == null) {
            event.getSubject().sendMessage("向超管发送消息时发生错误：找不到管理员");
            return;
        }
        log.info("Start to Send Message");
        MessageChainBuilder chainBuilder = new MessageChainBuilder()
                .append("您有一条新的留言 来自\n群聊：")
                .append(String.valueOf(event.getGroup().getId()))
                .append("用户：")
                .append(String.valueOf(event.getSender().getId()))
                .append("\n---------------\n")
                .append(event.getMessage());
        contact.sendMessage(chainBuilder.build());
    }

    @Receive(type = ReceiveType.Group, msg = "/设置", query = ReceiveQueryType.Front)
    public void setSetting(GroupMessageEvent event, ReceiveParameter receiveParameter) {
        if (suAdminService.isAdmin(event)) {
            settingService.setSetting(event, receiveParameter.getParameters());
        } else {
            event.getSubject().sendMessage("权限不足");
        }
    }

    @Receive(type = ReceiveType.Group, msg = "/关于")
    public void showAbout(GroupMessageEvent event) {
        log.info("Start to Send Message");
        event.getSubject().sendMessage(new PlainText("关于FurBot\n" +
                "当前版本 " + appVersion +"\n" +
                "编译时间 " + appBuildTime +"\n" +
                "项目地址 https://github.com/s235784/FurBot"));
    }
}
