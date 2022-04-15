package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * {@link JMHSample_11_Loops} 中提到过在 @Benchmark 方法中使用循环的问题。
 * 但是有时候你可能需要访问集合中的元素，没有循环的话很难实现。
 * 这里我们提供了一种安全使用循环的方案。
 */


@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_34_SafeLooping {

    /**
     * 假设我们想要测一下执行 work() 不同的参数会带来什么性能影响。
     * 对于测试相同实现但是参数不同的多个实例，可以用下面这种方法。
     */

    static final int BASE = 42;

    static int work(int x) {
        return BASE + x;
    }

    /**
     * 我们可以通过设定基准的方式检查测试是否如我们预期进行。
     * 如果随着数据规模的扩大，基准测试的开销是线性增长，说明没问题。
     * 若与线性增长偏差过大，可能是哪里出现了问题。
     */

    @Param({"1", "10", "100", "1000"})
    int size;

    int[] xs;

    @Setup
    public void setup() {
        xs = new int[size];
        for (int c = 0; c < size; c++) {
            xs[c] = c;
        }
    }

    /**
     * 首先是一种显然的错误。保存结果在本地变量中是不行的。
     * 足够智能的编译器会内联 work()，并且发现最后一个work() 才需要计算。
     * 无论跑多大规模，开销都差不多。
     */

    @Benchmark
    public int measureWrong_1() {
        int acc = 0;
        for (int x : xs) {
            acc = work(x);
        }
        return acc;
    }

    /**
     * 其次是另一种错误，将所有的计算结果累加到本地变量。
     * 虽然这会使得强制计算每个 work() 的值，但是因为流水线的存在，
     * 多次循环可能会被合并成一个循环，从而影响测试结果。
     * <p>
     * 在这个例子中，HotSpot 会进行一系列优化，比如进行方法内联，
     * 将 work() 的函数调用优化为 42 + x。
     * 还会进行循环展开，最终的性能取决于总共进行了多少次循环，
     * 以及每次循环的步长有多大。
     */

    @Benchmark
    public int measureWrong_2() {
        int acc = 0;
        for (int x : xs) {
            acc += work(x);
        }
        return acc;
    }

    /**
     * 接下来看看如何正确测试。
     * 显而易见的做法是将每一次循环产生的数据投喂给 Blackhole，
     * 这将会迫使 JVM 每次都完整地调用 work()。
     * 我们通常希望多个线程同时计算 work()，
     * 但是 {@link Blackhole} 的内存布局阻止了这种优化。
     */

    @Benchmark
    public void measureRight_1(Blackhole bh) {
        for (int x : xs) {
            bh.consume(work(x));
        }
    }

    /**
     * *警告*：请先阅读以下说明！
     * <p>
     * 对于毫秒级别的基准测试 Blackhole 的开销过于巨大。
     * 这种情况下可以仿照 Blackhole 的原理，自己写一个简单的版本。
     * 这种技巧依赖于具体的虚拟机实现，并且只有在你已经完全理解了 JMH 的生成代码的前提下才可以使用。
     * 但在处理毫秒级的问题上，这确实是个有用的方式。
     * <p>
     * 你*不应该*使用这种技巧，除非完全清楚它的各种细节。
     */

    @Benchmark
    public void measureRight_2() {
        for (int x : xs) {
            sink(work(x));
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static void sink(int v) {
        // *匹配方法签名时要注意不要使用自动装箱和自动装箱拆箱*。
        // 刻意留白。
    }


    /**
     * 你可能发现了，measureWrong_1() 的开销不依赖于 size，measureWrong_2() 的开销与 size 不是线性的。
     * 这两种情况都要比 measureRight_*() 快得多。相对而言，
     * measureRight_2() 比 measureRight_1() 快一点点。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_34_SafeLooping.class.getSimpleName())
                .forks(3)
                .build();

        new Runner(opt).run();
    }

}
