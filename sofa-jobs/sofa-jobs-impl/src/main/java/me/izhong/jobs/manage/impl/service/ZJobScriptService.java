package me.izhong.jobs.manage.impl.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.jobs.manage.impl.core.model.ZJobScript;

import java.util.List;

public interface ZJobScriptService extends CrudBaseService<Long,ZJobScript> {
    List<ZJobScript> findByJobId(long jobId);

    void removeOld(Long id, int i);
}
