package me.izhong.jobs.agent.job;

import com.alibaba.fastjson.JSON;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.DateUtil;
import me.izhong.jobs.agent.bean.JobContext;
import me.izhong.jobs.agent.bean.JobsConfigBean;
import me.izhong.jobs.agent.exp.JobExecutionException;
import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.agent.util.ContextUtil;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.model.ReturnT;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行script脚本的定时任务，通过run.sh调起来
 * 日志通过dubbo上送到统一调度平台
 */
@Slf4j
public class ShellCommandRunJob extends IJobHandler {

    private String SHName = "/bin/sh";
    private String command = "run.sh";


    @Override
    public ReturnT<String> execute(JobContext jobContext) throws Exception {
        Long jobId = jobContext.getJobId();
        Long triggerId = jobContext.getTriggerId();

        String run_env = ContextUtil.getRunEnv();
        JobsConfigBean configBean = ContextUtil.getBean(JobsConfigBean.class);
        JobServiceReference facade = ContextUtil.getBean(JobServiceReference.class);
        IJobMngFacade jobMng = facade.getJobMngFacade();

        String scriptDir = configBean.getScriptPath();

        Map<String, String> envs = new HashMap<>();
        envs.put("os","win");

        Map<String, String> params = jobContext.getParams();

        log.info("shell command job[{}]  trigger [{}] timeout:{}  envs:{} param: {}",
                jobId, jobContext.getTriggerId(),jobContext.getTimeout(), envs, params);

        try {
            if (params == null)
                params = new HashMap<>();
            long timeout = jobContext.getTimeout();

            String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String envParams = JSON.toJSONString(envs);
            String execParams = JSON.toJSONString(params);
            log.info("开始任务:serverName={} command={},envParams={}, execParams={}, timeout={}",
                    ContextUtil.getServerName(), command, envParams, execParams, timeout);

            if (StringUtils.isBlank(command)) {
                throw new JobExecutionException("参数错误");
            }

            String shellCommand = SHName + " " + scriptDir
                    + "/"+ command
                    + " -DisJobAgent=true -DscriptType=groovy -DsTime=" + dateTime
                    + " -DjobId=" + jobId + " -DtriggerId=" + triggerId
                    + " -Denvs=" + envParams + " -Dparams=" + execParams;
            log.info("shellCommand:{}", shellCommand);

            DefaultExecutor shellExecutor = new DefaultExecutor();

            ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
            shellExecutor.setWatchdog(watchdog);

            //正常退出状态码
            shellExecutor.setExitValue(0);
            log.info("系统日志目录:{}",configBean.getLogDir());
            File logFile = new File(configBean.getLogDir());
            if(!logFile.exists()) {
                throw new JobExecutionException("日志文件路径不存在:"+configBean.getLogDir());
            }
            String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
            logFile = new File(configBean.getLogDir() + File.separator + dateString + File.separator + +jobId + "_" + triggerId + ".txt");

            File parentDir = logFile.getParentFile();
            if(!parentDir.exists()){
                parentDir.mkdirs();
            }
            if(!logFile.exists()) {
                log.info("文件不存在 {} 创建",logFile.getAbsolutePath());
                boolean isSc = logFile.createNewFile();
                if(!isSc){
                    log.info("文件创建失败");
                }
            }

            @Cleanup
            FileOutputStream fos = new FileOutputStream(logFile);
            PumpStreamHandler streamHandler = new PumpStreamHandler(fos);
            shellExecutor.setStreamHandler(streamHandler);

            Date startTime = new Date();
            log.info("run.sh任务开始执行, 上送执行时间:triggerId:{} startTime:{}",triggerId,DateUtil.dateTime(startTime));

            jobMng.uploadJobStartStatics(triggerId,startTime);

            CommandLine cmdLine = CommandLine.parse(shellCommand);
            int exitValue = shellExecutor.execute(cmdLine);
            log.info("run.sh任务结束了: triggerId:{} exitValue:{} 上送结果信息",triggerId,exitValue);
            //记录执行状态信息
            jobMng.uploadJobEndStatics(triggerId,new Date(), exitValue, exitValue == 0 ? "执行成功":"执行失败");

            return ReturnT.SUCCESS;
        } catch (Throwable e) {
            log.error("任务失败 triggerId:{}",triggerId, e);
            jobMng.uploadJobEndStatics(triggerId,new Date(), 254, "执行异常:" + e.getMessage());
            return ReturnT.FAIL;
        } finally {

        }
    }

}
