package me.izhong.jobs.manage.impl.core.thread;

import me.izhong.jobs.manage.impl.core.trigger.TriggerTypeEnum;
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


    // job timeout count
    private volatile long minTim = System.currentTimeMillis()/60000;     // ms > min
    private volatile ConcurrentMap<Long, AtomicInteger> jobTimeoutCountMap = new ConcurrentHashMap<>();


    /**
     * addJobScript trigger
     */
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
                    // do trigger
                    JobDirectTrigger.trigger(jobId, triggerType, failRetryCount, executorParam);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    // check timeout-count-map
                    long minTim_now = System.currentTimeMillis()/60000;
                    if (minTim != minTim_now) {
                        minTim = minTim_now;
                        jobTimeoutCountMap.clear();
                    }

                    // incr timeout-count-map
                    long cost = System.currentTimeMillis()-start;
                    if (cost > 500) {       // ob-timeout threshold 500ms
                        AtomicInteger timeoutCount = jobTimeoutCountMap.putIfAbsent(jobId, new AtomicInteger(1));
                        if (timeoutCount != null) {
                            timeoutCount.incrementAndGet();
                        }
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

    // ---------------------- helper ----------------------
    /**
     * @param jobId
     * @param triggerType
     * @param failRetryCount
     * 			>=0: use this param
     * 			<0: use param from job info config
     * @param executorParam
     *          null: use job param
     *          not null: cover job param
     */
    public static void trigger(long jobId, TriggerTypeEnum triggerType, int failRetryCount, String executorParam) {
        SpringUtil.getBean(JobTriggerPoolHelper.class).addTrigger(jobId, triggerType, failRetryCount, executorParam);
    }

}
