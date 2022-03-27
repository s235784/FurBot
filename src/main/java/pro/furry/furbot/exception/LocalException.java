package pro.furry.furbot.exception;

import pro.furry.furbot.type.ErrorType;

/**
 * @author NuoTian
 * @date 2022/3/21
 */
public class LocalException extends RuntimeException {
    private ErrorType errorType;

    public LocalException(String msg) {
        super(msg);
    }

    public LocalException(ErrorType errorType) {
        super(errorType.getMsg());
    }

    public LocalException(String msg, Exception e) {
        super(msg, e);
    }
}
