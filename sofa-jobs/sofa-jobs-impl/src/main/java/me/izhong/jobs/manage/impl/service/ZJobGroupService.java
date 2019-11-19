package me.izhong.jobs.manage.impl.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.jobs.manage.impl.core.model.ZJobGroup;

import java.util.List;

public interface ZJobGroupService extends CrudBaseService<Long,ZJobGroup> {
    List<ZJobGroup> findByAddressType(int i);
}
