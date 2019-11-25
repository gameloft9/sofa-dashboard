package me.izhong.jobs.manage.impl.core.model;

import com.alibaba.fastjson.annotation.JSONField;
import me.izhong.db.common.annotation.AutoId;
import me.izhong.db.common.annotation.CreateTimeAdvise;
import me.izhong.db.common.annotation.PrimaryId;
import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_djob_script")
public class ZJobScript extends TimedBasedEntity implements Serializable {

	@AutoId
	@PrimaryId
	@Indexed(unique = true)
	private Long jobScriptId;// 主键ID

	@NotNull
	private Long jobId;

	private String type;
	@NotNull
	private String script;

	@Indexed
	@CreateTimeAdvise
	private Date addTime;
}
