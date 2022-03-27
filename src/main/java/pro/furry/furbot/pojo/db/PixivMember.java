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
@TableName("pixiv_member")
public class PixivMember {
    @TableId(value="id", type= IdType.AUTO)
    private Integer id;
    @TableField("pixiv_id")
    private Long pixivId;
    @TableField("pixiv_account")
    private String pixivAccount;
    @TableField("pixiv_name")
    private String pixivName;
}
