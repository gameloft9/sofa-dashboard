package me.izhong.jobs.manage.impl.core.util;

import me.izhong.jobs.manage.impl.core.model.ZJobScript;
import me.izhong.jobs.model.JobScript;
import org.springframework.beans.BeanUtils;

public class JobScriptUtil {

    public static JobScript toRpcBean(ZJobScript db){
        if(db == null)
            return null;
        JobScript job = new JobScript();
        BeanUtils.copyProperties(db,job);
        return job;
    }

    public static ZJobScript toDbBean(JobScript job){
        if(job ==null)
            return null;
        ZJobScript xInfo = new ZJobScript();
        BeanUtils.copyProperties(job,xInfo);
        return xInfo;
    }


}
