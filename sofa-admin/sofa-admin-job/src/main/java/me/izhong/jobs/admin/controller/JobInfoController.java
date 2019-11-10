package me.izhong.jobs.admin.controller;

import me.izhong.common.util.CronUtil;
import me.izhong.common.util.DateUtil;
import me.izhong.db.common.exception.BusinessException;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.domain.PageModel;
import me.izhong.jobs.admin.service.JobServiceReference;
import me.izhong.db.common.annotation.AjaxWrapper;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.model.JobGroup;
import me.izhong.jobs.type.GlueTypeEnum;
import me.izhong.model.ReturnT;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.jobs.admin.service.JobServiceReference;
import me.izhong.jobs.type.GlueTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/monitor/djob")
public class JobInfoController {

	private String prefix = "monitor/djob";

	@Autowired(required = false)
	private JobServiceReference jobServiceReference;

	@GetMapping()
	public String job( Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup)
	{
		// 枚举-字典
		//model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	// 路由策略-列表
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		//model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	// 阻塞处理策略-字典

		// 执行器列表
		List<JobGroup> jobGroupList_all =  jobServiceReference.jobService.selectAllJobGroup();

		// filter group
		List<JobGroup> jobGroupList = jobGroupList_all;
		if (jobGroupList==null || jobGroupList.size()==0) {
		//	throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}

		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);
		return prefix + "/job";
	}

	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<Job> pageList(HttpServletRequest request, Job ino) {
		PageModel<Job>  pm = jobServiceReference.jobService.pageList(PageRequestUtil.fromRequest(request),ino);
		if(pm != null){
			List<Job> jobs = pm.getRows();
			if(jobs != null) {
				jobs.forEach(e -> {
					e.setTriggerLastTimeString(DateUtil.parseLongToFullSting(e.getTriggerLastTime()));
					e.setTriggerNextTimeString(DateUtil.parseLongToFullSting(e.getTriggerNextTime()));
				});
			}
		}
		return pm;
	}

	@GetMapping("/add")
	public String add(Model model) {
		model.addAttribute("groupList",jobServiceReference.jobService.selectAllJobGroup());
		return prefix + "/add";
	}

	@PostMapping("/add")
	@AjaxWrapper
	public void add(Job jobInfo) {
		if(jobInfo.getExecutorFailRetryCount() == null) {
			jobInfo.setExecutorFailRetryCount(0L);
		}
		if(jobInfo.getExecutorTimeout() == null) {
			jobInfo.setExecutorTimeout(30000L);
		}
        jobInfo.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        jobInfo.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
		ReturnT<String> rObj = jobServiceReference.jobService.add(jobInfo);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}

	@GetMapping("/edit/{jobId}")
	public String edit(@PathVariable("jobId") Long jobId,Model model) {
		if(jobId == null){
			throw BusinessException.build("jobId 不能为空");
		}
		model.addAttribute("groupList",jobServiceReference.jobService.selectAllJobGroup());
		Job job = jobServiceReference.jobService.findByJobId(jobId);
		if(job == null) {
			throw BusinessException.build(String.format("任务不存在%s",jobId));
		}
		model.addAttribute("job",job);
		return prefix + "/edit";
	}


	@PostMapping("/edit")
	@AjaxWrapper
	public void update(Job jobInfo) {
		Job exists_jobInfo = jobServiceReference.jobService.findByJobId(jobInfo.getJobId());
		if (exists_jobInfo == null) {
			throw BusinessException.build("任务不存在");
		}

		//只修改想修改的内容
		exists_jobInfo.setJobGroupId(jobInfo.getJobGroupId());
		exists_jobInfo.setJobCron(jobInfo.getJobCron());
		exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
		exists_jobInfo.setAuthor(jobInfo.getAuthor());
		exists_jobInfo.setAlarmEmail(jobInfo.getAlarmEmail());
		exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
		exists_jobInfo.setExecutorHandler(jobInfo.getExecutorHandler());
		exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
		exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
		exists_jobInfo.setExecutorTimeout(jobInfo.getExecutorTimeout());
		exists_jobInfo.setExecutorFailRetryCount(jobInfo.getExecutorFailRetryCount());
		exists_jobInfo.setChildJobId(jobInfo.getChildJobId());
		exists_jobInfo.setTriggerStatus(jobInfo.getTriggerStatus());
		exists_jobInfo.setRemark(jobInfo.getRemark());

		ReturnT<String> rObj = jobServiceReference.jobService.update(exists_jobInfo);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}
	
	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(Long jobId) {
		ReturnT<String> rObj = jobServiceReference.jobService.remove(jobId);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}
	
	@RequestMapping("/stop")
	@AjaxWrapper
	public void stop(Long jobId) {
		ReturnT<String> rObj = jobServiceReference.jobService.disable(jobId);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}
	
	@RequestMapping("/start")
	@AjaxWrapper
	public void start(Long jobId) {
		ReturnT<String> rObj = jobServiceReference.jobService.enable(jobId);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}
	
	@RequestMapping("/trigger")
	@AjaxWrapper
	public void triggerJob(Long jobId, String executorParam) {
		// force cover job param
		if (executorParam == null) {
			executorParam = "";
		}
		ReturnT<String> rObj = jobServiceReference.jobService.trigger(jobId);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
		//JobTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, executorParam);
	}

	@PostMapping("/checkCronExpressionIsValid")
	@ResponseBody
	public boolean checkCronExpressionIsValid(String jobCron)
	{
		return CronUtil.isValid(jobCron);
	}
	
}