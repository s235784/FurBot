package pro.furry.furbot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.furry.furbot.exception.LocalException;
import pro.furry.furbot.pojo.db.GroupSettingPixiv;
import pro.furry.furbot.pojo.db.PixivMember;
import pro.furry.furbot.type.RefererType;
import pro.furry.furbot.util.PublicUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
@Slf4j
@Service
public class PixivService {
    private DBService dbService;
    private HttpService httpService;
    private RedisService redisService;
    private AdminService suAdminService;

    @Autowired
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }

    @Autowired
    public void setSuAdminService(AdminService suAdminService) {
        this.suAdminService = suAdminService;
    }

    public void sendSpecificPicture(GroupMessageEvent event, Long pId) throws LocalException {
        sendPicture(event, pId, true);
    }

    public void sendRandPicture(GroupMessageEvent event) throws LocalException {
        Long gId = event.getGroup().getId();
        if (!dbService.isGroupHasPMember(gId)) {
            event.getSubject().sendMessage("请先添加一个画师 /添加画师 [画师P站ID]");
            return;
        }
        Long pId = dbService.getRandPMember(event.getSubject().getId());
        sendPicture(event, pId, false);
    }

    private void sendPicture(GroupMessageEvent event, Long pId, boolean isSpecific) throws LocalException {
        Long bId = event.getBot().getId();
        Long gId = event.getGroup().getId();
        Long sId = event.getSender().getId();
        if (!suAdminService.isSuperAdmin(sId) && redisService.isGroupGotPicture(bId, gId)) {
            event.getSubject().sendMessage("请求太频繁了，休息一下吧");
            return;
        }

        Map<String, String> data = httpService.getRandPMemberWork(gId, pId, 1, isSpecific);
        String lid = data.get("id");
        String title = data.get("title");
        String username = data.get("username");
        String imageURL = data.get("image");
        String type = data.get("type");

        InputStream imageStream = httpService.getPictureFromFurBotAPI(imageURL, RefererType.PIXIV);
        ExternalResource resource;
        try {
            resource = ExternalResource.Companion.create(imageStream);
        } catch (IOException e) {
            throw new LocalException("读取图片时发生错误", e);
        }
        log.info("Start to Send Message");
        Image image = ExternalResource.uploadAsImage(resource, event.getSubject());
        MessageChain chain = new MessageChainBuilder()
                .append(new PlainText("[" + type + "]\n"))
                .append(new PlainText("画师：" + username + "\n"))
                .append(new PlainText("标题：" + title + "\n"))
                .append(image)
                .append(new PlainText("\n" + getPixivWorkURL(lid)))
                .build();
        event.getSubject().sendMessage(chain);
        try {
            imageStream.close();
            resource.close();
        } catch (IOException e) {
            throw new LocalException("关闭资源时发生错误", e);
        }

        if (!suAdminService.isSuperAdmin(sId)) {
            redisService.cacheGroupGotPicture(bId, gId);
        }
    }

    public void addPixivMember(GroupMessageEvent event, Long pid) throws LocalException {
        Long bId = event.getBot().getId();
        Long gId = event.getGroup().getId();
        if (dbService.isGroupAddedPMember(gId, pid)) {
            event.getSubject().sendMessage("这位画师已经添加过啦");
            return;
        }

        Map<String, String> data = httpService.getPixivMemberInfo(pid);
        String name = data.get("name");
        String account = data.get("account");
        String avatarUrl = data.get("avatarUrl");

        InputStream imageStream = httpService.getPictureFromFurBotAPI(avatarUrl, RefererType.PIXIV);
        ExternalResource resource;
        try {
            resource = ExternalResource.Companion.create(imageStream);
        } catch (IOException e) {
            throw new LocalException("读取图片时发生错误", e);
        }
        log.info("Start to Send Message");
        Image image = ExternalResource.uploadAsImage(resource, event.getSubject());
        MessageChain chain = new MessageChainBuilder()
                .append(new PlainText("请确认画师信息\n"))
                .append(new PlainText("ID：" + pid + "\n"))
                .append(new PlainText("昵称：" + name + "\n"))
                .append(new PlainText("用户名：" + account + "\n"))
                .append(image)
                .append(new PlainText("\n确认添加请回复Y，取消添加请回复N"))
                .build();
        event.getSubject().sendMessage(chain);
        try {
            imageStream.close();
            resource.close();
        } catch (IOException e) {
            throw new LocalException("关闭资源时发生错误", e);
        }

        Map<String, Object> cache = new HashMap<>();
        cache.put("gId", gId);
        cache.put("pId", pid);
        cache.put("pAccount", account);
        cache.put("pName", name);
        redisService.cacheAddPMember(bId, gId, cache);
    }

    public void confirmAddPixivMember(GroupMessageEvent event, String key) throws LocalException {
        Map<Object, Object> cache = redisService.getHash(key);
        Long gId = PublicUtil.parseLong(cache.get("gId"));
        Long pId = PublicUtil.parseLong(cache.get("pId"));
        String pAccount = String.valueOf(cache.get("pAccount"));
        String pName = String.valueOf(cache.get("pName"));
        if (gId == null || pId == null || pAccount == null || pName == null) {
            throw new LocalException("获取画师信息时发生错误");
        }
        dbService.addPixivMember(gId, pId, pAccount, pName);
        redisService.deleteCache(key);
        event.getSubject().sendMessage("已添加画师 " + pAccount + " (" + pId + ")");
    }

    public void cancelAddPixivMember(GroupMessageEvent event, String key) {
        String pId = redisService.getHashItem(key, "pId");
        String pAccount = redisService.getHashItem(key, "pAccount");
        redisService.deleteCache(key);
        if (pId != null && pAccount != null) {
            event.getSubject().sendMessage("已取消添加画师 " + pAccount + " (" + pId + ")");
        }
    }

    public void deleteGroupPixivMember(GroupMessageEvent event, Long pId) throws LocalException {
        Long bId = event.getBot().getId();
        Long gId = event.getGroup().getId();
        if (!dbService.isGroupAddedPMember(gId, pId)) {
            event.getSubject().sendMessage("这位画师还没有添加呢");
            return;
        }

        PixivMember member = dbService.getPixivMemberInfo(pId);
        MessageChain chain = new MessageChainBuilder()
                .append(new PlainText("请确认画师信息\n"))
                .append(new PlainText("ID：" + pId + "\n"))
                .append(new PlainText("昵称：" + member.getPixivName() + "\n"))
                .append(new PlainText("用户名：" + member.getPixivAccount() + "\n"))
                .append(new PlainText("\n确认删除请回复Y，取消删除请回复N"))
                .build();
        event.getGroup().sendMessage(chain);

        Map<String, Object> cache = new HashMap<>();
        cache.put("gId", gId);
        cache.put("pId", pId);
        cache.put("pAccount", member.getPixivAccount());
        redisService.cacheDeletePMember(bId, gId, cache);
    }

    public void confirmDeletePixivMember(GroupMessageEvent event, String key) throws LocalException {
        Map<Object, Object> cache = redisService.getHash(key);
        Long gId = PublicUtil.parseLong(cache.get("gId"));
        Long pId = PublicUtil.parseLong(cache.get("pId"));
        String pAccount = String.valueOf(cache.get("pAccount"));
        if (gId == null || pId == null) {
            throw new LocalException("获取画师信息时发生错误");
        }
        dbService.deletePixivMember(gId, pId);
        redisService.deleteCache(key);
        event.getSubject().sendMessage("已删除画师 " + pAccount + " (" + pId + ")");
    }

    public void cancelDeletePixivMember(GroupMessageEvent event, String key) {
        String pId = redisService.getHashItem(key, "pId");
        String pAccount = redisService.getHashItem(key, "pAccount");
        redisService.deleteCache(key);
        if (pId != null && pAccount != null) {
            event.getSubject().sendMessage("已取消删除画师 " + pAccount + " (" + pId + ")");
        }
    }

    public void listPixivMember(GroupMessageEvent event, int page) {
        Long gid = event.getGroup().getId();
        Page<GroupSettingPixiv> result = dbService.getGroupPMemberByPage(gid, page);

        if (page > result.getPages()) {
            event.getSubject().sendMessage("没有那么多画师呐");
            return;
        }

        log.info("Start to Send Message");
        MessageChainBuilder chainBuilder = new MessageChainBuilder()
                .append("画师列表\n----- 第")
                .append(String.valueOf(result.getCurrent()))
                .append("页 共")
                .append(String.valueOf(result.getPages()))
                .append("页 -----\n")
                .append("ID  昵称\n");

        for (GroupSettingPixiv groupSettingPixiv : result.getRecords()) {
            PixivMember pixivMember = dbService.getPixivMember(groupSettingPixiv.getPixivId());
            chainBuilder.append(String.valueOf(pixivMember.getPixivId()))
                    .append("   ")
                    .append(pixivMember.getPixivName())
                    .append("\n");
        }
        chainBuilder.append("------ 共")
                .append(String.valueOf(result.getTotal()))
                .append("位 ------");

        event.getSubject().sendMessage(chainBuilder.build());
    }

    public String getPixivWorkURL(String lid) {
        return "https://www.pixiv.net/artworks/" + lid;
    }
}
