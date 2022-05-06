package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 这个例子用来展示内存访问方式对性能的影响。
 * 详见<a href="https://zh.wikipedia.org/wiki/%E8%AE%BF%E9%97%AE%E5%B1%80%E9%83%A8%E6%80%A7">局部性原理</a>。
 * <p>
 * 很多情况下的性能差异都是来自于内存访问方式的差异。
 * 在下面的例子中，我们分别按行按列访问二维矩阵。
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
@State(Scope.Benchmark)
public class JMHSample_37_CacheAccess {


    private final static int COUNT = 4096;
    private final static int MATRIX_SIZE = COUNT * COUNT;

    private int[][] matrix;

    @Setup
    public void setup() {
        matrix = new int[COUNT][COUNT];
        Random random = new Random(1234);
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                matrix[i][j] = random.nextInt();
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(MATRIX_SIZE)
    public void colFirst(Blackhole bh) {
        for (int c = 0; c < COUNT; c++) {
            for (int r = 0; r < COUNT; r++) {
                bh.consume(matrix[r][c]);
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(MATRIX_SIZE)
    public void rowFirst(Blackhole bh) {
        for (int r = 0; r < COUNT; r++) {
            for (int c = 0; c < COUNT; c++) {
                bh.consume(matrix[r][c]);
            }
        }
    }

    /*
        显然，按列访问会更慢。因为 Java 中多维数组会从低维到高维依次分配。
        例如 3×4×5 的矩阵，会先分配 5 个单位的空间，再将这个空间复制 3 次，得到 4×5 的空间，
        然后再将 4×5 的空间复制 2 次，得到 3×4×5 的空间。
        并且在这个过程中内存的分配是连续的，在内存中可以看作一维数组。
        当矩阵非常大，按非连续内存访问会有缓存失效的问题。
        -prof perfnorm 参数可以很方便地展示缓存失效。

        Benchmark                                                 Mode  Cnt   Score    Error  Units
        JMHSample_37_MatrixCopy.colFirst                          avgt   25   5.306 ±  0.020  ns/op
        JMHSample_37_MatrixCopy.colFirst:·CPI                     avgt    5   0.621 ±  0.011   #/op
        JMHSample_37_MatrixCopy.colFirst:·L1-dcache-load-misses   avgt    5   2.177 ±  0.044   #/op <-- OOPS
        JMHSample_37_MatrixCopy.colFirst:·L1-dcache-loads         avgt    5  14.804 ±  0.261   #/op
        JMHSample_37_MatrixCopy.colFirst:·LLC-loads               avgt    5   2.165 ±  0.091   #/op
        JMHSample_37_MatrixCopy.colFirst:·cycles                  avgt    5  22.272 ±  0.372   #/op
        JMHSample_37_MatrixCopy.colFirst:·instructions            avgt    5  35.888 ±  1.215   #/op

        JMHSample_37_MatrixCopy.rowFirst                          avgt   25   2.662 ±  0.003  ns/op
        JMHSample_37_MatrixCopy.rowFirst:·CPI                     avgt    5   0.312 ±  0.003   #/op
        JMHSample_37_MatrixCopy.rowFirst:·L1-dcache-load-misses   avgt    5   0.066 ±  0.001   #/op
        JMHSample_37_MatrixCopy.rowFirst:·L1-dcache-loads         avgt    5  14.570 ±  0.400   #/op
        JMHSample_37_MatrixCopy.rowFirst:·LLC-loads               avgt    5   0.002 ±  0.001   #/op
        JMHSample_37_MatrixCopy.rowFirst:·cycles                  avgt    5  11.046 ±  0.343   #/op
        JMHSample_37_MatrixCopy.rowFirst:·instructions            avgt    5  35.416 ±  1.248   #/op

        因此在比较两个基准测试时，如果性能差异是由内存局部性引起的，应当引起警惕。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + JMHSample_37_CacheAccess.class.getSimpleName() + ".*")
                .build();

        new Runner(opt).run();
    }

}