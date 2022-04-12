package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * JVM 擅长于 profile 导向的优化，通过分析改进程序的运行时性能。
 * 这对于基准测试来说是个坏消息，因为不同的测试一起运行会干扰 JVM 的分析结果。
 * Forking（在不同的进程中运行）可以规避这个问题。
 * <p>
 * JMH 默认会进行 fork。
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_12_Forking {


    /**
     * 这里有一个简单的计数器接口和两个实现。
     * 即使这两个实现一模一样，但是对于 JVM 来说这是两个完全不同的类。
     * 这里就是想测试混淆 JVM 的分析结果会有什么问题。
     */

    public interface Counter {
        int inc();
    }

    public static class Counter1 implements Counter {
        private int x;

        @Override
        public int inc() {
            return x++;
        }
    }

    public static class Counter2 implements Counter {
        private int x;

        @Override
        public int inc() {
            return x++;
        }
    }

    /**
     * {@link JMHSample_11_Loops} 这个例子中的提到的循环优化，
     * 这里同样会碰到这样的问题。
     */

    public int measure(Counter c) {
        int s = 0;
        for (int i = 0; i < 10; i++) {
            s += c.inc();
        }
        return s;
    }

    /*
     * 两个计数器的实例。
     */
    Counter c1 = new Counter1();
    Counter c2 = new Counter2();

    /**
     * 先单独测试 Counter1，@Fork(0) 会让测试运行在同一个 JVM 中。
     */

    @Benchmark
    @Fork(0)
    public int measure_1_c1() {
        return measure(c1);
    }

    /**
     * 然后是 Counter2。
     */

    @Benchmark
    @Fork(0)
    public int measure_2_c2() {
        return measure(c2);
    }

    /*
     * 然后继续再测试 Counter1。
     */

    @Benchmark
    @Fork(0)
    public int measure_3_c1_again() {
        return measure(c1);
    }

    /*
     * 这两个测试显式地使用了 @Fork 注解。JMH 会把这个注解作为在 forked JVM 中运行测试的请求。
     * 其实在命令行中添加参数 "-f" 可以很容易地强制进行 fork。
     * fork 是默认开启的，但是我们为了统一上面的代码，仍然使用了这个注解。
     */

    /**
     * fork 状态下测试 Counter1。
     */

    @Benchmark
    @Fork(1)
    public int measure_4_forked_c1() {
        return measure(c1);
    }

    /**
     * fork 状态下测试 Counter2。
     */

    @Benchmark
    @Fork(1)
    public int measure_5_forked_c2() {
        return measure(c2);
    }

    /**
     * C1 运行地很快，C2 比 C1 慢，C1 again 又比 C2 慢！
     * 这是因为 JVM 的分析结果混淆在了一起。
     * 而 fork 就没有这样的问题。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_12_Forking.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
