package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 可以使用 HotSpot 的工具来告诉编译器如何处理特定的方法。
 * 采取了三种方法来演示。
 * <p>
 * 前置知识：方法内联 (Method Inlining)
 * Java 编程语言中虚拟方法调用的频率是一个重要优化瓶颈。
 * 一旦 Java HotSpot 自适应优化器在执行期间收集有关程序热点的信息，它不仅将热点编译为本机代码，而且还对该代码执行大量方法内联。
 * <p>
 * 它大大降低了方法调用的动态频率，从而节省了执行这些方法调用所需的时间。
 * 但更重要的是，内联会为优化程序生成更大的代码块。
 * 这创造了一种能够显着提高传统编译器优化效率的情况，克服了 Java 性能的主要障碍。
 *
 * @see <a href="https://www.oracle.com/java/technologies/whitepaper.html">oracle 官方白皮书</a>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_16_CompilerControl {


    /**
     * 我们的目标
     * <ul>
     * <li> - 第一个方法禁止内联
     * <li> - 第二个方法强制内联
     * <li> - 第三个方法不进行编译
     * </ul>
     * 当然可以直接把注解直接放到 @Benchmark 方法的头上，
     * 这里的封装是为了看起来更清晰。
     */

    public void target_blank() {
        // 空方法
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void target_dontInline() {
        // 空方法
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    public void target_inline() {
        // 空方法
    }

    @CompilerControl(CompilerControl.Mode.EXCLUDE)
    public void target_exclude() {
        // 空方法
    }

    /*
     * 以下是直接运行的测试方法
     */

    @Benchmark
    public void baseline() {
        // 空方法
    }

    @Benchmark
    public void blank() {
        target_blank();
    }

    @Benchmark
    public void dontinline() {
        target_dontInline();
    }

    @Benchmark
    public void inline() {
        target_inline();
    }

    @Benchmark
    public void exclude() {
        target_exclude();
    }

    /**
     * 观察到 baseline, blank, inline 的性能差距不大。
     * don't inline 有一些差距，因为每次都有方法调用而非内联。
     * exclude 非常慢，因为它压根儿没编译。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_16_CompilerControl.class.getSimpleName())
                .warmupIterations(0)
                .measurementIterations(3)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}