package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class JMHSample_02_BenchmarkModes {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JMHSample_02_BenchmarkModes.class.getSimpleName())
                .exclude(JMHSample_02_BenchmarkModes.class.getSimpleName() + ".measureAll")
                .exclude(JMHSample_02_BenchmarkModes.class.getSimpleName() + ".measureMultiple")
                .output("JMHSample_02_BenchmarkModes_result.sampleLog")
                .forks(1)
                .build();

        new Runner(options).run();
    }


    /*
     * JMH 在编译期间生成代码。JMH 可以通过多种 mode 对代码进行基准测试。
     * 通过 @BenchmarkMode 指定默认的 mode。还可以在 main 函数中显式覆写默认 mode。
     *
     * 执行的方法可能抛出异常，可以显式声明抛出异常。
     * 一旦真的抛出异常，基准测试将终止。
     *
     * 当你对本框架的某种 behavior 产生困惑时，看看本框架生成的代码会很有帮助。
     * 生成的代码可能不是按照你预想的方式在运行。
     * 建议按照教程的顺序进行，并且记得检查生成的代码。
     *
     * 这个 Java 文件生成的代码的位置是：
     * target/generated-sources/annotations/.../JMHSample_02_BenchmarkModes.java
     */

    /**
     * <p>
     * Throughput: 每单位时间的操作 ops/time
     * </p><p>
     * {@link Mode#Throughput}：
     * 持续调用标记有{@link Benchmark}的方法，计算所有工作线程的总吞吐量。
     * 在一定的时间内反复调用，直至时间耗尽。
     * </p>
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public static void measureThroughput() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /*
     * Mode.AverageTime measures the average execution time, and it does it
     * in the way similar to Mode.Throughput.
     *
     * Some might say it is the reciprocal throughput, and it really is.
     * There are workloads where measuring times is more convenient though.
     */

    /**
     * <p>
     * Average time: 每次操作的平均时间 time/op
     * </p><p>
     * {@link Mode#AverageTime}：
     * 持续调用标记有{@link Benchmark}的方法，计算调用所有工作线程的平均时间。
     * 这个是{@link Mode#Throughput}的倒数，在一定的时间内反复调用，直至时间耗尽。
     * </p>
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static void measureAverageTime() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /**
     * <p>
     * Sample time: 对每次操作的时间进行采样 Sampling time
     * </p><p>
     * {@link Mode#SampleTime}：
     * 持续调用标记有{@link Benchmark}的方法，随机采集一部分测试需要的时间。
     * 可以采集运行时间的分布，百分比等。这个模式自动调整采样频率，但是可能会因为测量的暂停遗漏一些采样。
     * 在一定的时间内反复调用，直至时间耗尽。
     * </p>
     */
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static void measureSampleTime() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /**
     * <p>
     * Single shot time: 测量单次操作的时间。
     * </p><p>
     * {@link Mode#SingleShotTime}：
     * 运行一次{@link Benchmark}方法，并且测量它的时间。
     * 不进行 warm up（不进行 JIT 优化）
     * </p>
     * 这种模式的注意事项包括:
     * <ul>
     *  <li>通常需要更多的预热/测量迭代</li>
     *  <li>如果基准测试很小，框架开销可能很大；如果这是一个问题，切换到{@link Mode#SampleTime} 模式</li>
     * </ul>
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static void measureSingleShotTime() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /**
     * <p>
     * {@link BenchmarkMode} 可以添加多个参数。
     * 多个测试，一次完成。
     * </p>
     */

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureMultiple() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /**
     * <p>
     * {@link Benchmark} 一次性完成所有测试。
     * </p>
     */
    @Benchmark
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureAll() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }
}
