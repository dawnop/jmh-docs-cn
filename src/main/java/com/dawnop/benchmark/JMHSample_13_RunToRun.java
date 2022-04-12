package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Forking 还可以估算多次运行结果的方差。
 * <p>
 * JVM 是非常复杂的系统，它天生就具有不确定性。因此需要在实验中考虑运行的方差。
 * <p>
 * 幸运的是，Forking 方式汇总了多个 JVM 实例的运行结果。
 */


@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JMHSample_13_RunToRun {


    /**
     * 为了清晰地展示不同运行的方差，被测方法选择了生成随机数，多次运行会有较大的方差。
     * 大多数的被测方法每次运行结果差异不大，我们人为地选择方差较大的被测方法以验证上述观点。
     */

    @State(Scope.Thread)
    public static class SleepyState {
        public long sleepTime;

        @Setup
        public void setup() {
            sleepTime = (long) (Math.random() * 1000);
        }
    }

    /*
     * 现在运行不同的次数。
     */

    @Benchmark
    @Fork(1)
    public void baseline(SleepyState s) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(s.sleepTime);
    }

    @Benchmark
    @Fork(5)
    public void fork_1(SleepyState s) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(s.sleepTime);
    }

    @Benchmark
    @Fork(20)
    public void fork_2(SleepyState s) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(s.sleepTime);
    }

    /**
     * 注意到 baseline 随机睡眠 [0..1000] ms，所有的 fork 运行时间的期望是 500 ms。
     * 实际上，即使对于同一次 fork，不同的 iteration 仍然会有较小的误差。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_13_RunToRun.class.getSimpleName())
                .warmupIterations(0)
                .measurementIterations(3)
                .build();

        new Runner(opt).run();
    }

}