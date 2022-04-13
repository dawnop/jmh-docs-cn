package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 在有些特殊的情况下，你已经有了当前代码的测试结果，
 * 但你需要获取粒度更小的 throughput/time 指标，
 * 即把当前代码的测试结果当作基准获取更小粒度的指标。
 * <p>
 * 为了满足这种状况，JMH 提供了一种特殊的注解，{@link AuxCounters}。
 * 这种注解需要加在 {@link State} 上，作用是把 @State 对象当作计数器。
 * {@link AuxCounters} javadoc 中详细介绍了这种方式的限制。
 */

@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class JMHSample_23_AuxCounters {

    @State(Scope.Thread)
    @AuxCounters(AuxCounters.Type.OPERATIONS)
    public static class OpCounters {
        // 这些字段将会被作为计数器
        public int case1;
        public int case2;

        // 这个方法的返回值也会作为一种指标
        public int total() {
            return case1 + case2;
        }
    }

    @State(Scope.Thread)
    @AuxCounters(AuxCounters.Type.EVENTS)
    public static class EventCounters {
        // 这个字段将会被作为计数器
        public int wows;
    }

    /**
     * 这段代码用两个分支模拟测量不同的指标。
     * 被 @AuxCounters 标记的 @State 对象拥有用户控制的计数器，
     * JMH 会将它们的值作为性能分析的指标。
     */

    @Benchmark
    public void splitBranch(OpCounters counters) {
        if (Math.random() < 0.1) {
            counters.case1++;
        } else {
            counters.case2++;
        }
    }

    @Benchmark
    public void runSETI(EventCounters counters) {
        float random = (float) Math.random();
        float wowSignal = (float) Math.PI / 4;
        if (random == wowSignal) {
            // 这情况有点逆天，建议改成 double
            counters.wows++;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_23_AuxCounters.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}