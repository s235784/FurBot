package pro.furry.furbot.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.furry.furbot.type.RedisActionType;
import pro.furry.furbot.util.RedisUtil;

import java.util.Map;
import java.util.Set;

/**
 * @author NuoTian
 * @date 2022/3/25
 */
@Slf4j
@Service
public class RedisService {
    private RedisUtil redisUtil;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public String getUnconfirmedAction(Long bId, Long sId) {
        Set<String> keys = redisUtil.keys(getFormatKey(bId, RedisActionType.Unknown_Action, sId));
        if (keys.isEmpty()) 
            return null;
        return keys.iterator().next();
    }

    public void cacheUnconfirmedPMember(Long bId, Long sId, @NotNull Map<String, Object> cache) {
        log.info("Set UnconfirmedPMember Cache:" + cache);
        redisUtil.hashSet(getFormatKey(bId, RedisActionType.Unconfirmed_Pixiv_Member, sId), cache, 60);
    }

    public boolean isUnconfirmedPMember(@NotNull String key, Long bId, Long sId) {
        return key.equals(getFormatKey(bId, RedisActionType.Unconfirmed_Pixiv_Member, sId));
    }

    public void cacheGroupGotPicture(Long bId, Long gId) {
        log.info("Set GroupGotPicture Cache:" + gId);
        redisUtil.set(getFormatKey(bId, RedisActionType.Group_Got_Picture, gId), null, 60*5);
    }

    public boolean isGroupGotPicture(Long bId, Long gId) {
        return redisUtil.hasKey(getFormatKey(bId, RedisActionType.Group_Got_Picture, gId));
    }

    public void cacheGroupSearchedPicture(Long bId, Long gId) {
        log.info("Set GroupSearchPicture Cache:" + gId);
        redisUtil.set(getFormatKey(bId, RedisActionType.Group_Searched_Picture, gId), null, 60*10);
    }

    public boolean isGroupSearchedPicture(Long bId, Long gId) {
        return redisUtil.hasKey(getFormatKey(bId, RedisActionType.Group_Searched_Picture, gId));
    }

    public String getHashItem(String key, String item) {
        return String.valueOf(redisUtil.hashGet(key, item));
    }

    public Map<Object, Object> getHash(String key) {
        return redisUtil.hashGetAll(key);
    }

    public void deleteCache(String key) {
        redisUtil.delete(key);
    }

    /**
     * 返回格式化的Key
     * 例如 FurBot:{BotId}:UnconfirmedPMember:{ObjectId}
     * @param bId Bot的ID（QQ号）
     * @param action FurBot:{BotId}:{action}:{oId}
     * @param oId FurBot:{BotId}:{action}:{oId}
     * @return key
     */
    private String getFormatKey(Long bId, RedisActionType action, Long oId) {
        return "FurBot:" +
                bId +
                ":" +
                action.getActionName() +
                ":" +
                oId;
    }
}
