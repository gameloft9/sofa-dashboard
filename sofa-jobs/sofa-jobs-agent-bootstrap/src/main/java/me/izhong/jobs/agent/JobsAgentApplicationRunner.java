package me.izhong.jobs.agent;

import me.izhong.jobs.agent.job.ExecGrooyScript;
import me.izhong.jobs.agent.job.IBatch;
import me.izhong.jobs.agent.job.context.ScriptRunContext;
import me.izhong.jobs.agent.log.ConsoleLog;
import me.izhong.jobs.agent.service.JobServiceReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import java.io.File;
import java.util.*;

@ImportResource({ "classpath*:rpc-sofa-boot-starter.xml" })
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        MongoAutoConfiguration.class,
        GroovyTemplateAutoConfiguration.class})
@ComponentScan(value = {"com.xuis,me.izhong"})
@Slf4j
public class JobsAgentApplicationRunner implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(JobsAgentApplicationRunner.class, args);
    }

    @Autowired
    private JobServiceReference jobServiceReference;

    @Autowired
    private List<IBatch> batchs;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /*String scriptType = System.getProperty("scriptType");
        if(StringUtils.isNotBlank(scriptType)) {
            return;
        }

        log.info("args:{}",args.getSourceArgs());

        log.info("启动测试==============");
        long jobId = 3;
        long triggerId = 3;

//        String classPath = this.getClass().getClassLoader().getResource("").getPath();
//        File srcPath = new File(classPath).getParentFile().getParentFile();
//        log.info("当前路径：{}",srcPath.getAbsolutePath());
//        File groovyFile = new File(srcPath.getAbsolutePath() + "/src/main/groovy/test.groovy");
//        if(!groovyFile.exists()) {
//            log.error("文件不存在");
//            System.exit(-1);
//        }
//        String script = FileUtils.readFileToString(groovyFile,"utf-8");
        HashMap params = new HashMap();
        params.put("log","/usr");

        HashMap envs = new HashMap();
        envs.put("os","mac");
        JobContext context = new JobContext(jobId,triggerId,3000,envs,params);

        ShellCommandRunJob commandJob = new ShellCommandRunJob("run.sh");
        try {
            //后面考虑缓存 进程id
            commandJob.execute(context);
        } catch (Exception e) {
            log.error("",e);
        }*/
    }

}
