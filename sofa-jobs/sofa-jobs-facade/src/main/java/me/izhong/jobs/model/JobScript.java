package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class JobScript implements Serializable {

    private Long jobScriptId;// 任务主键ID
    private Long jobId;

    private String type;
    private String script;
    private String remark;
    private Date addTime;

    private Date createTime;
    private String createBy;
    private Date updateTime;
    private String updateBy;
}
