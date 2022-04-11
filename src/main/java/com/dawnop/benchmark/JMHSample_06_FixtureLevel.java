package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class JMHSample_06_FixtureLevel {

    double x;

    /**
     * Fixture 方法有几个不同的等级。用以决定在什么时候运行。
     * 对用户来说有三个可用的 level。从高到低分别是：
     * <p>
     * Level.Trial: 整个基准测试的前后（一个 @Benchmark 为一个基准测试） <br>
     * Level.Iteration: 每次迭代（Iteration）的前后 <br>
     * Level.Invocation: 较为复杂，见 {@link Level}
     * <p>
     * Fixture 方法消耗的时间不会计入性能指标，可以用来做一些 heavy 的操作。
     */

    @TearDown(Level.Iteration)
    public void check() {
        assert x > Math.PI : "Nothing changed?";
    }

    @Benchmark
    public void measureRight() {
        x++;
    }

    @Benchmark
    public void measureWrong() {
        double x = 0;
        x++;
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_06_FixtureLevel.class.getSimpleName())
                .forks(1)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // 改成 ture 可以避免 assert 失败带来的程序终止
                .build();

        new Runner(opt).run();
    }

}
