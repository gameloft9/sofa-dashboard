package me.izhong.dashboard.manage.expection;

import me.izhong.db.common.exception.BusinessException;

/**
 * 验证码错误异常类
 */
public class CaptchaException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public CaptchaException() {
        super("user.jcaptcha.error", null);
    }
}
