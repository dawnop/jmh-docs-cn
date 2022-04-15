package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 有时候你需要深入了解系统状态变化的信息，为此，我们需要一个实验性质的状态对象：
 * Control，这个对象由 JMH 来更新状态。用于将重要信息从 JMH 传达给被测对象 。
 * <p>
 * 在这里例子里，我们想估算简单的 AtomicBoolean 对象状态来回变化的速度。
 * 不幸的是，朴素的实现方式会使得一个线程处于活锁的状态。
 * 因为执行 ping/pong 的过程并不是完美一来一回。
 * 如果其中一个线程已经不再工作，我们需要终止另一个线程。
 */

@State(Scope.Group)
public class JMHSample_18_Control {


    public final AtomicBoolean flag = new AtomicBoolean();

    @Benchmark
    @Group("pingpong")
    public void ping(Control cnt) {
        while (!cnt.stopMeasurement && !flag.compareAndSet(false, true)) {
            // 刻意留白
        }
    }

    @Benchmark
    @Group("pingpong")
    public void pong(Control cnt) {
        while (!cnt.stopMeasurement && !flag.compareAndSet(true, false)) {
            // 刻意留白
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_18_Control.class.getSimpleName())
                .threads(2)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
