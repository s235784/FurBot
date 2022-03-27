package pro.furry.furbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import pro.furry.furbot.pojo.db.ApiSetting;

/**
 * @author NuoTian
 * @date 2022/3/26
 */
@Mapper
public interface ApiSettingMapper extends BaseMapper<ApiSetting> {
}
