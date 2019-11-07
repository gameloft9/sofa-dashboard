package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class Job implements Serializable {
    private String jobName;
    private String jobKey;
    private Long jobId;
    private Long jobGroupId;
    private String jobGroup;

    private String jobCron;
    private String jobDesc;

    private String author;
    private String alarmEmail;
    private String remark;

    private String executorRouteStrategy;	// 执行器路由策略
    private String executorHandler;		    // 执行器，任务Handler名称
    private String executorParam;		    // 执行器，任务参数
    private String executorBlockStrategy;	// 阻塞处理策略
    private Integer executorTimeout;     		// 任务执行超时时间，单位秒
    private Integer executorFailRetryCount;		// 失败重试次数

    private String glueType;		// GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum
    private String glueSource;		// GLUE源代码
    private String glueRemark;		// GLUE备注
    private Date glueUpdateTime;	// GLUE更新时间

    private String childJobId;		// 子任务ID，多个逗号分隔

    private Integer triggerStatus;		// 调度状态：0-停止，1-运行
    private Long triggerLastTime;	// 上次调度时间
    private Long triggerNextTime;	// 下次调度时间

    private String triggerLastTimeString;	// 上次调度时间
    private String triggerNextTimeString;	// 下次调度时间

    private Date createTime;
    private String createBy;
    private Date updateTime;
    private String updateBy;

}
