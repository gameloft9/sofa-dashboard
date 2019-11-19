import me.izhong.jobs.agent.util.JobStatsUtil

try {

    HashMap<String,String> params = params;
    log.info("参数是:{} {}",params,params.get("xx"));

    String key ="xxx.so"

    boolean exist = JobStatsUtil.checkExist(key);
    println("exist:${exist}")

    String value1 = JobStatsUtil.getValue1(key)
    println("value1:${value1}")

    JobStatsUtil.insertOrUpdate(key,"ty","耗时2");

    value1 = JobStatsUtil.getValue1(key)
    println("update value1 应该为耗时2:${value1}")

    JobStatsUtil.remove(key)
    value1 = JobStatsUtil.getValue1(key)
    println("update value1 应该为空:${value1}")


    return 0
} catch (Exception e) {
    log.error("", e);
    return -1;
}


