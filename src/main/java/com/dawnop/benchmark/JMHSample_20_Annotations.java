package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


/**
 * 除了命令行参数之外，我们可以通过添加注解的方式为各种基准测试提供参数默认值。
 * 当有大量的基准测试时并且参数各不相同时，注解非常方便。
 * <p>
 * 注解可以加在类上，影响整个类中所有的 @Benchmark 方法。
 * 注解的生效顺序是就近原则，方法上的注解覆盖类上注解。
 */

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
public class JMHSample_20_Annotations {

    double x1 = Math.PI;


    @Benchmark
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public double measure() {
        return Math.log(x1);
    }

    /**
     * JMH 中的参数大都有默认值，可以通过 {@link Options} 或者命令行修改。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_20_Annotations.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}