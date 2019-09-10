package me.izhong.dashboard.manage.expection;

public class RoleBlockedException extends BusinessException {


    public static final String KEY = "ROLE_BLOCK";

    public RoleBlockedException() {
        super(KEY, "角色已禁用");
    }

    public RoleBlockedException(String message) {
        super(KEY, message);
    }
}
