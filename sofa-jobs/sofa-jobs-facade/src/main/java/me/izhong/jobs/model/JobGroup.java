package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class JobGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private String groupName;
    private String remark;
    private Long groupId;
    private Long order;

    private Date createTime;
    private String createBy;
    private Date updateTime;
    private String updateBy;
}
