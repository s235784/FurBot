package pro.furry.furbot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
@Data
@AllArgsConstructor
public class ReceiveParameter {
    private String[] Parameters;

    public static ReceiveParameter getParameterFromContext(String context, String frontText) {
        String str = context.replace(frontText, "").trim();
        String[] parameters = str.isEmpty() ? new String[]{} : str.split(" ");
        return new ReceiveParameter(parameters);
    }
}
