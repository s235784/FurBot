package pro.furry.furbot.pojo.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author NuoTian
 * @date 2022/3/26
 */
@Data
@TableName("api_setting")
public class ApiSetting {
    @TableId(value="id", type= IdType.AUTO)
    private Integer id;
    @TableField("api_name")
    private String apiName;
    @TableField("api_url")
    private String apiURL;
}
