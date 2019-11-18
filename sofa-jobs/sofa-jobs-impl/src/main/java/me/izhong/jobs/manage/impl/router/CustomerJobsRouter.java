package me.izhong.jobs.manage.impl.router;

import com.alipay.sofa.rpc.bootstrap.ConsumerBootstrap;
import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.client.Router;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.manage.IJobAgentMngFacade;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.manage.impl.core.model.XxlJobLog;
import me.izhong.jobs.manage.impl.core.util.SpringUtil;
import me.izhong.jobs.manage.impl.service.XxlJobInfoService;
import me.izhong.jobs.manage.impl.service.XxlJobLogService;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Extension(value = "customerRouter")
@AutoActive(consumerSide = true)
@Slf4j
public class CustomerJobsRouter extends Router {

    @Override
    public void init(ConsumerBootstrap consumerBootstrap) {

    }

    @Override
    public boolean needToLoad(ConsumerBootstrap consumerBootstrap) {
        String inerfaceId = consumerBootstrap.getConsumerConfig().getInterfaceId();
        if(StringUtils.equals(inerfaceId,IJobAgentMngFacade.class.getName())) {
            log.info("begin effect");
            return true;
        }
        return false;
    }

    @Override
    public List<ProviderInfo> route(SofaRequest request, List<ProviderInfo> providerInfos) {

        try {
            String interfaceName = request.getInterfaceName();
            String method = request.getMethod().getName();
            XxlJobInfoService jobInfoService = SpringUtil.getBean(XxlJobInfoService.class);
            XxlJobLogService jobLogService = SpringUtil.getBean(XxlJobLogService.class);
            log.info("路由地址: interfaceName:{} method:{}  {}", interfaceName, method, providerInfos);
            if ("trigger".equals(method)) {
                int providerSize = providerInfos.size();
                int randInt = RandomUtils.nextInt() % providerSize;
                ProviderInfo pi = providerInfos.get(randInt);
                log.info("按照路由策略执行trigger,总共{}选择{}地址是{}",providerSize,randInt,pi.getHost());

                Object[] args = request.getMethodArgs();
                Long jobId = (Long) args[0];
                Long triggerId = (Long) args[1];
                jobInfoService.selectByPId(jobId);

                jobLogService.updateExecutorAddress(triggerId, pi.getHost());

                return new ArrayList<ProviderInfo>() {{
                    add(pi);
                }};

            } else if (StringUtils.equalsAny(method, "catLog", "kill")) {
                log.info("按照路由策略执行method:{}",method);

                Object[] args = request.getMethodArgs();
                Long jobId = (Long) args[0];
                Long triggerId = (Long) args[1];
                jobInfoService.selectByPId(jobId);

                XxlJobLog jobLog = jobLogService.selectByPId(triggerId);
                if (jobLog == null) {
                    log.info("任务{} 日志没有找到 {}", jobId, triggerId);
                    throw new RuntimeException("任务" + jobId + "日志没有找到 " + triggerId);
                }
                String jobAddress = jobLog.getExecutorAddress();
                if (StringUtils.isBlank(jobAddress)) {
                    log.info("任务{} 日志{} 的执行地址为空", jobId, triggerId);
                }
                return providerInfos.stream().filter(e -> e.getHost().equals(jobAddress)).collect(toList());
            }
        }catch (Exception e) {
            log.error("route err:", e);
        }
        return providerInfos;
    }
}