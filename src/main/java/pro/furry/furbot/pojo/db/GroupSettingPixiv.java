package pro.furry.furbot.pojo.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
@Data
@TableName("group_setting_pixiv")
public class GroupSettingPixiv {
    @TableId(value="id", type= IdType.AUTO)
    private Integer id;
    @TableField("group_id")
    private Long groupId;
    @TableField("pixiv_id")
    private Long pixivId;
}
