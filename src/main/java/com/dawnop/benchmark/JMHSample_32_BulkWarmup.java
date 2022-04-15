package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.WarmupMode;

import java.util.concurrent.TimeUnit;

/**
 * 这是对 {@link JMHSample_12_Forking} 的补充说明。
 * <p>
 * 有时候你想把不同 benchmark 中的 JVM 分析结果全部汇总起来，
 * 此时 JVM 对每种情况的优化都存在，但都不是最好，来测试 JVM 一般优化的表现。
 * <p>
 * JMH 有个混合预热的特性，然而并不是在同一个 JVM 中预热所有的代码，然后测试。
 * JMH 仍然会 fork 新的 JVM，但是预热阶段将会对所有的测试代码进行预热。
 * 为了避免 JIT 优化对结果的影响，不同的测试仍然在不同的 JVM 上运行，仅仅预热是相同的。
 * <p>
 * 测试代码和 {@link JMHSample_12_Forking} 差不多。
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_32_BulkWarmup {

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

    Counter c1 = new Counter1();
    Counter c2 = new Counter2();

    /**
     * 这是我们要测试的代码，注意要关闭内联，必须要进行方法调用，
     */

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int measure(Counter c) {
        int s = 0;
        for (int i = 0; i < 10; i++) {
            s += c.inc();
        }
        return s;
    }

    @Benchmark
    public int measure_c1() {
        return measure(c1);
    }

    @Benchmark
    public int measure_c2() {
        return measure(c2);
    }

    /**
     * 注意看 JMH 的每个 fork 先进行了全部预热，然后只进行了一个测试。每个 fork 都重新进行了预热。
     * C1 和 C2 的分数差不多，但是比 JMHSample_12_Forking 中的分数可差太多了。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(JMHSample_32_BulkWarmup.class.getSimpleName())
                // .includeWarmup(...) <-- 这里可以把其他的 benchmark 测试放进来一起预热
                .warmupMode(WarmupMode.BULK) // 看看 WarmupMode 还有啥模式
                .forks(1).build();

        new Runner(opt).run();
    }

}