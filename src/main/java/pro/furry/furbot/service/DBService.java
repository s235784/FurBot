package pro.furry.furbot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.furry.furbot.exception.LocalException;
import pro.furry.furbot.mapper.*;
import pro.furry.furbot.pojo.db.*;
import pro.furry.furbot.type.ApiType;
import pro.furry.furbot.type.GlobalSettingType;
import pro.furry.furbot.util.PublicUtil;

import java.util.List;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
@Slf4j
@Service
public class DBService {

    private ApiSettingMapper apiSettingMapper;
    private PixivMemberMapper pixivMemberMapper;
    private GroupSettingMapper groupSettingMapper;
    private GlobalSettingMapper globalSettingMapper;
    private GroupSettingPixivMapper groupSettingPixivMapper;

    @Autowired
    public void setApiSettingMapper(ApiSettingMapper apiSettingMapper) {
        this.apiSettingMapper = apiSettingMapper;
    }

    @Autowired
    public void setPixivMemberMapper(PixivMemberMapper pixivMemberMapper) {
        this.pixivMemberMapper = pixivMemberMapper;
    }

    @Autowired
    public void setGroupSettingMapper(GroupSettingMapper groupSettingMapper) {
        this.groupSettingMapper = groupSettingMapper;
    }

    @Autowired
    public void setGlobalSettingMapper(GlobalSettingMapper globalSettingMapper) {
        this.globalSettingMapper = globalSettingMapper;
    }

    @Autowired
    public void setGroupSettingPixivMapper(GroupSettingPixivMapper groupSettingPixivMapper) {
        this.groupSettingPixivMapper = groupSettingPixivMapper;
    }

    public void addPixivMember(Long gId, Long pId, String account, String name) {
        log.info("向群聊 {} 添加画师 {}", gId, pId);
        PixivMember member = new PixivMember();
        member.setPixivId(pId);
        member.setPixivAccount(account);
        member.setPixivName(name);
        QueryWrapper<PixivMember> pixivMemberQueryWrapper = new QueryWrapper<>();
        pixivMemberQueryWrapper.eq("pixiv_id", pId);
        if (pixivMemberMapper.exists(pixivMemberQueryWrapper)) {
            pixivMemberMapper.update(member, pixivMemberQueryWrapper);
        } else {
            pixivMemberMapper.insert(member);
        }

        QueryWrapper<GroupSettingPixiv> groupSettingPixivQueryWrapper = new QueryWrapper<>();
        groupSettingPixivQueryWrapper.eq("group_id", gId)
                .eq("pixiv_id", pId);
        if (!groupSettingPixivMapper.exists(groupSettingPixivQueryWrapper)) {
            GroupSettingPixiv settingPixiv = new GroupSettingPixiv();
            settingPixiv.setGroupId(gId);
            settingPixiv.setPixivId(pId);
            groupSettingPixivMapper.insert(settingPixiv);
        }
    }

    public void deletePixivMember(Long gId, Long pId) {
        QueryWrapper<GroupSettingPixiv> pixivQueryWrapper = new QueryWrapper<>();
        pixivQueryWrapper.eq("group_id", gId)
                .eq("pixiv_id", pId);
        groupSettingPixivMapper.delete(pixivQueryWrapper);
    }

    public boolean isGroupAddedPMember(Long gId, Long pId) {
        QueryWrapper<GroupSettingPixiv> groupSettingPixivQueryWrapper = new QueryWrapper<>();
        groupSettingPixivQueryWrapper.eq("group_id", gId)
                .eq("pixiv_id", pId);
        return groupSettingPixivMapper.exists(groupSettingPixivQueryWrapper);
    }

    public boolean isGroupHasPMember(Long gId) {
        QueryWrapper<GroupSettingPixiv> groupSettingPixivQueryWrapper = new QueryWrapper<>();
        groupSettingPixivQueryWrapper.eq("group_id", gId);
        return groupSettingPixivMapper.exists(groupSettingPixivQueryWrapper);
    }

    public Long getRandPMember(Long gId) {
        QueryWrapper<GroupSettingPixiv> groupSettingPixivQueryWrapper = new QueryWrapper<>();
        groupSettingPixivQueryWrapper.eq("group_id", gId);
        List<GroupSettingPixiv> list = groupSettingPixivMapper.selectList(groupSettingPixivQueryWrapper);
        int randIndex = PublicUtil.getRandInt(0, list.size() - 1);
        return list.get(randIndex).getPixivId();
    }

    public Long getPMemberCount(Long gId) {
        QueryWrapper<GroupSettingPixiv> groupSettingPixivQueryWrapper = new QueryWrapper<>();
        groupSettingPixivQueryWrapper.eq("group_id", gId);
        return groupSettingPixivMapper.selectCount(groupSettingPixivQueryWrapper);
    }

    public PixivMember getPixivMemberInfo(Long pId) {
        QueryWrapper<PixivMember> pixivMemberQueryWrapper = new QueryWrapper<>();
        pixivMemberQueryWrapper.eq("pixiv_id", pId);
        return pixivMemberMapper.selectOne(pixivMemberQueryWrapper);
    }

    public String getApiURL(@NotNull ApiType apiName) throws LocalException {
        QueryWrapper<ApiSetting> apiSettingQueryWrapper = new QueryWrapper<>();
        apiSettingQueryWrapper.eq("api_name", apiName.getApi_name());
        ApiSetting apiSetting = apiSettingMapper.selectOne(apiSettingQueryWrapper);
        if (apiSetting == null || apiSetting.getApiURL() == null) {
            throw new LocalException("相关API未配置");
        } else {
            return apiSetting.getApiURL();
        }
    }

    public void setApiURL(@NotNull ApiType apiName, @NotNull String url) {
        QueryWrapper<ApiSetting> apiSettingQueryWrapper = new QueryWrapper<>();
        apiSettingQueryWrapper.eq("api_name", apiName.getApi_name());
        ApiSetting apiSetting = new ApiSetting();
        apiSetting.setApiName(apiName.getApi_name());
        apiSetting.setApiURL(url);
        if (apiSettingMapper.exists(apiSettingQueryWrapper)) {
            apiSettingMapper.update(apiSetting, apiSettingQueryWrapper);
        } else {
            apiSettingMapper.insert(apiSetting);
        }
    }

    public Page<GroupSettingPixiv> getGroupPMemberByPage(Long gId, int page) {
        log.info("Get Pixiv Member by Page, gId: {}, Page: {}", gId, page);

        QueryWrapper<GroupSettingPixiv> groupSettingPixivQueryWrapper = new QueryWrapper<>();
        groupSettingPixivQueryWrapper.eq("group_id", String.valueOf(gId));

        Page<GroupSettingPixiv> groupSettingPixivPage = new Page<>(page,10);
        groupSettingPixivMapper.selectPage(groupSettingPixivPage, groupSettingPixivQueryWrapper);

        return groupSettingPixivPage;
    }

    public PixivMember getPixivMember(Long pId) throws LocalException {
        QueryWrapper<PixivMember> pixivMemberQueryWrapper = new QueryWrapper<>();
        pixivMemberQueryWrapper.eq("pixiv_id", String.valueOf(pId));

        PixivMember pixivMember = pixivMemberMapper.selectOne(pixivMemberQueryWrapper);
        if (pixivMember == null) {
            throw new LocalException("数据库数据不完整");
        }
        return pixivMember;
    }

    @Nullable
    public String getGlobalSetting(@NotNull GlobalSettingType setting) {
        QueryWrapper<GlobalSetting> globalSettingQueryWrapper = new QueryWrapper<>();
        globalSettingQueryWrapper.eq("setting_name", setting.getSettingName());
        GlobalSetting globalSetting = globalSettingMapper.selectOne(globalSettingQueryWrapper);
        if (globalSetting == null || globalSetting.getSettingValue() == null) {
            return null;
        } else {
            return globalSetting.getSettingValue();
        }
    }

    public void setGlobalSetting(@NotNull GlobalSettingType setting, String value) {
        QueryWrapper<GlobalSetting> globalSettingQueryWrapper = new QueryWrapper<>();
        globalSettingQueryWrapper.eq("setting_name", setting.getSettingName());
        GlobalSetting globalSetting = new GlobalSetting();
        globalSetting.setSettingName(setting.getSettingName());
        globalSetting.setSettingValue(value);
        if (globalSettingMapper.exists(globalSettingQueryWrapper)) {
            globalSettingMapper.update(globalSetting, globalSettingQueryWrapper);
        } else {
            globalSettingMapper.insert(globalSetting);
        }
    }

    public String getGroupSetting(Long gId, String name) {
        QueryWrapper<GroupSetting> groupSettingQueryWrapper = new QueryWrapper<>();
        groupSettingQueryWrapper.eq("group_id", String.valueOf(gId))
                .eq("setting_name", name);
        GroupSetting groupSetting = groupSettingMapper.selectOne(groupSettingQueryWrapper);
        return groupSetting == null ? null : groupSetting.getSettingValue();
    }

    public void setGroupSetting(Long gId, String name, String value) {
        log.info("Set Group, Name: {}, Id: {}, Value: {}", name, gId, value);
        QueryWrapper<GroupSetting> groupSettingQueryWrapper = new QueryWrapper<>();
        groupSettingQueryWrapper.eq("group_id", String.valueOf(gId))
                .eq("setting_name", name);
        GroupSetting groupSetting = new GroupSetting();
        groupSetting.setGroupId(String.valueOf(gId));
        groupSetting.setSettingName(name);
        groupSetting.setSettingValue(value);
        if (groupSettingMapper.exists(groupSettingQueryWrapper)) {
            groupSettingMapper.update(groupSetting, groupSettingQueryWrapper);
        } else {
            groupSettingMapper.insert(groupSetting);
        }
    }
}
