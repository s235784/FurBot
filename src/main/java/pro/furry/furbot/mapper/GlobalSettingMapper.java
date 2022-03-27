package pro.furry.furbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import pro.furry.furbot.pojo.db.GlobalSetting;

/**
 * @author NuoTian
 * @date 2022/3/27
 */
@Mapper
public interface GlobalSettingMapper extends BaseMapper<GlobalSetting> {
}
