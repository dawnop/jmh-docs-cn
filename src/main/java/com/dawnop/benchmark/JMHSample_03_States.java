package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JMHSample_03_States {

    /**
     * 有些时候，需要在基准测试时维护一些状态。
     * 由于 JMH 大多用以进行并发基准测试，我们选择了 state-bearing objects 的方式。
     * <p>
     * 下面是两个状态对象，它们的类名不重要，重要的时它们被标记了 {@link State}。
     * 这些对象会懒加载，并且在整个基准测试中复用。
     * <p>
     * 一个重要的特点是，某个状态将会被所有访问该状态的线程中的一个进行初始化。
     * 这意味着你可以像在工作线程那样初始化字段。
     * 可以通过指定 {@link Scope#Thread}，达到 {@link ThreadLocal} 的效果。
     * （实现某个线程上的局部变量）。
     */

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        volatile double x = Math.PI;
    }

    @State(Scope.Thread)
    public static class ThreadState {
        volatile double x = Math.PI;
    }

    /*
     * Benchmark 方法可以引用状态，JMH 会在方法调用时注入相应的状态。
     * 可以没有状态，一个状态或者多个状态的引用。
     * 这样可以轻而易举地实现多线程基准测试。
     *
     * 下面有两个方法来测试上述说明。
     */

    /**
     * 所有的基准测试线程都可以调用这个方法。
     * 然而，当 {@link ThreadState} 被标记为 {@link Scope#Thread}，
     * 每个线程都拥有自己的对该状态的拷贝。
     * 用以实现线程之间状态不共享的基准测试。
     */
    @Benchmark
    public void measureUnshared(ThreadState state) {
        state.x++;
    }

    /**
     * 所有的基准测试线程都可以调用这个方法。
     * 当 {@link BenchmarkState} 被标记为 {@link Scope#Benchmark}，
     * 所有的线程将会共享状态实例。
     * 用以实现线程共享状态的基准测试。
     */

    @Benchmark
    public void measureShared(BenchmarkState state) {
        state.x++;
    }

    /**
     * 状态共享，所有的线程访问同一块内存。
     * 状态不共享，所有的线程访问各自的内存。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_03_States.class.getSimpleName())
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}