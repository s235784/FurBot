package pro.furry.furbot.pojo.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author NuoTian
 * @date 2022/3/28
 */
@Data
@TableName("group_setting")
public class GroupSetting {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("group_id")
    private String groupId;
    @TableField("setting_name")
    private String settingName;
    @TableField("setting_value")
    private String settingValue;
}
