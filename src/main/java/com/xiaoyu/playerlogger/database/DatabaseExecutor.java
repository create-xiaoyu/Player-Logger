package com.xiaoyu.playerlogger.database;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.DEDICATED_SERVER)
public class DatabaseExecutor {
    private static ExecutorService executor;

    public static void init(int threadCount) {
        executor = Executors.newFixedThreadPool(threadCount);
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}
