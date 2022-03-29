package pro.furry.furbot.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.furry.furbot.exception.LocalException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author NuoTian
 * @date 2022/3/28
 */
@Slf4j
@Service
public class SauceService {
    private HttpService httpService;
    private RedisService redisService;
    private SuAdminService suAdminService;

    @Autowired
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setSuAdminService(SuAdminService suAdminService) {
        this.suAdminService = suAdminService;
    }

    public void searchSauce(MessageEvent event, Image image) {
        Long bId = event.getBot().getId();
        Long gId = event.getSubject().getId();
        Long sId = event.getSender().getId();
        if (!suAdminService.isSuperAdmin(sId) && redisService.isGroupSearchedPicture(bId, gId)) {
            event.getSubject().sendMessage("请求太频繁了，休息一下吧");
            return;
        }

        log.info("Search Picture form Sauce");
        String url = Image.queryUrl(image);
        Map<String, String> data = httpService.searchPictureFromSauce(url);
        String similarity = data.get("similarity");
        String thumbnail = data.get("thumbnail");
        String title = data.get("title");
        String author = data.get("author");
        String external = data.get("external");

        InputStream inputStream = httpService.getPictureFromURL(thumbnail);
        ExternalResource resource;
        try {
            resource = ExternalResource.Companion.create(inputStream);
        } catch (IOException e) {
            throw new LocalException("读取图片时发生错误", e);
        }
        log.info("Start to Send Message");
        Image thumbnailImage = ExternalResource.uploadAsImage(resource, event.getSubject());
        MessageChain chain = new MessageChainBuilder()
                .append("标题：")
                .append(title)
                .append("\n作者：")
                .append(author)
                .append("\n相似度：")
                .append(similarity)
                .append("\n")
                .append(thumbnailImage)
                .append("\n")
                .append(external)
                .build();
        event.getSubject().sendMessage(chain);
        try {
            inputStream.close();
            resource.close();
        } catch (IOException e) {
            throw new LocalException("关闭资源时发生错误", e);
        }

        if (!suAdminService.isSuperAdmin(sId)) {
            redisService.cacheGroupSearchedPicture(bId, gId);
        }
    }
}
