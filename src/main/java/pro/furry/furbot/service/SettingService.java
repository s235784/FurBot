package pro.furry.furbot.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.furry.furbot.exception.LocalException;
import pro.furry.furbot.type.GlobalSettingType;
import pro.furry.furbot.type.GroupSettingType;
import pro.furry.furbot.util.PublicUtil;

/**
 * @author NuoTian
 * @date 2022/3/28
 */
@Slf4j
@Service
public class SettingService {
    private DBService dbService;

    @Autowired
    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }

    public void setSetting(MessageEvent event, String @NotNull [] parameters) {
        GroupSettingType groupSettingType = GroupSettingType.getInstance(parameters[0]);
        if (groupSettingType == null) {
            event.getSubject().sendMessage("没有这个设置呢");
            return;
        }
        // 判断设置的类型
        switch (groupSettingType.getReceiveType()) {
            case User:
                if (!(event.getSubject() instanceof User)) {
                    event.getSubject().sendMessage("没有这个设置呢");
                    return;
                }
                break;
            case Group:
                if (!(event.getSubject() instanceof Group)) {
                    event.getSubject().sendMessage("没有这个设置呢");
                    return;
                }
                break;
        }
        try {
            switch (groupSettingType) {
                case Show_R18_Content:
                    if (parameters.length != 2) {
                        event.getSubject().sendMessage("参数格式错误！");
                        return;
                    }
                    setGroupSetting(event.getSubject().getId(), groupSettingType, parameters[1]);
                    break;
                case Enable_Bot:
                case Picture_Time_Limit:
                    if (parameters.length != 3) {
                        event.getSubject().sendMessage("参数格式错误！");
                        return;
                    }
                    Long gId = PublicUtil.parseLong(parameters[1]);
                    if (gId == null) {
                        event.getSubject().sendMessage("群号格式错误");
                        return;
                    }
                    setGroupSetting(gId, groupSettingType, parameters[2]);
                    break;
            }
            event.getSubject().sendMessage("设置已更新");
        } catch (LocalException e) {
            event.getSubject().sendMessage(e.getMessage());
        }
    }

    private void setGroupSetting(@NotNull Long gId, @NotNull GroupSettingType type, String value) throws LocalException {
        if (Boolean.class.isAssignableFrom(type.getValueType())) {
            if (value.equals("开") || value.equalsIgnoreCase("true")) {
                dbService.setGroupSetting(gId, type.getSettingName(), "true");
            } else if (value.equals("关") || value.equalsIgnoreCase("false")) {
                dbService.setGroupSetting(gId, type.getSettingName(), "false");
            } else {
                throw new LocalException("该设置的参数只能是 开/关/true/false");
            }
        } else if (Long.class.isAssignableFrom(type.getValueType())) {
            if (PublicUtil.parseLong(value) != null) {
                dbService.setGroupSetting(gId, type.getSettingName(), value);
            } else {
                throw new LocalException("该设置的参数只能是数字");
            }
        } else {
            // 还需要对Value判断是否合法
            // todo
            dbService.setGroupSetting(gId, type.getSettingName(), value);
        }
    }

    public String getGroupSetting(Long gId, @NotNull GroupSettingType setting) {
        String value = dbService.getGroupSetting(gId, setting.getSettingName());
        return value == null ? setting.getDefaultValue() : value;
    }

    public Long getGroupSettingAsLong(Long gId, @NotNull GroupSettingType setting) {
        String valueStr = getGroupSetting(gId, setting);
        Long value = PublicUtil.parseLong(valueStr);
        if (value == null) {
            throw new LocalException("数据库数据不合法");
        } else {
            return value;
        }
    }

    public Boolean getGroupSettingAsBoolean(Long gId, @NotNull GroupSettingType setting) {
        return "true".equals(getGroupSetting(gId, setting));
    }

    public String getGlobalSetting(@NotNull GlobalSettingType setting) {
        return dbService.getGlobalSetting(setting);
    }
}
