package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class JMHSample_05_StateFixtures {

    double x;

    /**
     * Setup 默认每个 @Benchmark 前执行
     * TearDown 默认每个 @Benchmark 后执行
     * Setup 和 TearDown 称之为 fixture 方法。
     * <p>
     * {@link State} 对象一直存在于基准测试的整个生命周期中，这有助于进行状态管理。
     * 有种东西叫 fixture 方法，你可能在 JUnit 和 TestNG 中见过它。
     * <p>
     * Fixture 方法只在 State 对象中生效，在其他地方编译会报错。
     * <p>
     * 与 State 一样，fixture 方法仅仅在访问状态的线程中被调用。
     * 基准测试线程在访问状态时会执行对应的 fixture 方法。
     * <p>
     * fixture 方法同样可以在静态字段上生效，尽管这种行为脱离了状态管理。
     * 这种情况下遵循基本的 Java 语义（意味着同一个类的所有实例共享静态字段）
     */

    @Setup
    public void prepare() {
        x = Math.PI;
    }

    /**
     * 检查静态变量是否更改
     */

    @TearDown
    public void check() {
        assert x > Math.PI : "Nothing changed?";
    }

    /**
     * 这个方法做得对，对 State 对象的字段 x 进行自增操作。
     * check() 方法将会断言成功，因为 benchmark 至少进行了一次调用。
     */

    @Benchmark
    public void measureRight() {
        x++;
    }

    /**
     * 这个方法之后的 check() 断言将会失败，因为自增的是本地变量的 x。
     * 同时 JMH 会运行失败。
     */

    @Benchmark
    public void measureWrong() {
        double x = 0;
        x++;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_05_StateFixtures.class.getSimpleName())
                .forks(1)
                .jvmArgs("-ea")
                .build();

        new Runner(opt).run();
    }
}
