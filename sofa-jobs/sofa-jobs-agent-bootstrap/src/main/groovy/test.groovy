
import org.apache.commons.lang3.time.DateUtils

import java.util.concurrent.TimeUnit


try {

    HashMap<String,String> params = params;
    log.info("参数是:{} {}",params,params.get("xx"));

    Date yesterday = DateUtils.addDays(new Date(), -1);
    println "脚本操作异常，错误信息:" + yesterday
    TimeUnit.SECONDS.sleep(300)
    log.info("log 测试");
    log.info("log 测试2 {}",yesterday);

    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
}


