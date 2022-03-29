package pro.furry.furbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import pro.furry.furbot.pojo.db.GroupSetting;

/**
 * @author NuoTian
 * @date 2022/3/28
 */
@Mapper
public interface GroupSettingMapper extends BaseMapper<GroupSetting> {
}
