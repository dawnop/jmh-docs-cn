package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


/**
 * 如果只产生一个结果，隐式返回更具有可读性。如 {@link JMHSample_08_DeadCode}。
 * 但是不要因为隐式返回使得基准测试代码不具有可读性。
 * <p>
 * 如果需要返回多个计算结果，需要考虑如下两个选择。
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class JMHSample_09_Blackholes {


    double x1 = Math.PI;
    double x2 = Math.PI * 2;

    /**
     * Baseline：单次 log 计算。
     */

    @Benchmark
    public double baseline() {
        return Math.log(x1);
    }

    /**
     * log(x2) 不会被优化，log(x1) 会被优化。
     */

    @Benchmark
    public double measureWrong() {
        Math.log(x1);
        return Math.log(x2);
    }

    /**
     * 选择 A:
     * <p>
     * 在返回中合并数个运算结果。
     * 当合并结果的开销远小于计算时，可以采用这种方式。
     */

    @Benchmark
    public double measureRight_1() {
        return Math.log(x1) + Math.log(x2);
    }

    /**
     * 选择 B:
     * <p>
     * 使用显式的 Black-hole 对象，把计算结果放入其中。
     * （Black-hole 是一个绑定在 JVM 上的 @State 对象）
     */

    @Benchmark
    public void measureRight_2(Blackhole bh) {
        bh.consume(Math.log(x1));
        bh.consume(Math.log(x2));
    }

    /**
     * 可以预计 measureWrong() 和 Baseline 的开销差不多，
     * measureRight() 是 Baseline 的两倍开销。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_09_Blackholes.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
