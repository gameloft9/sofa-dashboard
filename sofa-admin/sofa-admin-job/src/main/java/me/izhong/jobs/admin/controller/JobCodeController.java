package me.izhong.jobs.admin.controller;

import me.izhong.db.common.annotation.AjaxWrapper;
import me.izhong.db.common.exception.BusinessException;
import me.izhong.jobs.admin.service.JobServiceReference;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.model.JobScript;
import me.izhong.model.ReturnT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/monitor/djob")
public class JobCodeController {

	private String prefix = "monitor/djob";

	@Autowired(required = false)
	private JobServiceReference jobServiceReference;

	@RequestMapping("/code")
	public String index(HttpServletRequest request, Model model, long jobId) {
		Job jobInfo = jobServiceReference.jobService.findByJobId(jobId);

		if (jobInfo == null) {
			throw new RuntimeException("任务未找到");
		}

		// Glue类型-字典
		//model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());

		//model.addAttribute("jobInfo", jobInfo);
		//model.addAttribute("jobLogGlues", jobLogGlues);
		model.addAttribute("jobId", jobInfo.getJobId());

		Long scriptId = jobInfo.getJobScriptId();
		if(scriptId != null) {
			JobScript jobScript = jobServiceReference.jobService.findByJobScriptId(scriptId);
			if(jobScript != null)
				model.addAttribute("jobScript", jobScript.getScript());
		}
		return prefix + "/jobCode";
	}
	
	@RequestMapping("/code/save")
	@AjaxWrapper
	public ReturnT<String> save(Model model, long id, String jobScript, String jobRemark) {
		// valid
		if (jobScript==null) {
			throw BusinessException.build("脚本内容不能为空");
		}
		if (jobScript.length()<10) {
			throw BusinessException.build("脚本内容不能为空");
		}
		if (jobRemark==null) {
			throw BusinessException.build("脚本内容不能为空");
		}
		if (jobRemark.length()<4 || jobRemark.length()>100) {
			throw BusinessException.build("脚本内容不能为空");
		}
		Job exists_jobInfo = jobServiceReference.jobService.findByJobId(id);
		if (exists_jobInfo == null) {
			throw BusinessException.build("任务不存在");
		}

		JobScript jobS = new JobScript();
		jobS.setScript(jobScript);
		jobS.setRemark(jobRemark);
		JobScript  rt = jobServiceReference.jobService.addJobScript(jobS);
		if(rt==null) {
			throw BusinessException.build("保存异常");
		}
		jobS = rt;
		Long jobScriptId = jobS.getJobScriptId();
		jobServiceReference.jobService.updateJobScriptId(id,jobScriptId);

		// removeJobGroup code backup more than 30
		jobServiceReference.jobService.removeOldScript(exists_jobInfo.getJobId(), 30);

		return ReturnT.SUCCESS;
	}
	
}
