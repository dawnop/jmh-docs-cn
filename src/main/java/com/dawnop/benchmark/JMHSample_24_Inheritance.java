package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 在有些特殊的情况下，你可能希望 @Benchmark 方法在父类中（有可能是抽象类），
 * 但是 @Benchmark 方法调用的某个方法需要子类来具体实现。
 * <p>
 * 根据已有的经验，如果某个类含有 @Benchmark 方法，那它所有的子类应该也有相同的方法。
 * 警告：只有在编译期之前添加的子类，编译时才能生成相应的基准测试代码。
 * 比如像 Lombok 类似的直接修改语法树的插件，生成的子类的是无效的。
 * 比如像运行时 Java 的反射，生成的子类也是无效的。
 * <p>
 * 注解的效力遵循就近原则，加在方法上的注解优先于加在类上的注解。
 */
public class JMHSample_24_Inheritance {


    @BenchmarkMode(Mode.AverageTime)
    @Fork(1)
    @State(Scope.Thread)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static abstract class AbstractBenchmark {
        int x;

        @Setup
        public void setup() {
            x = 42;
        }

        @Benchmark
        @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
        @Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
        public double bench() {
            return doWork() * doWork();
        }

        protected abstract double doWork();
    }

    public static class BenchmarkLog extends AbstractBenchmark {
        @Override
        protected double doWork() {
            return Math.log(x);
        }
    }

    public static class BenchmarkSin extends AbstractBenchmark {
        @Override
        protected double doWork() {
            return Math.sin(x);
        }
    }

    public static class BenchmarkCos extends AbstractBenchmark {
        @Override
        protected double doWork() {
            return Math.cos(x);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_24_Inheritance.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
