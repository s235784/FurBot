package pro.furry.furbot.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.furry.furbot.type.GroupSettingType;

/**
 * @author NuoTian
 * @date 2022/3/28
 */
@Slf4j
@Service
public class GroupSettingService {
    private DBService dbService;

    @Autowired
    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }

    public void changeGroupSetting(MessageEvent event, String name, String value) {
        Long gid = event.getSubject().getId();
        GroupSettingType type = GroupSettingType.getInstance(name);
        if (type == null) {
            event.getSubject().sendMessage("没有这个设置呢");
            return;
        }
        if (Boolean.class.isAssignableFrom(type.getValueType())) {
            if (value.equals("开") || value.equalsIgnoreCase("true")) {
                dbService.setGroupSetting(gid, type, "true");
            } else if (value.equals("关") || value.equalsIgnoreCase("false")) {
                dbService.setGroupSetting(gid, type, "false");
            } else {
                event.getSubject().sendMessage("该设置的参数只能是 开/关/true/false 呢");
            }
        } else {
            // 还需要对Value判断是否合法
            // todo
            dbService.setGroupSetting(gid, type, value);
        }
        event.getSubject().sendMessage("设置已更新");
    }
}
