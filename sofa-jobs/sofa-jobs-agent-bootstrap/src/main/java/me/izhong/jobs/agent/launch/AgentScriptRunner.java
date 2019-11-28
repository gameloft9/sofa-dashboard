package me.izhong.jobs.agent.launch;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.agent.job.IBatch;
import me.izhong.jobs.agent.job.context.ScriptRunContext;
import me.izhong.jobs.agent.log.AgentLog;
import me.izhong.jobs.agent.log.ConsoleLog;
import me.izhong.jobs.agent.log.RemoteLog;
import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.agent.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.model.JobLog;
import me.izhong.jobs.model.JobScript;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AgentScriptRunner implements ApplicationRunner {

    @Autowired
    private JobServiceReference jobServiceReference;

    @Autowired
    private List<IBatch> batchs;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        AgentLog agentLog = null;
        try {
            //只有run.sh 才会调度到这里
            String scriptType = System.getProperty("scriptType");
            //String scriptType = StringUtil.firstValue(args.getOptionValues("scriptType"));
            if (StringUtils.isNotBlank(scriptType)) {
                log.info("运行run.sh scriptType:{}", scriptType);
                Long jobId = Long.valueOf(System.getProperty("jobId"));
                if (jobId == null) {
                    throw new RuntimeException("jobId不能为空");
                }
                Long triggerId = Long.valueOf(System.getProperty("triggerId"));
                if (triggerId == null) {
                    throw new RuntimeException("triggerId不能为空");
                }
                IJobMngFacade facade = jobServiceReference.getJobMngFacade();
                if(facade == null) {
                    log.info("IJobMngFacade 不能为空");
                }
                String loggerString = System.getProperty("logger");
                if (StringUtils.equalsIgnoreCase(loggerString, "console")) {
                    agentLog = new ConsoleLog();
                } else {
                    agentLog = new RemoteLog(jobId, triggerId);
                }

                JobScript jobScript = facade.findCurrentJobScriptByJobId(jobId);
                if(jobScript == null) {
                    log.info("任务{}的Script未找到，检查是否配置了脚本",jobId );
                    System.exit(2);
                    throw new Exception("任务"+jobId+"的Script未找到，检查是否配置了脚本");
                }
                JobLog jobLog = facade.findJobLogByJobLogId(triggerId);
                if(jobLog == null) {
                    log.info("任务{}的JobLog未找到",jobId );
                    throw new Exception("任务"+jobId+"的JobLog未找到");
                }
                String script = jobScript.getScript();
                log.info("脚本内容:{}", script);

//                String envsString = System.getProperty("envs");
//                log.info("envs:{}", envsString);
//                Map<String, String> envs ;
//                if(StringUtils.isNotBlank(envsString)) {
//                    if(envsString.startsWith("{\"")) {
//                        envs = JSONObject.parseObject(envsString, new TypeReference<Map<String, String>>() {
//                        });
//                    } else if(envsString.startsWith("{")) {
//                        envs = StringUtil.parseParams(envsString);
//                    } else {
//                        log.info("无法解析参数");
//                        return;
//                    }
//                } else {
//                    envs = new HashMap<>();
//                }

                String paramsString = jobLog.getExecutorParam();
                //String paramsString = System.getProperty("params");
                log.info("params:{}", paramsString);
                Map<String, String> params = new HashMap<>();;
                if(StringUtils.isNotBlank(paramsString)) {
                    if(paramsString.startsWith("{\"")) {
                        params = JSONObject.parseObject(paramsString, new TypeReference<Map<String, String>>() {
                        });
                    } else if(paramsString.startsWith("{")) {
                        params = StringUtil.parseParams(paramsString);
                    } else {
                        log.info("无法解析参数");
                        System.exit(3);
                    }
                }
                //log.info("params2:{}",params);

                //====初始化运行环境===
                ScriptRunContext context = new ScriptRunContext();
                context.setJobId(jobId);
                context.setScript(script);
                try {
                    context.setTimeout(Long.valueOf(System.getProperty("timeout")));
                } catch (Exception e) {
                    context.setTimeout(-1);
                }
                context.setLog(agentLog);
                //context.setEnvs(envs);
                context.setParams(params);

                int code = 0;
                boolean find = false;
                for (IBatch b : batchs) {
                    if (StringUtils.equalsIgnoreCase(scriptType, b.scriptType())) {
                        find = true;
                        code = b.execute(context);
                        break;
                    }
                }
                if (!find) {
                    agentLog.info("没有找到对应的脚本类型");
                }
                agentLog.info("run 运行结束 code:{}", code);
                Thread.sleep(3000);
                System.exit(code);
            }
        } catch (Exception e) {
            if(agentLog != null) {
                agentLog.info("run 异常运行结束" + e.getMessage());
            }
            log.info("run 异常运行结束", e);
            Thread.sleep(3000);
            System.exit(255);
        }
        log.info("run 执行到最后结束");
    }
}
