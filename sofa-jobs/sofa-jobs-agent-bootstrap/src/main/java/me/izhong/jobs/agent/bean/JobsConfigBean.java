package me.izhong.jobs.agent.bean;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JobsConfigBean {

    @Value("${jobs.script.dir}")
    private String scriptPath;

    @Value("${jobs.log.dir}")
    private String logDir;
}
