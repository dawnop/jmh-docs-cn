package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * DEC 的另一个方面就是常量折叠。
 * 如果 JVM 认识到某个计算的结果是常量，会巧妙地优化掉。
 * <p>
 * 我们可以将外部的计算移动到 JMH 循环之内。
 * <p>
 * 可以通过访问 @State 对象的非 final 字段，阻止常量折叠的发生。
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_10_ConstantFold {


    /**
     * IDE 会告诉你：“你可以把这玩意儿变成本地变量”，别信它。
     * IDE 还会告诉你：“你可以把这玩意儿变成 final 变量”，也别信它。
     * 非测试环境下下这么做是对的，但在这里为了测试的正确性，不能这么做。
     */
    private double x = Math.PI;

    private final double wrongX = Math.PI;

    /**
     * Baseline：简单地返回 Math.PI
     */
    @Benchmark
    public double baseline() {
        return Math.PI;
    }

    /**
     * 错误，这个计算的结果是可预见的，会被常量折叠。
     */
    @Benchmark
    public double measureWrong_1() {
        return Math.log(Math.PI);
    }

    /**
     * 错误，这个计算的结果是可预见的，会被常量折叠。
     */
    @Benchmark
    public double measureWrong_2() {
        return Math.log(wrongX);
    }

    /**
     * 正确，这个计算的结果不可预见。
     */
    @Benchmark
    public double measureRight() {
        // This is correct: the source is not predictable.
        return Math.log(x);
    }

    /**
     * measureWrong_*() 的开销会非常小，真实的开销应该看 measureRight()。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_10_ConstantFold.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
