package pro.furry.furbot.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author NuoTian
 * @date 2022/3/27
 */
@Getter
@AllArgsConstructor
public enum ApiType {
    Hibi_Pixiv_Member("hibi_pixiv_member"),
    Hibi_Pixiv_Member_Illust("hibi_pixiv_member_illust"),
    Hibi_Sauce_From("hibi_sauce_from"),
    Bot_Picture("bot_picture");
    private final String api_name;
}
