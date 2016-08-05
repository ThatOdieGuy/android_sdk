package com.adjust.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pfms on 05/08/2016.
 */
public class CustomScheduledExecutorService extends ScheduledThreadPoolExecutor {
//    private AtomicInteger threadCounter = new AtomicInteger(1);
//    private String source;

    public CustomScheduledExecutorService(final String source) {
        super(1,                                        // Single thread
                new ThreadFactory() {                   // Creator of daemon threads
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setName(Constants.THREAD_PREFIX + thread.getName() + source);
                        thread.setDaemon(true);
                        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread th, Throwable tr) {
                                AdjustFactory.getLogger().error("Thread %s with error %s", th.getName(), tr.getMessage());
                            }
                        });
                        AdjustFactory.getLogger().verbose("Thread %s created", thread.getName());
                        return thread;
                    }
                }, new RejectedExecutionHandler() {     // Logs rejected runnables rejected from the entering the pool
                    @Override
                    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                        AdjustFactory.getLogger().warn("Runnable %s rejected from %s ", runnable.toString(), source);
                    }
                }
        );
//        this.source = source;
        super.allowCoreThreadTimeOut(true);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
//        AdjustFactory.getLogger().verbose("CustomScheduledExecutorService schedule from %s source, with %d delay", source, delay);
        return super.schedule(new RunnableWrapper(command), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
//        AdjustFactory.getLogger().verbose("CustomScheduledExecutorService scheduleWithFixedDelay from %s source, with %d delay and %d initial delay",
//                source, delay, initialDelay);
        return super.scheduleWithFixedDelay(new RunnableWrapper(command), initialDelay, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
//        AdjustFactory.getLogger().verbose("CustomScheduledExecutorService scheduleWithFixedDelay from %s source, with %d delay and %d period",
//                source, period, initialDelay);
        return super.scheduleAtFixedRate(new RunnableWrapper(command), initialDelay, period, unit);
    }

    private class RunnableWrapper implements Runnable {
        private Runnable runnable;
//        private long created;
//        private int threadNumber;

        public RunnableWrapper(Runnable runnable) {
            this.runnable = runnable;
//            created = System.currentTimeMillis();
//            threadNumber = threadCounter.getAndIncrement();
//            AdjustFactory.getLogger().verbose("RunnableWrapper %d from %s created at %d", threadNumber, source, created);
        }

        @Override
        public void run() {
            try {
//                long before = System.currentTimeMillis();
//                AdjustFactory.getLogger().verbose("RunnableWrapper %d from %s source, before running at %d", threadNumber, source, before);
                runnable.run();
//                long after = System.currentTimeMillis();
//                AdjustFactory.getLogger().verbose("RunnableWrapper %d from %s source, after running at %d", threadNumber, source, after);
            } catch (Throwable t) {
                AdjustFactory.getLogger().error("Runnable error %s", t.getMessage());
            }
        }
    }
}
