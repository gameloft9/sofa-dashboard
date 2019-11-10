package me.izhong.jobs.manage.impl.core.thread;

import me.izhong.jobs.manage.impl.core.conf.RegistryConfig;
import me.izhong.jobs.manage.impl.core.conf.XxlJobAdminConfig;
import me.izhong.jobs.manage.impl.core.model.XxlJobGroup;
import me.izhong.jobs.manage.impl.core.model.XxlJobRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JobRegistryMonitorHelper {
	private static Logger logger = LoggerFactory.getLogger(JobRegistryMonitorHelper.class);

	private Thread registryThread;
	private volatile boolean toStop = false;

	@PostConstruct
	public void start(){
		registryThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!toStop) {
					try {
						// auto registry group
						List<XxlJobGroup> groupList = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupService().findByAddressType(0);
						if (groupList!=null && !groupList.isEmpty()) {

							// removeJobGroup dead address (admin/executor)
							List<Long> ids = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryService().findDead(RegistryConfig.DEAD_TIMEOUT);
							if (ids!=null && ids.size()>0) {
								XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryService().remove(ids);
							}

							// fresh online address (admin/executor)
							HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
							List<XxlJobRegistry> list = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryService().findNormal(RegistryConfig.DEAD_TIMEOUT);
							if (list != null) {
								for (XxlJobRegistry item: list) {
									//if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
										String appName = item.getRegistryKey();
										List<String> registryList = appAddressMap.get(appName);
										if (registryList == null) {
											registryList = new ArrayList<String>();
										}

										if (!registryList.contains(item.getRegistryValue())) {
											registryList.add(item.getRegistryValue());
										}
										appAddressMap.put(appName, registryList);
									//}
								}
							}

							// fresh group address
							for (XxlJobGroup group: groupList) {
								List<String> registryList = appAddressMap.get(group.getGroupName());
//								String addressListStr = null;
//								if (registryList!=null && !registryList.isEmpty()) {
//									Collections.sort(registryList);
//									addressListStr = "";
//									for (String item:registryList) {
//										addressListStr += item + ",";
//									}
//									addressListStr = addressListStr.substring(0, addressListStr.length()-1);
//								}
								group.setAddressList(registryList);
								XxlJobAdminConfig.getAdminConfig().getXxlJobGroupService().update(group);
							}
						}
					} catch (Exception e) {
						if (!toStop) {
							logger.error("job registry monitor thread error:{}", e);
						}
					}
					try {
						TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
					} catch (InterruptedException e) {
						if (!toStop) {
							logger.error("job registry monitor thread error:{}", e);
						}
					}
				}
				logger.info("job registry monitor thread stop");
			}
		});
		registryThread.setDaemon(true);
		registryThread.setName("xxl-job, admin JobRegistryMonitorHelper");
		registryThread.start();
	}

	@PreDestroy
	public void toStop(){
		toStop = true;
		// interrupt and wait
		registryThread.interrupt();
		try {
			registryThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

}