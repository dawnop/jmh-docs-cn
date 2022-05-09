# JMH 中文文档

## Introduction

### JMH是什么

> JMH（Java Microbenchmark Harness）是Java用来做基准测试一个工具，该工具由openJDK提供并维护，精度可以达到纳秒级。该工具是由 Oracle 内部实现 JIT 的大牛们编写的，他们应该比任何人都了解 JIT 以及 JVM 对于基准测试的影响。

### 基准测试

> 通过设计合理的测试方法，选用合适的测试工具和被测系统，实现对某个特定目的场景中某项性能指标进行定量的测试。

## 快速开始

### 依赖引入

自**JDK9**开始，**OpenJDK**自带JMH工具，若在其他JDK或1.8以下版本，需要添加依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.openjdk.jmh</groupId>
        <artifactId>jmh-core</artifactId>
        <version>1.35</version>
    </dependency>
    <dependency>
        <groupId>org.openjdk.jmh</groupId>
        <artifactId>jmh-generator-annprocess</artifactId>
        <version>1.35</version>
    </dependency>
</dependencies>
```

### HelloWorld

```java
@Benchmark
public void wellHelloThere() {
    // this method was intentionally left blank.
}
public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
            .include(JMHSample_01_HelloWorld.class.getSimpleName())
            .forks(1)
            .build();
    new Runner(opt).run();
}
```

### 运行结果

```shell
# Run progress: 0.00% complete, ETA 00:01:40
# Fork: 1 of 1
# Warmup Iteration   1: 3784235476.488 ops/s
# Warmup Iteration   2: 3892337311.171 ops/s
# Warmup Iteration   3: 3897471620.777 ops/s
# Warmup Iteration   4: 3895720743.071 ops/s
# Warmup Iteration   5: 3867401304.801 ops/s
Iteration   1: 3853931025.933 ops/s
Iteration   2: 3896916857.877 ops/s
Iteration   3: 3883980029.206 ops/s
Iteration   4: 3867538916.343 ops/s
Iteration   5: 3760621239.939 ops/s


Result "com.dawnop.benchmark.JMHSample_01_HelloWorld.wellHelloThere":
  3852597613.860 ±(99.9%) 207664741.991 ops/s [Average]
  (min, avg, max) = (3760621239.939, 3852597613.860, 3896916857.877), stdev = 53929874.027
  CI (99.9%): [3644932871.869, 4060262355.851] (assumes normal distribution)


# Run complete. Total time: 00:01:40

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                Mode  Cnt           Score           Error  Units
JMHSample_01_HelloWorld.wellHelloThere  thrpt    5  3852597613.860 ± 207664741.991  ops/s
```

对JVM进行预热后执行测试代码，共执行了5次，平均执行次数为每秒3852597613次，误差为每秒207664741次，误差不到0.1%。

## 案例

1. [HelloWorld](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_01_HelloWorld.java)，简单介绍如何运行
2. [BenchmarkMode](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_02_BenchmarkMode.java)，介绍可选的测量模式，可以依据固定时间测量执行测试，也可以测量每次执行的平均时间
3. [States](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_03_States.java)，如何维护一些测量时的状态，可以认为是测量时共享的变量
4. [DefaultState](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_04_DefaultState.java)，像引用字段一样引用状态
5. [StateFixture](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_05_StateFixture.java)，`@Setup`和`@TearDown`可以指定 `@Benchmark` 方法执行前后的额外操作。
6. [FixtureLevel](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_06_FixtureLevel.java)，`@Setup`和`@TearDown` 执行的时机。
7. [FixtureLevelInvocation](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_07_FixtureLevelInvocation.java)，`Level.Invocation` 的使用。
8. [DeadCode](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_08_DeadCode.java)，避免死代码优化。
9. [Blackholes](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_09_Blackholes.java)，使用`Blackhole`避免死代码优化。
10. [ConstantFold](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_10_ConstantFold.java)，避免常量折叠。
11. [Loops](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_11_Loops.java)，避免在基准测试中使用循环。
12. [Forking](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_12_Forking.java)，运行多个JVM实例以隔离基准测试。
13. [RunToRun](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_13_RunToRun.java)，对比不同JVM实例运行差异。
14. 
15. [Asymmetric](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_15_Asymmetric.java)，多线程分别执行不同的代码。
16. [CompilerControl](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_16_CompilerControl.java)，使用`@CompilerControl`避免方法内联。
17. [SyncIterations](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_17_SyncIterations.java)，多线程执行时，线程的开销。
18. [Control](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_18_Control.java)，运行时检查JMH的系统状态。
19. 
20. [Annotations](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_20_Annotations.java)，JMH中常用的注解。
21. [ConsumeCPU](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_21_ConsumeCPU.java)，使得CPU空转指定的时间。
22. [FalseSharing](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_22_FalseSharing.java)，避免Java中的伪共享。
23. [AuxCounters](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_23_AuxCounters.java)，手动指定性能分析的参考数据。
24. [Inheritance](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_24_Inheritance.java)，基准测试时要避免编译器对抽象语法树的修改，例如`Lombok`。
25. [API_GA](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_25_API_GA.java)，使用遗传算法对JVM参数调优。
26. [BatchSize](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_26_BatchSize.java)，使用 `batchSize` 参数指定`AverageTime` 模式下一次执行的基准测试数。
27. [Param](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_27_Param.java)，使用`@Param` 参数化基准测试测试，使得每次基准测试执行不同的参数。
28. [BlackholeHelpers](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_28_BlackholeHelpers.java)，在`@Setup`和`@TearDown`方法中使用 `Blackhole`。
29. [StatesDAG](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_29_StatesDAG.java)，指明状态之间可行的依赖关系，不允许循环依赖。
30. [Interrupts](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_30_Interrupts.java)，JMH可以自动解除多线程死锁。
31. [InfraParams](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_31_InfraParams.java)，如何在基准测试中获取系统参数。
32. [BulkWarmup](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_32_BulkWarmup.java)，JVM的优化结果与预热阶段执行的代码有关。预热情况有干扰会极大影响优化结果。
33. [SecurityManager](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_33_SecurityManager.java)，如何在JMH中使用Java 的`SecurityManager`
34. [SafeLooping](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_34_SafeLooping.java)，如何正确使用循环，避免循环展开优化。
35. [Profilers](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_35_Profilers.java)，使用JMH自带的分析工具对测试进行细粒度分析。
36. [BranchPrediction](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_36_BranchPrediction.java)，如何避免分支预测。
37. [CacheAccess](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_37_CacheAccess.java)，应当熟悉内存的局部化原理。
38. [PerInvokeSetup](https://github.com/dawnop/jmh-docs-cn/blob/master/src/main/java/com/dawnop/benchmark/JMHSample_38_PerInvokeSetup.java)，展示一种每次基准测试都需要初始化状态的情形。
