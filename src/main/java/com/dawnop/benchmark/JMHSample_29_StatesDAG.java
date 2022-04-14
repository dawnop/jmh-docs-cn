package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


/**
 * 警告：这是一个实验性质的特性，我们有可能将其移除并不进行通知！
 * <p>
 * 当有多个 @State 对象相互引用时，要注意可能出现循环依赖的问题。
 * JMH 允许 @State 对象以 DAG (directed acyclic graph) 的方式引用彼此，
 * 可以通过 @State 对象的 helper 方法进行变量传递，引用另一个 @State 对象。
 * {@link JMHSample_28_BlackholeHelpers} 只是本例的一种特例。
 * <p>
 * 某个对象的 @Setup 方法引用的其他 @State 对象，其他对象都可以在本对象创建之前初始化。
 * 这就是 DAG 的性质。同样的，@TearDown 方法引用的所有 @State
 * 对象都不会在本对象之前调用其他对象的 @TearDown 方法。
 * <p>
 * 下面的例子中的依赖关系是：（-> 为依赖关系）
 * Local -> Shared -> Counter。
 * <p>
 * PS: 这个例子只是拿来说明问题，工程中不建议这么写。
 */


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class JMHSample_29_StatesDAG {


    public static class Counter {
        int x;

        public int inc() {
            return x++;
        }

        public void dispose() {
            // 假装这里做了有用的事情。
        }
    }


    @State(Scope.Benchmark)
    public static class Shared {
        List<Counter> all;
        Queue<Counter> available;

        /**
         * Shared 维护了一堆的 Counter，运行的时候会把它们 poll 出来，送给 Local。
         * setup 方法只会调用一次，初始化完成所有 @State。
         * 后续 Local 方法会间接地获取 Shared 中的 Counter 方法。
         */
        @Setup
        public synchronized void setup() {
            all = new ArrayList<>();
            for (int c = 0; c < 10; c++) {
                all.add(new Counter());
            }

            available = new LinkedList<>();
            available.addAll(all);
        }

        @TearDown
        public synchronized void tearDown() {
            for (Counter c : all) {
                c.dispose();
            }
        }

        public synchronized Counter getMine() {
            return available.poll();
        }
    }

    @State(Scope.Thread)
    public static class Local {
        Counter cnt;

        @Setup
        public void setup(Shared shared) {
            cnt = shared.getMine();
        }
    }

    @Benchmark
    public int test(Local local) {
        return local.cnt.inc();
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_29_StatesDAG.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}