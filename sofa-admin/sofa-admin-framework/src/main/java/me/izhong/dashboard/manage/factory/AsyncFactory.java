package me.izhong.dashboard.manage.factory;

import eu.bitwalker.useragentutils.UserAgent;
import me.izhong.dashboard.manage.constants.SystemConstants;
import me.izhong.dashboard.manage.entity.SysLoginInfo;
import me.izhong.dashboard.manage.entity.SysOperLog;
import me.izhong.dashboard.manage.entity.SysUserOnline;
import me.izhong.dashboard.manage.security.session.OnlineSession;
import me.izhong.dashboard.manage.service.SysLoginInfoService;
import me.izhong.dashboard.manage.service.SysOperLogService;
import me.izhong.dashboard.manage.service.SysUserOnlineService;
import me.izhong.dashboard.manage.util.IpUtil;
import me.izhong.dashboard.manage.util.LogUtil;
import me.izhong.dashboard.manage.util.ServletUtil;
import me.izhong.dashboard.manage.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimerTask;


public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 同步session到数据库
     *
     * @param session 在线用户会话
     * @return 任务task
     */
    public static TimerTask syncSessionToDb(final OnlineSession session) {
        return new TimerTask() {
            @Override
            public void run() {
                SysUserOnline online = new SysUserOnline();
                online.setSessionId(String.valueOf(session.getId()));
                online.setDeptName(session.getDeptName());
                online.setLoginName(session.getLoginName());
                online.setStartTimestamp(session.getStartTimestamp());
                online.setLastAccessTime(session.getLastAccessTime());
                online.setExpireTime(session.getTimeout());
                online.setIpAddr(session.getHost());
                online.setLoginLocation(IpUtil.getRealAddressByIP(session.getHost()));
                online.setBrowser(session.getBrowser());
                online.setOs(session.getOs());
                online.setStatus(session.getStatus());
                SpringUtil.getBean(SysUserOnlineService.class).saveOnline(online);

            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param sysOperLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final SysOperLog sysOperLog) {
        return new TimerTask() {
            @Override
            public void run() {
                // 远程查询操作地点
                sysOperLog.setOperLocation(IpUtil.getRealAddressByIP(sysOperLog.getOperIp()));
                SpringUtil.getBean(SysOperLogService.class).insert(sysOperLog);
            }
        };
    }

    public static TimerTask recordLoginInfo(final String username, final String status, final String message, final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtil.getRequest().getHeader("User-Agent"));
        final String ip = IpUtil.getIpAddr(ServletUtil.getRequest());
        return new TimerTask() {
            @Override
            public void run() {
                StringBuilder s = new StringBuilder();
                s.append(LogUtil.getBlock(ip));
                s.append(IpUtil.getRealAddressByIP(ip));
                s.append(LogUtil.getBlock(username));
                s.append(LogUtil.getBlock(status));
                s.append(LogUtil.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLoginInfo logininfor = new SysLoginInfo();
                logininfor.setLoginName(username);
                logininfor.setIpAddr(ip);
                logininfor.setLoginLocation(IpUtil.getRealAddressByIP(ip));
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                logininfor.setLoginTime(new Date());
                // 日志状态
                if (SystemConstants.SUCCESS.equals(status) || SystemConstants.LOGOUT.equals(status)) {
                    logininfor.setStatus(SystemConstants.SUCCESS);
                } else if (SystemConstants.FAIL.equals(status)) {
                    logininfor.setStatus(SystemConstants.FAIL);
                }
                // 插入数据
                SpringUtil.getBean(SysLoginInfoService.class).insert(logininfor);
            }
        };
    }
}
