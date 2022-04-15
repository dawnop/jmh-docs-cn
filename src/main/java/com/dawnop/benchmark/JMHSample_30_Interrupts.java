package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * JMH 还可以基准测试中的检测线程是否处于 stuck （长期处于某一任务）状态，
 * 并且将会在不影响测试的情况下强制终止 stuck 线程。
 * <p>
 * 我们打算简单的测测 {@link ArrayBlockingQueue} 的性能。
 * 不幸的是，若没有框架支持，因为 take/put 操作并不是一一对应的，
 * 所以测试中的一个线程就有可能会死锁。
 * 所幸，take/put 操作都可以被中断，所以 JMH 提供了中断测试的方式。
 * 若有线程被中断，JMH 会通知我们，所以我们可以看看中断是否影响了测试。
 * 当线程长期（时间可以被我们指定）处于某一任务，JMH 就会中断线程。
 * <p>
 * 这个例子是 {@link JMHSample_18_Control} 的变体，
 * 但是没有显式的 Control 对象。看看它是如何优雅地中断线程的。
 */


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
public class JMHSample_30_Interrupts {


    private BlockingQueue<Integer> q;

    @Setup
    public void setup() {
        q = new ArrayBlockingQueue<>(1);
    }

    @Group("Q")
    @Benchmark
    public Integer take() throws InterruptedException {
        return q.take();
    }

    @Group("Q")
    @Benchmark
    public void put() throws InterruptedException {
        q.put(42);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_30_Interrupts.class.getSimpleName())
                .threads(2)
                .forks(5)
                .timeout(TimeValue.seconds(10))
                .build();

        new Runner(opt).run();
    }

}