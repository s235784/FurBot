package pro.furry.furbot.pojo.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author NuoTian
 * @date 2022/3/27
 */
@Data
@TableName("global_setting")
public class GlobalSetting {
    @TableId(value="id", type= IdType.AUTO)
    private Integer id;
    @TableField("setting_name")
    private String settingName;
    @TableField("setting_value")
    private String settingValue;
}
