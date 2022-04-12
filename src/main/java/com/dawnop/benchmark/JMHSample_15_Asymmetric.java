package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 目前为止所有的测试都是对称的：所有线程里执行的都是相同的代码。
 * 有时候，我们需要非对称的测试，JMH 提供了 {@link Group} 的概念，
 * 用以将多个方法绑定为一组，多个线程可以同时执行不同的方法。
 * <p>
 * 每个组包含一个或多个线程，组里的线程会执行 @Group 标记的 @Benchmark 方法。
 * 每次运行可能包含多个组。当每次运行只有一个组，总共的线程数就是组里的线程数。.
 * <p>
 * {@link Scope#Benchmark} 和 {@link Scope#Thread} 并不能覆盖所有的情况。
 * 要么共享一切，要么啥都不共享。为了解决这种问，引入了 {@link Scope#Group}。
 * 标记为 @Group 的状态会被组内共享，而非组间共享。
 * <ul>
 * 举个例子：
 * <li>
 *     a) 定义组 g ，有三个线程执行 inc()，一个线程执行 get()。每个组有四个线程。
 * <li>
 *     b) 如果想让以四个线程运行测试用例，一个组就够了。如果想要 4*N 个线程，需要 N 个组。
 * <li>
 *     c) 每个组有一个 @State 实例 counter，每个组内共享 counter。
 */

@State(Scope.Group)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_15_Asymmetric {

    private AtomicInteger counter;

    @Setup
    public void up() {
        counter = new AtomicInteger();
    }

    @Benchmark
    @Group("g")
    @GroupThreads(3)
    public int inc() {
        return counter.incrementAndGet();
    }

    @Benchmark
    @Group("g")
    @GroupThreads(1)
    public int get() {
        return counter.get();
    }


    /**
     * 将会分别获取 inc() 和 get() 的性能指标。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_15_Asymmetric.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}