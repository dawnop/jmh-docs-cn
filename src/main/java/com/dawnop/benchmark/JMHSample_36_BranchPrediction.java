package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 这个例子用来说明分支预测对于有规则的数据的性能优化。
 * <p>
 * 假设我们的if-else依赖于数组的内容。
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
@State(Scope.Benchmark)
public class JMHSample_36_BranchPrediction {


    private static final int COUNT = 1024 * 1024;

    private byte[] sorted;
    private byte[] unsorted;

    @Setup
    public void setup() {
        sorted = new byte[COUNT];
        unsorted = new byte[COUNT];
        Random random = new Random(1234);
        random.nextBytes(sorted);
        random.nextBytes(unsorted);
        Arrays.sort(sorted);
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public void sorted(Blackhole bh1, Blackhole bh2) {
        for (byte v : sorted) {
            if (v > 0) {
                bh1.consume(v);
            } else {
                bh2.consume(v);
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public void unsorted(Blackhole bh1, Blackhole bh2) {
        for (byte v : unsorted) {
            if (v > 0) {
                bh1.consume(v);
            } else {
                bh2.consume(v);
            }
        }
    }

    /*
        上述排序与未排序的基准测试存在极大的性能差距。
        这是因为排好序的数组分支预测的命中率会很高，而未排序的数组分支预测命中率就比较低。
        可以通过 -prof perfnorm 参数方便地进行展示，
        注意到未排序的基准测试有较大的 branch-misses（分支预测失败）和较大的 CPI（平均指令周期）

        Benchmark                                                       Mode  Cnt   Score    Error  Units
        JMHSample_36_BranchPrediction.sorted                            avgt   25   2.160 ±  0.049  ns/op
        JMHSample_36_BranchPrediction.sorted:·CPI                       avgt    5   0.286 ±  0.025   #/op
        JMHSample_36_BranchPrediction.sorted:·branch-misses             avgt    5  ≈ 10⁻⁴            #/op
        JMHSample_36_BranchPrediction.sorted:·branches                  avgt    5   7.606 ±  1.742   #/op
        JMHSample_36_BranchPrediction.sorted:·cycles                    avgt    5   8.998 ±  1.081   #/op
        JMHSample_36_BranchPrediction.sorted:·instructions              avgt    5  31.442 ±  4.899   #/op
        JMHSample_36_BranchPrediction.unsorted                          avgt   25   5.943 ±  0.018  ns/op
        JMHSample_36_BranchPrediction.unsorted:·CPI                     avgt    5   0.775 ±  0.052   #/op
        JMHSample_36_BranchPrediction.unsorted:·branch-misses           avgt    5   0.529 ±  0.026   #/op  <--- OOPS
        JMHSample_36_BranchPrediction.unsorted:·branches                avgt    5   7.841 ±  0.046   #/op
        JMHSample_36_BranchPrediction.unsorted:·cycles                  avgt    5  24.793 ±  0.434   #/op
        JMHSample_36_BranchPrediction.unsorted:·instructions            avgt    5  31.994 ±  2.342   #/op

        许多情况下，需要结合分支预测的最好和最坏情况做分析。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + JMHSample_36_BranchPrediction.class.getSimpleName() + ".*")
                .build();

        new Runner(opt).run();
    }

}