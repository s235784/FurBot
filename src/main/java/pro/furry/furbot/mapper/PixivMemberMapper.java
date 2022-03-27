package pro.furry.furbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import pro.furry.furbot.pojo.db.PixivMember;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
@Mapper
public interface PixivMemberMapper extends BaseMapper<PixivMember> {
}
