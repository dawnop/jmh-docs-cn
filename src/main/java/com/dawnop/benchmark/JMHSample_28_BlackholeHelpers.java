package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


/**
 * 有时候你需要不在 @Benchmark 方法里的 black hole，
 * 而是辅助方法里的 black hole，
 * 因为你想在辅助方法中动态实现某个接口，并在实例中调用 black hole。
 * 这时候，你可以直接在辅助方法的方法签名上加上 Blackhole。
 * 这种技巧在 @Setup 和 @TearDown 方法，
 * 以及其他的 JMH 中的对象（比如 Control）中同样适用。
 * <p>
 * 这是 {@link JMHSample_08_DeadCode} 的变体，
 * 但是外层包裹了一层匿名类。
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class JMHSample_28_BlackholeHelpers {


    public interface Worker {
        void work();
    }

    private Worker workerBaseline;
    private Worker workerRight;
    private Worker workerWrong;

    @Setup
    public void setup(final Blackhole bh) {
        workerBaseline = new Worker() {
            double x;

            @Override
            public void work() {
                // do nothing
            }
        };

        workerWrong = new Worker() {
            double x;

            @Override
            public void work() {
                Math.log(x);
            }
        };

        workerRight = new Worker() {
            double x;

            @Override
            public void work() {
                bh.consume(Math.log(x));
            }
        };

    }

    @Benchmark
    public void baseline() {
        workerBaseline.work();
    }

    @Benchmark
    public void measureWrong() {
        workerWrong.work();
    }

    @Benchmark
    public void measureRight() {
        workerRight.work();
    }

    /*
     * 你可以看到 measureWrong() 和 baseline() 的开销差不多。
     * 而 measureRight() 的开销则大一些。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_28_BlackholeHelpers.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
