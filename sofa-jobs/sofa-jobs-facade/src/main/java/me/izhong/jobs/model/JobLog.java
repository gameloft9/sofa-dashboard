package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class JobLog implements Serializable {

    private Long jobLogId;
    private Long jobId;
    private Long jobGroupId;
    private String jobDesc;

    private String status;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    // execute info
    private String executorAddress;
    private String executorHandler;
    private String executorParam;
    private Integer executorFailRetryCount;
    private String blockStrategy;

    // trigger info
    private Date triggerTime;
    private String triggerType;
    private Integer triggerCode;
    private String triggerMsg;

    // handle info
    private Date handleTime;
    private Date finishHandleTime;
    private String costHandleTime;
    private Integer handleCode;
    private String handleMsg;

    // alarm info
    private int alarmStatus;
}
