package pro.furry.furbot.service;

import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.furry.furbot.type.GlobalSettingType;
import pro.furry.furbot.util.PublicUtil;

/**
 * @author NuoTian
 * @date 2022/3/27
 */
@Service
public class AdminService {
    private DBService dbService;

    @Autowired
    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }

    public boolean isSuperAdmin(Long uId) {
        return String.valueOf(uId).equals(dbService.getGlobalSetting(GlobalSettingType.Super_Admin_QQ));
    }

    public boolean isAdmin(@NotNull GroupMessageEvent event) {
        Long uId = event.getSender().getId();
        return event.getSender().getPermission() == MemberPermission.ADMINISTRATOR ||
                event.getSender().getPermission() == MemberPermission.OWNER ||
                isSuperAdmin(uId);
    }

    @Nullable
    public Long getSuperAdmin() {
        return PublicUtil.parseLong(dbService.getGlobalSetting(GlobalSettingType.Super_Admin_QQ));
    }
}
