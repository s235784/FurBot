package pro.furry.furbot.service;

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
public class SuAdminService {
    private DBService dbService;

    @Autowired
    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }

    public boolean isSuperAdmin(Long uId) {
        return String.valueOf(uId).equals(dbService.getGlobalSetting(GlobalSettingType.Super_Admin_QQ));
    }

    @Nullable
    public Long getSuperAdmin() {
        return PublicUtil.parseLong(dbService.getGlobalSetting(GlobalSettingType.Super_Admin_QQ));
    }
}
