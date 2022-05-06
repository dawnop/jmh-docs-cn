package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * 这个例子用来展示含有随机数的基准测试中的问题。
 * <p>
 * 假设我们打算测一测冒泡排序的时间开销，
 * 用随机数初始化数组，并多次进行冒泡排序，这样是很傻的。
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
public class JMHSample_38_PerInvokeSetup {

    private void bubbleSort(byte[] b) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int c = 0; c < b.length - 1; c++) {
                if (b[c] > b[c + 1]) {
                    byte t = b[c];
                    b[c] = b[c + 1];
                    b[c + 1] = t;
                    changed = true;
                }
            }
        }
    }

    /**
     * 我们将在下面的测试中使用 Data 作为数据源。
     */
    @State(Scope.Benchmark)
    public static class Data {

        @Param({"1", "16", "256"})
        int count;

        byte[] arr;

        @Setup
        public void setup() {
            arr = new byte[count];
            Random random = new Random(1234);
            random.nextBytes(arr);
        }
    }

    /**
     * 这种调用的方式是错误的。对于冒泡排序来说，已经排序好的数组往往会消耗更少的时间。
     * 初次调用就已经将数组排好序了，后续的基准测试执行时间会不准确。
     * <p>
     * 可以在 {@link Level#Invocation} 的 @setup 方法中对拷贝被测元素的赋本。
     * 但是这样会碰到 {@link Level#Invocation} 中提到的问题，尝试此方法之前请务必阅读
     * {@link Level#Invocation}。
     */
    @Benchmark
    public byte[] measureWrong(Data d) {
        bubbleSort(d.arr);
        return d.arr;
    }


    @State(Scope.Thread)
    public static class DataCopy {
        byte[] copy;

        @Setup(Level.Invocation)
        public void setup2(Data d) {
            copy = Arrays.copyOf(d.arr, d.arr.length);
        }
    }

    @Benchmark
    public byte[] measureNeutral(DataCopy d) {
        bubbleSort(d.copy);
        return d.copy;
    }

    /**
     * 大多数情况下，将每次重置数组的操作放入基准测试的被测代码中是比较好的选择，
     * 尤其是被测代码本身的执行时间远超重置数组的时间。
     */

    @Benchmark
    public byte[] measureRight(Data d) {
        byte[] c = Arrays.copyOf(d.arr, d.arr.length);
        bubbleSort(c);
        return c;
    }

    /*
        Benchmark                                   (count)  Mode  Cnt      Score     Error  Units
        JMHSample_38_PerInvokeSetup.measureWrong          1  avgt   25      2.408 ±   0.011  ns/op
        JMHSample_38_PerInvokeSetup.measureWrong         16  avgt   25      8.286 ±   0.023  ns/op
        JMHSample_38_PerInvokeSetup.measureWrong        256  avgt   25     73.405 ±   0.018  ns/op
        JMHSample_38_PerInvokeSetup.measureNeutral        1  avgt   25     15.835 ±   0.470  ns/op
        JMHSample_38_PerInvokeSetup.measureNeutral       16  avgt   25    112.552 ±   0.787  ns/op
        JMHSample_38_PerInvokeSetup.measureNeutral      256  avgt   25  58343.848 ± 991.202  ns/op
        JMHSample_38_PerInvokeSetup.measureRight          1  avgt   25      6.075 ±   0.018  ns/op
        JMHSample_38_PerInvokeSetup.measureRight         16  avgt   25    102.390 ±   0.676  ns/op
        JMHSample_38_PerInvokeSetup.measureRight        256  avgt   25  58812.411 ± 997.951  ns/op

        我们可以看到 measureWrong 的结果问题很大，因为它太快了。
        measureNeutral 的结果没有太大的问题，但是相较于 measureRight，
        measureNeutral 的额外开销更不稳定，收操作系统和线程的影响更大，
        因此选择 measureRight 是较优的方式。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + JMHSample_38_PerInvokeSetup.class.getSimpleName() + ".*")
                .build();

        new Runner(opt).run();
    }
}