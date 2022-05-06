package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.ClassloaderProfiler;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.LinuxPerfProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 这个例子用来介绍分析器（profiler）。
 * <p>
 * JMH 有很多方便的分析器来对基准测试进行分析。
 * 尽管这些分析器不能代替成熟的外部分析器，但很多情况下适合对基准测试快速分析。
 * 当你频繁的调整基准测试的代码时，快速分析是很重要的。
 * <p>
 * 用 -lprof 参数来列出所有的 profiler。种类比较多，目前只演示几个比较常用的。
 * 每个分析器都有自己的选项，可以用过 -prof <profiler-name>:help 获取使用方法。
 * <p>
 * 由于分析器分析的方面不同，很难在一个例子中展示所有的 profiler。
 * 因此一共有好几个例子。
 */
public class JMHSample_35_Profilers {


    /*
     * ================================ MAPS BENCHMARK ================================
     */

    @State(Scope.Thread)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class Maps {
        private Map<Integer, Integer> map;

        @Param({"hashmap", "treemap"})
        private String type;

        private int begin;
        private int end;

        @Setup
        public void setup() {
            switch (type) {
                case "hashmap":
                    map = new HashMap<>();
                    break;
                case "treemap":
                    map = new TreeMap<>();
                    break;
                default:
                    throw new IllegalStateException("Unknown type: " + type);
            }

            begin = 1;
            end = 256;
            for (int i = begin; i < end; i++) {
                map.put(i, i);
            }
        }

        @Benchmark
        public void test(Blackhole bh) {
            for (int i = begin; i < end; i++) {
                bh.consume(map.get(i));
            }
        }

        public static void main(String[] args) throws RunnerException {
            Options opt = new OptionsBuilder()
                    .include(JMHSample_35_Profilers.Maps.class.getSimpleName())
                    .addProfiler(StackProfiler.class)
//                    .addProfiler(GCProfiler.class)
                    .build();

            new Runner(opt).run();
        }

        /*
            运行之后会有如下的结果

              Benchmark                              (type)  Mode  Cnt     Score    Error   Units
              JMHSample_35_Profilers.Maps.test     hashmap  avgt    5  1553.201 ±   6.199   ns/op
              JMHSample_35_Profilers.Maps.test     treemap  avgt    5  5177.065 ± 361.278   ns/op

            运行 -prof stack 之后会展示堆栈信息。

              ....[Thread state: RUNNABLE]........................................................................
               99.0%  99.0% org.openjdk.jmh.samples.JMHSample_35_Profilers$Maps.test
                0.4%   0.4% org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Maps_test.test_avgt_jmhStub
                0.2%   0.2% sun.reflect.NativeMethodAccessorImpl.invoke0
                0.2%   0.2% java.lang.Integer.valueOf
                0.2%   0.2% sun.misc.Unsafe.compareAndSwapInt

              ....[Thread state: RUNNABLE]........................................................................
               78.0%  78.0% java.util.TreeMap.getEntry
               21.2%  21.2% org.openjdk.jmh.samples.JMHSample_35_Profilers$Maps.test
                0.4%   0.4% java.lang.Integer.valueOf
                0.2%   0.2% sun.reflect.NativeMethodAccessorImpl.invoke0
                0.2%   0.2% org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Maps_test.test_avgt_jmhStub


            堆栈分析器用来查看我们关心的方法有没有运行。像其他的采样分析器一样，会有样本偏差。
            它很有可能没有注意到开销比较小的方法，比如 HashMap.get。


            然后是 GC 分析器。通过 -prof gc 运行。

              Benchmark                                                            (type)  Mode  Cnt    Score     Error   Units
              JMHSample_35_Profilers.Maps.test                                   hashmap  avgt    5  1553.201 ±   6.199   ns/op
              JMHSample_35_Profilers.Maps.test:·gc.alloc.rate                    hashmap  avgt    5  1257.046 ±   5.675  MB/sec
              JMHSample_35_Profilers.Maps.test:·gc.alloc.rate.norm               hashmap  avgt    5  2048.001 ±   0.001    B/op
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Eden_Space           hashmap  avgt    5  1259.148 ± 315.277  MB/sec
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Eden_Space.norm      hashmap  avgt    5  2051.519 ± 520.324    B/op
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Survivor_Space       hashmap  avgt    5     0.175 ±   0.386  MB/sec
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Survivor_Space.norm  hashmap  avgt    5     0.285 ±   0.629    B/op
              JMHSample_35_Profilers.Maps.test:·gc.count                         hashmap  avgt    5    29.000            counts
              JMHSample_35_Profilers.Maps.test:·gc.time                          hashmap  avgt    5    16.000                ms
              JMHSample_35_Profilers.Maps.test                                   treemap  avgt    5  5177.065 ± 361.278   ns/op
              JMHSample_35_Profilers.Maps.test:·gc.alloc.rate                    treemap  avgt    5   377.251 ±  26.188  MB/sec
              JMHSample_35_Profilers.Maps.test:·gc.alloc.rate.norm               treemap  avgt    5  2048.003 ±   0.001    B/op
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Eden_Space           treemap  avgt    5   392.743 ± 174.156  MB/sec
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Eden_Space.norm      treemap  avgt    5  2131.767 ± 913.941    B/op
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Survivor_Space       treemap  avgt    5     0.131 ±   0.215  MB/sec
              JMHSample_35_Profilers.Maps.test:·gc.churn.PS_Survivor_Space.norm  treemap  avgt    5     0.709 ±   1.125    B/op
              JMHSample_35_Profilers.Maps.test:·gc.count                         treemap  avgt    5    25.000            counts
              JMHSample_35_Profilers.Maps.test:·gc.time                          treemap  avgt    5    26.000                ms

            我们可以看到这个测试运行时会产生很多垃圾，gc.alloc 告诉我们，每秒分配 1257 MB / 377 MB 的空间，
            或者每次基准操作分配 2048 bytes 空间。gc.churn 告诉我们，GC 每秒从 Eden 中回收差不多大小的空间。
            换句话说，我们每次基准操作都会产生 2048 bytes 的垃圾。

            如果你仔细地研究了这个测试，你就会发现这差不多就是自动装箱的开销。

            gc.alloc 通常会产生更精准的数据，但是如果线程切换非常频繁，数据就可能会失效。
            gc.churn 的值会随着 GC 事件而更新，所以如果你想获得更精确的数据，可以尝试更长的运行时间/更小的堆空间。
            记住，同时参考 gc.alloc 和 gc.churn 往往可以获取更加全面的数据。

            分析器的其他数据往往和被测代码的性能有关，treemap 的时间消耗是 hashmap 的三倍，
            所以分配内存和回收的速度也要相应地慢一些。
            查看分析器的数据，看看是否因为内存分配 / GC 限制了性能。

            和大多数分析器一样，stack 和 gc 分析器都可以汇总来自多个 fork 的数据。
            在启用分析器的情况下 fork 多个实例有助于改善结果的误差。
        */
    }

    /*
     * ================================ CLASSLOADER BENCHMARK ================================
     */


    @State(Scope.Thread)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(3)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class Classy {

        /**
         * 简单的类加载器，只能重复加载同一个类。
         */
        public static class XLoader extends URLClassLoader {
            private static final byte[] X_BYTECODE = new byte[]{
                    (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0x00, 0x00, 0x00, 0x34, 0x00, 0x0D, 0x0A, 0x00, 0x03, 0x00,
                    0x0A, 0x07, 0x00, 0x0B, 0x07, 0x00, 0x0C, 0x01, 0x00, 0x06, 0x3C, 0x69, 0x6E, 0x69, 0x74, 0x3E, 0x01, 0x00, 0x03,
                    0x28, 0x29, 0x56, 0x01, 0x00, 0x04, 0x43, 0x6F, 0x64, 0x65, 0x01, 0x00, 0x0F, 0x4C, 0x69, 0x6E, 0x65, 0x4E, 0x75,
                    0x6D, 0x62, 0x65, 0x72, 0x54, 0x61, 0x62, 0x6C, 0x65, 0x01, 0x00, 0x0A, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65, 0x46,
                    0x69, 0x6C, 0x65, 0x01, 0x00, 0x06, 0x58, 0x2E, 0x6A, 0x61, 0x76, 0x61, 0x0C, 0x00, 0x04, 0x00, 0x05, 0x01, 0x00,
                    0x01, 0x58, 0x01, 0x00, 0x10, 0x6A, 0x61, 0x76, 0x61, 0x2F, 0x6C, 0x61, 0x6E, 0x67, 0x2F, 0x4F, 0x62, 0x6A, 0x65,
                    0x63, 0x74, 0x00, 0x20, 0x00, 0x02, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x04, 0x00,
                    0x05, 0x00, 0x01, 0x00, 0x06, 0x00, 0x00, 0x00, 0x1D, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x2A,
                    (byte) 0xB7, 0x00, 0x01, (byte) 0xB1, 0x00, 0x00, 0x00, 0x01, 0x00, 0x07, 0x00, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00,
                    0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x08, 0x00, 0x00, 0x00, 0x02, 0x00, 0x09,
            };

            public XLoader() {
                super(new URL[0], ClassLoader.getSystemClassLoader());
            }

            @Override
            protected Class<?> findClass(final String name) throws ClassNotFoundException {
                return defineClass(name, X_BYTECODE, 0, X_BYTECODE.length);
            }

        }

        @Benchmark
        public Class<?> load() throws ClassNotFoundException {
            return Class.forName("X", true, new XLoader());
        }

        public static void main(String[] args) throws RunnerException {
            Options opt = new OptionsBuilder()
                    .include(JMHSample_35_Profilers.Classy.class.getSimpleName())
                    .addProfiler(ClassloaderProfiler.class)
//                    .addProfiler(CompilerProfiler.class)
                    .build();

            new Runner(opt).run();
        }

        /*
            通过参数 -prof cl 运行类加载分析器。

                Benchmark                                              Mode  Cnt      Score      Error        Units
                JMHSample_35_Profilers.Classy.load                     avgt   15  34215.363 ±  545.892        ns/op
                JMHSample_35_Profilers.Classy.load:·class.load         avgt   15  29374.097 ±  716.743  classes/sec
                JMHSample_35_Profilers.Classy.load:·class.load.norm    avgt   15      1.000 ±    0.001   classes/op
                JMHSample_35_Profilers.Classy.load:·class.unload       avgt   15  29598.233 ± 3420.181  classes/sec
                JMHSample_35_Profilers.Classy.load:·class.unload.norm  avgt   15      1.008 ±    0.119   classes/op

            现在，我们可以看到每次基准测试加载的类的数量。总共每秒有 29k 的类被加载了。
            我们可以看到运行时加载类的的速度和卸载类的速度差不多。

            这个分析器在做类加载性能的测试的时候很好用，因为它可以清晰地展示出类确实被加载了，
            而不是通过代码中的 Class.forName 进行验证。
            如果你在做一个没有类加载的测试，你会期望以上的指标都是 0。


            另一个很有用的分析器可以判断编译器是否在后台进行繁重的工作，判断是否干扰测量。
            通过 -prof comp 执行编译分析器。
            Another useful profiler that could tell if compiler is doing a heavy work in background, and thus interfering
            with measurement, -prof comp:

                Benchmark                                                   Mode  Cnt      Score      Error  Units
                JMHSample_35_Profilers.Classy.load                          avgt    5  33523.875 ± 3026.025  ns/op
                JMHSample_35_Profilers.Classy.load:·compiler.time.profiled  avgt    5      5.000                ms
                JMHSample_35_Profilers.Classy.load:·compiler.time.total     avgt    5    479.000                ms

            我们应该正处于正常的状态，总共有 479 ms 的编译工作，只有 5 ms 发生在测试时。
            即使在正常状态下也会有一定程度的后台编译影响测试。

            和大多数分析器一样，cl 和 comp 分析器都可以汇总来自多个 fork 的数据。
            在启用分析器的情况下 fork 多个实例有助于改善结果的误差。
         */
    }

    /*
     * ================================ ATOMIC LONG BENCHMARK ================================
     */

    @State(Scope.Benchmark)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class Atomic {
        private AtomicLong n;

        @Setup
        public void setup() {
            n = new AtomicLong();
        }

        @Benchmark
        public long test() {
            return n.incrementAndGet();
        }


        public static void main(String[] args) throws RunnerException {
            Options opt = new OptionsBuilder()
                    .include(JMHSample_35_Profilers.Atomic.class.getSimpleName())
                    .addProfiler(LinuxPerfProfiler.class)
//                    .addProfiler(LinuxPerfNormProfiler.class)
//                    .addProfiler(LinuxPerfAsmProfiler.class)
//                    .addProfiler(WinPerfAsmProfiler.class)
//                    .addProfiler(DTraceAsmProfiler.class)
                    .build();

            new Runner(opt).run();
        }
        /*
            处理纳秒级别的基准测试需要深入了解运行时、硬件和生成代码。
            幸运的是，JMH 提供了一些方便的工具，简单方便地使用而不用关注底层。
            如果你用的是 linux，标准包里应该已经提供了 perf_events。
            这个工具直接获取硬件数据，并像 JMH 一样展示出来。
            但是 linux 上的 perf 工具有较大的缺陷。
            若是 JMH 同时启动多个实例，会导致 perf 会统计所有被 fork 出来的 JVM 实例。
            通过 JMH 本身的工具则没有这个问题，通过 JMH 的 -prof 参数可以使得
            JMH 为每一个 JVM 实例单独 调用 perf 工具。例子如下：

                 Perf stats:
                                --------------------------------------------------
                       4172.776137 task-clock (msec)         #    0.411 CPUs utilized
                               612 context-switches          #    0.147 K/sec
                                31 cpu-migrations            #    0.007 K/sec
                               195 page-faults               #    0.047 K/sec
                    16,599,643,026 cycles                    #    3.978 GHz                     [30.80%]
                   <not supported> stalled-cycles-frontend
                   <not supported> stalled-cycles-backend
                    17,815,084,879 instructions              #    1.07  insns per cycle         [38.49%]
                     3,813,373,583 branches                  #  913.870 M/sec                   [38.56%]
                         1,212,788 branch-misses             #    0.03% of all branches         [38.91%]
                     7,582,256,427 L1-dcache-loads           # 1817.077 M/sec                   [39.07%]
                           312,913 L1-dcache-load-misses     #    0.00% of all L1-dcache hits   [38.66%]
                            35,688 LLC-loads                 #    0.009 M/sec                   [32.58%]
                   <not supported> LLC-load-misses:HG
                   <not supported> L1-icache-loads:HG
                           161,436 L1-icache-load-misses:HG  #    0.00% of all L1-icache hits   [32.81%]
                     7,200,981,198 dTLB-loads:HG             # 1725.705 M/sec                   [32.68%]
                             3,360 dTLB-load-misses:HG       #    0.00% of all dTLB cache hits  [32.65%]
                           193,874 iTLB-loads:HG             #    0.046 M/sec                   [32.56%]
                             4,193 iTLB-load-misses:HG       #    2.16% of all iTLB cache hits  [32.44%]
                   <not supported> L1-dcache-prefetches:HG
                                 0 L1-dcache-prefetch-misses:HG #    0.000 K/sec                   [32.33%]
                      10.159432892 seconds time elapsed


            我们可以通过进程间通信（IPC）看到这些数据，被测对象有大量的读写，
            事无巨细都记录了下来，但是这个数据有点复杂，不是那么直观。
            所以我们可以通过 -prof perfnorm 标准化这些数据。

                Benchmark                                                   Mode  Cnt   Score    Error  Units
                JMHSample_35_Profilers.Atomic.test                          avgt   15   6.551 ±  0.023  ns/op
                JMHSample_35_Profilers.Atomic.test:·CPI                     avgt    3   0.933 ±  0.026   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-load-misses   avgt    3   0.001 ±  0.022   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-loads         avgt    3  12.267 ±  1.324   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-store-misses  avgt    3   0.001 ±  0.006   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-stores        avgt    3   4.090 ±  0.402   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-icache-load-misses   avgt    3   0.001 ±  0.011   #/op
                JMHSample_35_Profilers.Atomic.test:·LLC-loads               avgt    3   0.001 ±  0.004   #/op
                JMHSample_35_Profilers.Atomic.test:·LLC-stores              avgt    3  ≈ 10⁻⁴            #/op
                JMHSample_35_Profilers.Atomic.test:·branch-misses           avgt    3  ≈ 10⁻⁴            #/op
                JMHSample_35_Profilers.Atomic.test:·branches                avgt    3   6.152 ±  0.385   #/op
                JMHSample_35_Profilers.Atomic.test:·bus-cycles              avgt    3   0.670 ±  0.048   #/op
                JMHSample_35_Profilers.Atomic.test:·context-switches        avgt    3  ≈ 10⁻⁶            #/op
                JMHSample_35_Profilers.Atomic.test:·cpu-migrations          avgt    3  ≈ 10⁻⁷            #/op
                JMHSample_35_Profilers.Atomic.test:·cycles                  avgt    3  26.790 ±  1.393   #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-load-misses        avgt    3  ≈ 10⁻⁴            #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-loads              avgt    3  12.278 ±  0.277   #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-store-misses       avgt    3  ≈ 10⁻⁵            #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-stores             avgt    3   4.113 ±  0.437   #/op
                JMHSample_35_Profilers.Atomic.test:·iTLB-load-misses        avgt    3  ≈ 10⁻⁵            #/op
                JMHSample_35_Profilers.Atomic.test:·iTLB-loads              avgt    3   0.001 ±  0.034   #/op
                JMHSample_35_Profilers.Atomic.test:·instructions            avgt    3  28.729 ±  1.297   #/op
                JMHSample_35_Profilers.Atomic.test:·minor-faults            avgt    3  ≈ 10⁻⁷            #/op
                JMHSample_35_Profilers.Atomic.test:·page-faults             avgt    3  ≈ 10⁻⁷            #/op
                JMHSample_35_Profilers.Atomic.test:·ref-cycles              avgt    3  26.734 ±  2.081   #/op

            习惯上会裁剪掉与本测试无关的数据，但是这里全都展示出来了。
            每次操作大约进行 12 次读取，8 次写入，大部分缓存都命中了。
            大约有 6 个分支，大部分分支预测也命中了。
            大约有 28 个指令在 27 个时钟周期内被执行完毕。
            如果是多线程运行，数据将会变得很有趣。例如 8 个线程。

                Benchmark                                                   Mode  Cnt    Score     Error  Units
                JMHSample_35_Profilers.Atomic.test                          avgt   15  143.595 ±   1.968  ns/op
                JMHSample_35_Profilers.Atomic.test:·CPI                     avgt    3   17.741 ±  28.761   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-load-misses   avgt    3    0.175 ±   0.406   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-loads         avgt    3   11.872 ±   0.786   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-store-misses  avgt    3    0.184 ±   0.505   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-dcache-stores        avgt    3    4.422 ±   0.561   #/op
                JMHSample_35_Profilers.Atomic.test:·L1-icache-load-misses   avgt    3    0.015 ±   0.083   #/op
                JMHSample_35_Profilers.Atomic.test:·LLC-loads               avgt    3    0.015 ±   0.128   #/op
                JMHSample_35_Profilers.Atomic.test:·LLC-stores              avgt    3    1.036 ±   0.045   #/op
                JMHSample_35_Profilers.Atomic.test:·branch-misses           avgt    3    0.224 ±   0.492   #/op
                JMHSample_35_Profilers.Atomic.test:·branches                avgt    3    6.524 ±   2.873   #/op
                JMHSample_35_Profilers.Atomic.test:·bus-cycles              avgt    3   13.475 ±  14.502   #/op
                JMHSample_35_Profilers.Atomic.test:·context-switches        avgt    3   ≈ 10⁻⁴             #/op
                JMHSample_35_Profilers.Atomic.test:·cpu-migrations          avgt    3   ≈ 10⁻⁶             #/op
                JMHSample_35_Profilers.Atomic.test:·cycles                  avgt    3  537.874 ± 595.723   #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-load-misses        avgt    3    0.001 ±   0.006   #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-loads              avgt    3   12.032 ±   2.430   #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-store-misses       avgt    3   ≈ 10⁻⁴             #/op
                JMHSample_35_Profilers.Atomic.test:·dTLB-stores             avgt    3    4.557 ±   0.948   #/op
                JMHSample_35_Profilers.Atomic.test:·iTLB-load-misses        avgt    3   ≈ 10⁻³             #/op
                JMHSample_35_Profilers.Atomic.test:·iTLB-loads              avgt    3    0.016 ±   0.052   #/op
                JMHSample_35_Profilers.Atomic.test:·instructions            avgt    3   30.367 ±  15.052   #/op
                JMHSample_35_Profilers.Atomic.test:·minor-faults            avgt    3   ≈ 10⁻⁵             #/op
                JMHSample_35_Profilers.Atomic.test:·page-faults             avgt    3   ≈ 10⁻⁵             #/op
                JMHSample_35_Profilers.Atomic.test:·ref-cycles              avgt    3  538.697 ± 590.183   #/op

            注意到这次运行的平均指令周期（Cycle Per Instruction）非常长，大概 17 个时钟周期一条指令！
            事实上，总共需要执行的指令数并没有太大变换，但是总共运行的时钟周期却变得非常长，大约 530 周期以上。
            我们可以从其他的数据上看出端倪：每个缓存级别多多少少都会有缓存未命中的问题。
            对于这样的数据，我们下定结论：出现了伪共享（缓存共享失效）的问题。
            事实上，AtomicLong 被 8 个线程激烈争夺。
            perfnorm 参数应该可以在多个 fork 中使用，这样还可以减少整体的误差。
            最后是 -prof perfasm。在处理粒度很细的基准测试时，查看生成的代码很重要，
            我们可以通过 PrintAssembly 转储所有的生成代码，但是这样很难分辨哪些是我们需要的。
            但是我们可以通过 perf 判断那些地址被大量重复访问，以判断生成代码的位置。
            -prof perfasm 会得到生成代码中最热的地方，并将 lock xadd 指向对代码中最热的指令。
            硬件计数器对指令地址并不是非常精确，因此有时候可能指向相邻的代码行。

                Hottest code regions (>10.00% "cycles" events):
                ....[Hottest Region 1]..............................................................................
                 [0x7f1824f87c45:0x7f1824f87c79] in org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@29 (line 201)
                                                                                  ; implicit exception: dispatches to 0x00007f1824f87d21
                                    0x00007f1824f87c25: test   %r11d,%r11d
                                    0x00007f1824f87c28: jne    0x00007f1824f87cbd  ;*ifeq
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@32 (line 201)
                                    0x00007f1824f87c2e: mov    $0x1,%ebp
                                    0x00007f1824f87c33: nopw   0x0(%rax,%rax,1)
                                    0x00007f1824f87c3c: xchg   %ax,%ax            ;*aload
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@13 (line 199)
                                    0x00007f1824f87c40: mov    0x8(%rsp),%r10
                  0.00%             0x00007f1824f87c45: mov    0xc(%r10),%r11d    ;*getfield n
                                                                                  ; - org.openjdk.jmh.samples.JMHSample_35_Profilers$Atomic::test@1 (line 280)
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@16 (line 199)
                  0.19%    0.02%    0x00007f1824f87c49: test   %r11d,%r11d
                                    0x00007f1824f87c4c: je     0x00007f1824f87cad
                                    0x00007f1824f87c4e: mov    $0x1,%edx
                                    0x00007f1824f87c53: lock xadd %rdx,0x10(%r12,%r11,8)
                                                                                  ;*invokevirtual getAndAddLong
                                                                                  ; - java.util.concurrent.atomic.AtomicLong::incrementAndGet@8 (line 200)
                                                                                  ; - org.openjdk.jmh.samples.JMHSample_35_Profilers$Atomic::test@4 (line 280)
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@16 (line 199)
                 95.20%   95.06%    0x00007f1824f87c5a: add    $0x1,%rdx          ;*ladd
                                                                                  ; - java.util.concurrent.atomic.AtomicLong::incrementAndGet@12 (line 200)
                                                                                  ; - org.openjdk.jmh.samples.JMHSample_35_Profilers$Atomic::test@4 (line 280)
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@16 (line 199)
                  0.24%    0.00%    0x00007f1824f87c5e: mov    0x10(%rsp),%rsi
                                    0x00007f1824f87c63: callq  0x00007f1824e2b020  ; OopMap{[0]=Oop [8]=Oop [16]=Oop [24]=Oop off=232}
                                                                                  ;*invokevirtual consume
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@19 (line 199)
                                                                                  ;   {optimized virtual_call}
                  0.20%    0.01%    0x00007f1824f87c68: mov    0x18(%rsp),%r10
                                    0x00007f1824f87c6d: movzbl 0x94(%r10),%r11d   ;*getfield isDone
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@29 (line 201)
                  0.00%             0x00007f1824f87c75: add    $0x1,%rbp          ; OopMap{r10=Oop [0]=Oop [8]=Oop [16]=Oop [24]=Oop off=249}
                                                                                  ;*ifeq
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@32 (line 201)
                  0.20%    0.01%    0x00007f1824f87c79: test   %eax,0x15f36381(%rip)        # 0x00007f183aebe000
                                                                                  ;   {poll}
                                    0x00007f1824f87c7f: test   %r11d,%r11d
                                    0x00007f1824f87c82: je     0x00007f1824f87c40  ;*aload_2
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@35 (line 202)
                                    0x00007f1824f87c84: mov    $0x7f1839be4220,%r10
                                    0x00007f1824f87c8e: callq  *%r10              ;*invokestatic nanoTime
                                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub@36 (line 202)
                                    0x00007f1824f87c91: mov    (%rsp),%r10
                ....................................................................................................
                 96.03%   95.10%  <total for region 1>

            perfasm 还会打印最热的方法，以显示是否执行了基准测试。
            大多数时候，它也可以分解虚拟机和内核变量（kernel symbols）。

                ....[Hottest Methods (after inlining)]..............................................................
                 96.03%   95.10%  org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_avgt_jmhStub
                  0.73%    0.78%  org.openjdk.jmh.samples.generated.JMHSample_35_Profilers_Atomic_test::test_AverageTime
                  0.63%    0.00%  org.openjdk.jmh.infra.Blackhole::consume
                  0.23%    0.25%  native_write_msr_safe ([kernel.kallsyms])
                  0.09%    0.05%  _raw_spin_unlock ([kernel.kallsyms])
                  0.09%    0.00%  [unknown] (libpthread-2.19.so)
                  0.06%    0.07%  _raw_spin_lock ([kernel.kallsyms])
                  0.06%    0.04%  _raw_spin_unlock_irqrestore ([kernel.kallsyms])
                  0.06%    0.05%  _IO_fwrite (libc-2.19.so)
                  0.05%    0.03%  __srcu_read_lock; __srcu_read_unlock ([kernel.kallsyms])
                  0.04%    0.05%  _raw_spin_lock_irqsave ([kernel.kallsyms])
                  0.04%    0.06%  vfprintf (libc-2.19.so)
                  0.04%    0.01%  mutex_unlock ([kernel.kallsyms])
                  0.04%    0.01%  _nv014306rm ([nvidia])
                  0.04%    0.04%  rcu_eqs_enter_common.isra.47 ([kernel.kallsyms])
                  0.04%    0.02%  mutex_lock ([kernel.kallsyms])
                  0.03%    0.07%  __acct_update_integrals ([kernel.kallsyms])
                  0.03%    0.02%  fget_light ([kernel.kallsyms])
                  0.03%    0.01%  fput ([kernel.kallsyms])
                  0.03%    0.04%  rcu_eqs_exit_common.isra.48 ([kernel.kallsyms])
                  1.63%    2.26%  <...other 319 warm methods...>
                ....................................................................................................
                100.00%   98.97%  <totals>
                ....[Distribution by Area]..........................................................................
                 97.44%   95.99%  <generated code>
                  1.60%    2.42%  <native code in ([kernel.kallsyms])>
                  0.47%    0.78%  <native code in (libjvm.so)>
                  0.22%    0.29%  <native code in (libc-2.19.so)>
                  0.15%    0.07%  <native code in (libpthread-2.19.so)>
                  0.07%    0.38%  <native code in ([nvidia])>
                  0.05%    0.06%  <native code in (libhsdis-amd64.so)>
                  0.00%    0.00%  <native code in (nf_conntrack.ko)>
                  0.00%    0.00%  <native code in (hid.ko)>
                ....................................................................................................
                100.00%  100.00%  <totals>

            因为每个 fork 的地址都有不同，因此多个 fork 对于 perfasm 没有意义。
        */
    }
}