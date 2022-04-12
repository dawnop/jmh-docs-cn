package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.*;

import static java.lang.Integer.*;

/**
 * Fixture 方法有许多不同的等级来控制何时运行。
 * <p>
 * {@link Level#Invocation} 适用于每次 @Benchmark 方法被调用的前后。
 * 它并不被算作时间消耗。例如可以使用 sleep 来控制每次调用的间隔。
 * <p>
 * 使用了 Level.Invocation 之后，打时间戳和线程同步的开销会显著的影响测试结果。
 * 请小心使用。
 */
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class JMHSample_07_FixtureLevelInvocation {

    /**
     * 这个 State 对象保存着 Executor 的引用。
     * 在 Trial 的级别下，Executor 伴随着整个测试的过程。
     */

    @State(Scope.Benchmark)
    public static class NormalState {
        ExecutorService service;

        @Setup(Level.Trial)
        public void up() {
            service = Executors.newCachedThreadPool();
        }

        @TearDown(Level.Trial)
        public void down() {
            service.shutdown();
        }

    }

    /**
     * LaggingState 继承了 NormalState，
     * 同时多了一个 Invocation 级别的 fixture 方法，用以控制每次调用之间的间隔。
     */

    public static class LaggingState extends NormalState {
        public static final int SLEEP_TIME = getInteger("sleepTime", 10);

        @Setup(Level.Invocation)
        public void lag() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
        }
    }

    /**
     * 于是就有了如下的操作：
     * 当每次 submit 没有间隔时，得到的是热启动的运行结果。
     * 当每次 submit 之间有一定间隔时，得到的是冷启动的运行结果。
     * <p>
     * 差别在于线程唤醒的开销。
     */

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public double measureHot(NormalState e, final Scratch s) throws ExecutionException, InterruptedException {
        return e.service.submit(new Task(s)).get();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public double measureCold(LaggingState e, final Scratch s) throws ExecutionException, InterruptedException {
        return e.service.submit(new Task(s)).get();
    }

    /**
     * 把 doWork 封装成对象以放入 Callable 对象中。
     */

    @State(Scope.Thread)
    public static class Scratch {
        private double p;

        public double doWork() {
            p = Math.log(p);
            return p;
        }
    }

    public static class Task implements Callable<Double> {
        private Scratch s;

        public Task(Scratch s) {
            this.s = s;
        }

        @Override
        public Double call() {
            return s.doWork();
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_07_FixtureLevelInvocation.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}