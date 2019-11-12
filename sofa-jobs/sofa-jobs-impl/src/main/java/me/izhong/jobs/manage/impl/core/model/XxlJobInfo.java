package me.izhong.jobs.manage.impl.core.model;

import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import me.izhong.db.common.annotation.AutoId;
import me.izhong.db.common.annotation.PrimaryId;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_djob_info")
public class XxlJobInfo  extends TimedBasedEntity implements Serializable {

	@AutoId
	@PrimaryId
	@Indexed(unique = true)
	private Long jobId;				// 主键ID
	
	private Long jobGroupId;		// 执行器主键ID
	private String jobCron;		// 任务执行CRON表达式
	private String jobDesc;

	private String author;		// 负责人
	private String alarmEmail;	// 报警邮件

	private String executorRouteStrategy;	// 执行器路由策略
	private String executorHandler;		    // 执行器，任务Handler名称
	private String executorParam;		    // 执行器，任务参数
	private String executorBlockStrategy;	// 阻塞处理策略
	private Long executorTimeout;     		// 任务执行超时时间，单位秒
	private Integer executorFailRetryCount;		// 失败重试次数
	
	private String glueType;		// GLUE类型
	private String glueSource;		// GLUE源代码
	private String glueRemark;		// GLUE备注
	private Date glueUpdatetime;	// GLUE更新时间

	private String childJobId;		// 子任务ID，多个逗号分隔

	private Long triggerStatus;		// 调度状态：0-停止，1-运行
	private Long triggerLastTime;	// 上次调度时间
	private Long triggerNextTime;	// 下次调度时间
}
