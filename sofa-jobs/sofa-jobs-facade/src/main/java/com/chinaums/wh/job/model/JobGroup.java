package com.chinaums.wh.job.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class JobGroup implements Serializable {
    private String groupName;
    private String groupKey;
    private Long groupId;
    private List<String> groupUrl;

    private String createBy;
    private String updateBy;
}