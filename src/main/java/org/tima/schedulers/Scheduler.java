package org.tima.schedulers;

public interface Scheduler {
    void execute(Runnable task);
}