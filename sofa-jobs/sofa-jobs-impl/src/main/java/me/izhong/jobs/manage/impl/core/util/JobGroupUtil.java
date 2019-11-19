package me.izhong.jobs.manage.impl.core.util;

import me.izhong.jobs.manage.impl.core.model.ZJobGroup;
import me.izhong.jobs.model.JobGroup;
import org.springframework.beans.BeanUtils;

public class JobGroupUtil {

    public static JobGroup toRpcBean(ZJobGroup db){
        if(db == null)
            return null;
        JobGroup jobGroup = new JobGroup();
        BeanUtils.copyProperties(db,jobGroup);
        return jobGroup;
    }

    public static ZJobGroup toDbBean(JobGroup job){
        if(job ==null)
            return null;
        ZJobGroup zJobGroup = new ZJobGroup();
        BeanUtils.copyProperties(job, zJobGroup);
        return zJobGroup;
    }


}
