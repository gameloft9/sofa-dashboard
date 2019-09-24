package com.chinaums.wh.job.manage.impl.core.util;

import com.chinaums.wh.job.manage.impl.core.model.XxlJobInfo;
import com.chinaums.wh.job.model.Job;
import org.springframework.beans.BeanUtils;

public class JobUtil {

    public static Job toRpcBean(XxlJobInfo db){
        if(db == null)
            return null;
        Job job = new Job();
        BeanUtils.copyProperties(db,job);
        return job;
    }

    public static XxlJobInfo toDbBean(Job job){
        if(job ==null)
            return null;
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        BeanUtils.copyProperties(job,xxlJobInfo);
        return xxlJobInfo;
    }


}
