package me.izhong.jobs.manage.impl.core.util;

import me.izhong.jobs.manage.impl.core.model.ZJobLog;
import me.izhong.jobs.model.JobLog;
import org.springframework.beans.BeanUtils;

public class JobLogUtil {

    public static JobLog toRpcBean(ZJobLog db){
        if(db == null)
            return null;
        JobLog job = new JobLog();
        BeanUtils.copyProperties(db,job);
        return job;
    }

    public static ZJobLog toDbBean(JobLog job){
        if(job ==null)
            return null;
        ZJobLog xInfo = new ZJobLog();
        BeanUtils.copyProperties(job,xInfo);
        return xInfo;
    }


}
