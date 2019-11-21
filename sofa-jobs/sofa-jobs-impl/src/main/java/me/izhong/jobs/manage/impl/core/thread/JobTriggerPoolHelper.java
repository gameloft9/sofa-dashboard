package me.izhong.jobs.manage.impl.core.thread;

import me.izhong.jobs.type.TriggerTypeEnum;
import me.izhong.jobs.manage.impl.core.trigger.JobDirectTrigger;
import me.izhong.jobs.manage.impl.core.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class JobTriggerPoolHelper {
    private static Logger logger = LoggerFactory.getLogger(JobTriggerPoolHelper.class);
    // ---------------------- trigger pool ----------------------

    // fast/slow thread pool
    private ThreadPoolExecutor fastTriggerPool = new ThreadPoolExecutor(
            50,
            200,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "job, admin JobTriggerPoolHelper-fastTriggerPool-" + r.hashCode());
                }
            });

    private ThreadPoolExecutor slowTriggerPool = new ThreadPoolExecutor(
            10,
            100,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(2000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "job, admin JobTriggerPoolHelper-slowTriggerPool-" + r.hashCode());
                }
            });

    private volatile ConcurrentMap<Long, AtomicInteger> jobTimeoutCountMap = new ConcurrentHashMap<>();

    private void addTrigger(final long jobId, final TriggerTypeEnum triggerType, final int failRetryCount, final String executorParam) {

        // choose thread pool
        ThreadPoolExecutor triggerPool_ = fastTriggerPool;
        AtomicInteger jobTimeoutCount = jobTimeoutCountMap.get(jobId);
        if (jobTimeoutCount!=null && jobTimeoutCount.get() > 10) {      // job-timeout 10 times in 1 min
            triggerPool_ = slowTriggerPool;
        }

        // trigger
        triggerPool_.execute(new Runnable() {
            @Override
            public void run() {

                long start = System.currentTimeMillis();

                try {
                    JobDirectTrigger.trigger(jobId, triggerType, failRetryCount, executorParam);
                } catch (Exception e) {
                    logger.error("触发JobDirectTrigger异常", e);
                } finally {
                    AtomicInteger timeoutCount = jobTimeoutCountMap.get(jobId);
                    if (timeoutCount == null) {
                        timeoutCount = new AtomicInteger(1);
                        jobTimeoutCountMap.put(jobId,timeoutCount);
                    }
                    // incr timeout-count-map
                    long cost = System.currentTimeMillis()-start;
                    if (cost > 500) {
                        timeoutCount.incrementAndGet();
                    } else if(cost < 50) {
                        timeoutCount.set(1);
                    }

                }

            }
        });
    }

    @PreDestroy
    public void stop() {
        //triggerPool.shutdown();
        fastTriggerPool.shutdownNow();
        slowTriggerPool.shutdownNow();
        logger.info("job trigger thread pool shutdown success.");
    }

    public static void trigger(long jobId, TriggerTypeEnum triggerType, int failRetryCount, String executorParam) {
        SpringUtil.getBean(JobTriggerPoolHelper.class).addTrigger(jobId, triggerType, failRetryCount, executorParam);
    }

}
