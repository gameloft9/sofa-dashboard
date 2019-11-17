package me.izhong.dashboard.agent.test;


import com.alipay.sofa.registry.client.api.ConfigDataObserver;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.model.ConfigData;
import com.alipay.sofa.registry.client.api.registration.ConfiguratorRegistration;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClient;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import me.izhong.jobs.agent.JobsAgentApplicationRunner;
import me.izhong.jobs.agent.bean.JobsConfigBean;
import me.izhong.jobs.agent.job.ExecGrooyScript;
import me.izhong.jobs.agent.job.context.ScriptRunContext;
import me.izhong.jobs.agent.log.ConsoleLog;
import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.agent.util.ContextUtil;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.model.JobGroup;
import me.izhong.jobs.model.JobStats;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
//@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("local")
@Slf4j
//@ContextConfiguration(
//        classes = { ContextUtil.class ,JobServiceReference.class,JobsAgentApplicationRunner.class},
//        initializers = {ConfigFileApplicationContextInitializer.class} )
//@TestPropertySource(properties = { "spring.config.location=classpath:application.yml" })
@ComponentScan(value = {"com.xuis,me.izhong"})
public class ScriptTest {

//    @Autowired
//    JobsConfigBean configBean;
//
//    @Autowired
//    JobServiceReference xx;

    @Test
	public void testXXX() throws Exception {

//        ConsumerConfig<IJobMngFacade> consumerConfig = new ConsumerConfig<IJobMngFacade>()
//                .setInterfaceId(IJobMngFacade.class.getName()) // 指定接口
//                .setProtocol("bolt") // 指定协议
//                .setUniqueId("1")
//                .setDirectUrl("bolt://10.10.51.212:13302"); // 指定直连地址
//        // 生成代理类
//        IJobMngFacade helloService = consumerConfig.refer();
//        List<JobGroup> gps = helloService.selectAllJobGroup();
//
//        log.info("",gps);

        // 构造发布者注册表，主要是指定dataInfoId和zone
        //PublisherRegistration registration = new PublisherRegistration("com.alipay.test.demo.service");
        //registration.setZone("ZoneA");

        RegistryClientConfig config =  DefaultRegistryClientConfigBuilder.start()
                .setRegistryEndpoint("10.10.51.212").setRegistryEndpointPort(2181).build();
        DefaultRegistryClient registryClient = new DefaultRegistryClient(config);
        registryClient.init();

        // 发布服务数据，dataList内容是 "10.10.1.1:12200?xx=yy"，即只有一个服务数据
        //registryClient.register(registration, "10.10.1.1:12200?xx=yy");

        ConfiguratorRegistration registration = new ConfiguratorRegistration("xxx", null);
        registration.setConfigDataObserver(new ConfigDataObserver() {
            @Override
            public void handleData(String dataId, ConfigData configData) {
                log.info("===========dataId:{} configData:{}",dataId,configData);

            }
        });
        registryClient.register(registration);

        Thread.sleep(300000);



//        log.info("==========test groovy..");
//
//        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
//        JobStats st = facade.findJobStatsByKey("");
//
//        String file = "D:\\space\\ums\\sofa-dashboard\\sofa-jobs\\sofa-jobs-agent-bootstrap\\src\\main\\groovy\\jobstats.groovy";
//
//        //初始化运行环境
//        ScriptRunContext context = new ScriptRunContext();
//        context.setJobId(0L);
//        context.setTriggerId(0L);
//        //传文件路径主要是为了断点
//        context.setScriptFile(new File(file));
//
//        context.setTimeout(-1);
//        context.setLog(new ConsoleLog());
//
//        Map<String, String> envs = new HashMap<>();
//        context.setEnvs(envs);
//        Map<String, String> params = new HashMap<>();
//        params.put("log","logv");
//        context.setParams(params);
//
//
//        ExecGrooyScript execGrooyScript = new ExecGrooyScript();
//        int r = execGrooyScript.execute(context);
//        log.info("执行返回{}",r);
    }
}
