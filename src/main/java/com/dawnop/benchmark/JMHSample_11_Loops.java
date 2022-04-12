package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


/**
 * 在基准测试中测试循环是不是个好的选择。这个测试会告诉你为什么。
 * <p>
 * 有这么一种说法：调用一次测试方法，在循环内部执行测试代码，
 * 比循环调用测试方法有更小的框架开销。
 * 但是这种想法忽略了编译器的强大功能：循环优化 (loop optimization)。
 * 编译器会对你的循环一顿优化，优化之后的执行结果虽然不变，但是代码已经面目全非。
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_11_Loops {

    /*
     * 我们想测试一下把两个 int 相加的开销。
     */

    int x = 1;
    int y = 2;

    /**
     * 使用 JMH 的测试是正确的方法。
     */

    @Benchmark
    public int measureRight() {
        return (x + y);
    }

    /**
     * 这个方法模拟了 Java 自带的循环。
     */
    private int reps(int reps) {
        int s = 0;
        for (int i = 0; i < reps; i++) {
            s += (x + y);
        }
        return s;
    }

    /*
     * 用不同的重复次数来测量每一次重复的开销。
     * @OperationsPerInvocation 就是这样的东西。
     */

    @Benchmark
    @OperationsPerInvocation(1)
    public int measureWrong_1() {
        return reps(1);
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public int measureWrong_10() {
        return reps(10);
    }

    @Benchmark
    @OperationsPerInvocation(100)
    public int measureWrong_100() {
        return reps(100);
    }

    @Benchmark
    @OperationsPerInvocation(1_000)
    public int measureWrong_1000() {
        return reps(1_000);
    }

    @Benchmark
    @OperationsPerInvocation(10_000)
    public int measureWrong_10000() {
        return reps(10_000);
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public int measureWrong_100000() {
        return reps(100_000);
    }

    /**
     * 你应该会注意到重复的次数越多，数值上每次重复的开销就越少。
     * 从重复 1 次到重复 1000 次，每次重复的时间从 2.6ns 下降到 0.026ns。
     * 这已经超过了机器本身的性能。
     * <p>
     * 这是因为通过循环展开/流水线技术，大大降低了循环的次数和分支预测失败的可能性。
     * 建议不要过度使用循环，通过 JMH 更容易得到正确的测试结果。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_11_Loops.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}