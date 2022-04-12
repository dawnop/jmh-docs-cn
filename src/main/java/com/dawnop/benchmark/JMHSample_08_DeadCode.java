package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 许多基准测试工具都没有考虑到 DCE (Dead-Code Elimination)。
 * 编译器能够检测到一些无用的计算，并把其优化掉。
 * 麻烦的是，基准测试中的代码也有可能被优化掉。
 * <p>
 * JMH 提供了解决这个问题的方法：把运算结果返回，JMH 会处理这些数据，从而避免了 DCE。
 * 返回的结果被 Black-holes 隐式消耗。详情见 {@link JMHSample_09_Blackholes}。
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_08_DeadCode {


    private double x = Math.PI;

    /**
     * Baseline，啥都不做。
     */

    @Benchmark
    public void baseline() {
    }

    /**
     * 错误，运算结果没有返回，这段代码将会被优化掉。
     */

    @Benchmark
    public void measureWrong() {
        Math.log(x);
    }

    /**
     * 正确。
     */

    @Benchmark
    public double measureRight() {
        return Math.log(x);
    }

    /**
     * measureWrong() 会执行地非常快，但 measureRight() 却执行地很慢。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_08_DeadCode.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}