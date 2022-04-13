package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * 有时需要评估状态不稳定的的方法，每次调用测试的方法的开销差异极大。
 * <p>
 * 这种情况下，通过定时测量不是个好的选择，测量单次运行时间还可以接受。
 * 但是另一方面，要测试的东西太小了，单次测量因为误差很难得到有效的数据。
 * <p>
 * 我们可以使用 batchSize 参数来指定调用的次数。
 * <p>
 * 不通过手动循环的方式来执行每次 {@link Benchmark} 方法的调用，
 * 以免出现 {@link JMHSample_11_Loops} 中提到的问题。
 * 含有 @Setup/@TearDown(Level.Invocation) 注解的方法，
 * 每次进行 @Benchmark 方法调用的前后都会执行。
 */

@State(Scope.Thread)
public class JMHSample_26_BatchSize {

    List<String> list = new LinkedList<>();

    /*
     * 假设我们想向 list 中间插入数据。
     */

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @BenchmarkMode(Mode.AverageTime)
    public List<String> measureWrong_1() {
        list.add(list.size() / 2, "something");
        return list;
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5)
    @Measurement(iterations = 5, time = 5)
    @BenchmarkMode(Mode.AverageTime)
    public List<String> measureWrong_5() {
        list.add(list.size() / 2, "something");
        return list;
    }

    /**
     * 正确。
     */
    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    @BenchmarkMode(Mode.SingleShotTime)
    public List<String> measureRight() {
        list.add(list.size() / 2, "something");
        return list;
    }

    @Setup(Level.Iteration)
    public void setup() {
        list.clear();
    }

    /**
     * measureWrong_1() 和 measureWrong_5() 的测试结果有很明显的差异。
     * 这是因为被测方法消耗的时间跟运行的次数是线性关系，随着运行时间越多，
     * 每次调用被测方法消耗的时间也就越多。
     * measureRight() 并没有这种问题，因为它的执行次数是固定的。
     * 每 5000 次调用算是一次 iteration。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_26_BatchSize.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}