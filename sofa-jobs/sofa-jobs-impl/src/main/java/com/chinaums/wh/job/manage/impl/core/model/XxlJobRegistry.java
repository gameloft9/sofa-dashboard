package com.chinaums.wh.job.manage.impl.core.model;

import lombok.Data;
import com.chinaums.wh.db.common.annotation.AutoId;
import com.chinaums.wh.db.common.annotation.PrimaryId;
import me.izhong.dashboard.manage.domain.TimedBasedEntity;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;

@Data
public class XxlJobRegistry  extends TimedBasedEntity implements Serializable {

    @AutoId
    @PrimaryId
    @Indexed(unique = true)
    private Long jobRegistryId;

    private String registryGroup;
    private String registryKey;
    private String registryValue;

}