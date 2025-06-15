package org.tima;

import org.junit.Test;
import static org.junit.Assert.*;
import org.tima.schedulers.*;

public class SchedulerTest {
    @Test
    public void testIOThreadScheduler() {
        Scheduler scheduler = new IOThreadScheduler();
        final Thread[] thread = {null};
        scheduler.execute(() -> thread[0] = Thread.currentThread());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            fail("Прерывание");
        }
        assertNotNull(thread[0]);
        assertNotEquals(Thread.currentThread(), thread[0]);
    }

    @Test
    public void testComputationScheduler() {
        Scheduler scheduler = new ComputationScheduler();
        final Thread[] thread = {null};
        scheduler.execute(() -> thread[0] = Thread.currentThread());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            fail("Прерывание");
        }
        assertNotNull(thread[0]);
        assertNotEquals(Thread.currentThread(), thread[0]);
    }

    @Test
    public void testSingleThreadScheduler() {
        Scheduler scheduler = new SingleThreadScheduler();
        final Thread[] thread = {null};
        scheduler.execute(() -> thread[0] = Thread.currentThread());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            fail("Прерывание");
        }
        assertNotNull(thread[0]);
        assertNotEquals(Thread.currentThread(), thread[0]);
    }
}