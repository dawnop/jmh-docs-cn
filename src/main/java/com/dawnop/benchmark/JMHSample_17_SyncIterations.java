package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JMHSample_17_SyncIterations {

    private double src;

    /**
     * 如果使用多线程运行基准测试，启动和终止线程的开销极大的影响了性能。
     * <p>
     * 一种自然的想法是，把所有的线程按顺序放在起跑线上，一声令下全部出发。
     * 但是并不能保证所有的线程同时出发，会影响测试结果。
     * <p>
     * 最好的方法是引入缓冲任务，唤醒线程时让线程执行缓冲任务，
     * 然后原子地让系统开始测试任务。这种做法同样可以用于终止线程。
     * 这听起来很复杂，但是 JMH 已经提供了相应的解决方案。
     */

    @Benchmark
    public double test() {
        double s = src;
        for (int i = 0; i < 1000; i++) {
            s = Math.sin(s);
        }
        return s;
    }


    /**
     * 需要远超 CPU 核心数的线程有比较明显的效果。
     * 不饱和的线程也可以看出来。
     * <p>
     * 关闭了 -si 性能会更不稳定，即使有时候会有比较好的性能表现。
     * 这是一种不正确的优化，是由单独执行的线程得到的。
     * 开启了 -si 的版本性能会更加稳定、连贯。
     * <p>
     * -si 默认开启。
     * <p>
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_17_SyncIterations.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .measurementTime(TimeValue.seconds(1))
                .threads(Runtime.getRuntime().availableProcessors() * 16)
                .forks(1)
                .syncIterations(true) // try to switch to "false"
                .build();

        new Runner(opt).run();
    }

}