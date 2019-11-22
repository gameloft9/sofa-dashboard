import me.izhong.jobs.agent.util.TriggerUtil
import org.apache.commons.lang3.time.DateUtils

import java.text.SimpleDateFormat

try {

    HashMap<String,String> params = params;
    log.info("参数是:{} {}",params,params.get("xx"));

    Date yesterday = DateUtils.addDays(new Date(), -1);
    println "脚本操作异常，错误信息:" + yesterday
    //TimeUnit.SECONDS.sleep(300)
    log.info("log 测试");
    log.info("log 测试2 {}",yesterday);

    //TriggerUtil.triggerJob(2)
    TriggerUtil.updateJobNextTriggerTime(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-10-10 08:09:10"))
    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
}


