package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


/**
 * cache line 共享机制是一件让人伤脑筋的事情。
 * 如果两个线程访问（或者修改）相邻的内存地址，很有可能它们修改了同一个 cache line 上的数据。
 * 这可能导致明显的性能下降。
 * <p>
 * JMH 可以帮助你减轻这种问题：@States 对象周围被自动填充了无意义的数据，
 * 多线程访问的时候不会访问到同一个 cache line ，提升了效率。
 * <p>
 * <ul>
 * 假设有两个线程，
 * <li> a) 读自己字段的无辜 reader
 * <li> b) 写自己字段的暴躁 writer
 */


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
public class JMHSample_22_FalseSharing {


    /**
     * Baseline:
     * 因为共享机制，reader 和 writer 回有很大的额外开销。
     */

    @State(Scope.Group)
    public static class StateBaseline {
        int readOnly;
        int writeOnly;
    }

    @Benchmark
    @Group("baseline")
    public int reader(StateBaseline s) {
        return s.readOnly;
    }

    @Benchmark
    @Group("baseline")
    public void writer(StateBaseline s) {
        s.writeOnly++;
    }

    /**
     * 测试 1: 加填充
     * <p>
     * 我们试图通过加字段之间增加填充来缓解 cache line 共享的问题。
     * 这种方法并不通用，因为 JVM 有可能会更改字段的排列顺序，
     * 即使它们都是相同的类型。
     */

    @State(Scope.Group)
    public static class StatePadded {
        int readOnly;
        int p01, p02, p03, p04, p05, p06, p07, p08;
        int p11, p12, p13, p14, p15, p16, p17, p18;
        int writeOnly;
        int q01, q02, q03, q04, q05, q06, q07, q08;
        int q11, q12, q13, q14, q15, q16, q17, q18;
    }

    @Benchmark
    @Group("padded")
    public int reader(StatePadded s) {
        return s.readOnly;
    }

    @Benchmark
    @Group("padded")
    public void writer(StatePadded s) {
        s.writeOnly++;
    }

    /**
     * 测试 2: 继承
     * <p>
     * 我们可以尝试使用类的层级关系来缓解 cache line 共享的问题。
     * 因为父类字段总是先进行内存分配。
     * 通过这种构造，字段可以被填充包裹住。
     * <p>
     * 记得要用最小的数据类型，这样内存分配时就不会产生任何间隙，
     * 这些间隙有可能被后来的子类字段所占用。
     * 根据要保护的类的实际布局，我们可能需要很多的填充，
     * 以防要保护的类的字段被分配到父类字段中。
     */

    public static class StateHierarchy_1 {
        int readOnly;
    }

    public static class StateHierarchy_2 extends StateHierarchy_1 {
        byte p01, p02, p03, p04, p05, p06, p07, p08;
        byte p11, p12, p13, p14, p15, p16, p17, p18;
        byte p21, p22, p23, p24, p25, p26, p27, p28;
        byte p31, p32, p33, p34, p35, p36, p37, p38;
        byte p41, p42, p43, p44, p45, p46, p47, p48;
        byte p51, p52, p53, p54, p55, p56, p57, p58;
        byte p61, p62, p63, p64, p65, p66, p67, p68;
        byte p71, p72, p73, p74, p75, p76, p77, p78;
    }

    public static class StateHierarchy_3 extends StateHierarchy_2 {
        int writeOnly;
    }

    public static class StateHierarchy_4 extends StateHierarchy_3 {
        byte q01, q02, q03, q04, q05, q06, q07, q08;
        byte q11, q12, q13, q14, q15, q16, q17, q18;
        byte q21, q22, q23, q24, q25, q26, q27, q28;
        byte q31, q32, q33, q34, q35, q36, q37, q38;
        byte q41, q42, q43, q44, q45, q46, q47, q48;
        byte q51, q52, q53, q54, q55, q56, q57, q58;
        byte q61, q62, q63, q64, q65, q66, q67, q68;
        byte q71, q72, q73, q74, q75, q76, q77, q78;
    }

    @State(Scope.Group)
    public static class StateHierarchy extends StateHierarchy_4 {
    }

    @Benchmark
    @Group("hierarchy")
    public int reader(StateHierarchy s) {
        return s.readOnly;
    }

    @Benchmark
    @Group("hierarchy")
    public void writer(StateHierarchy s) {
        s.writeOnly++;
    }

    /**
     * 测试 3: 数组
     * <p>
     * 这种方法的原理为数组的内存分配方式是连续的。
     * 与其将字段放在类中，不如把字段放在数组里，
     * 并且字段之间有非常大的间隔。
     */

    @State(Scope.Group)
    public static class StateArray {
        int[] arr = new int[128];
    }

    @Benchmark
    @Group("sparse")
    public int reader(StateArray s) {
        return s.arr[0];
    }

    @Benchmark
    @Group("sparse")
    public void writer(StateArray s) {
        s.arr[64]++;
    }

    /**
     * 测试 4: @Contended
     * 这个注解告诉 JVM 需要填充这个变量的周围以防 cache line 共享。
     * <p>
     * JDK 1.8 可以在字段上加上 @sun.misc.Contended。
     * 记得添加 JVM 参数 -XX:-RestrictContended 来开启。
     * <p>
     * JDK 1.9 引入了 JPMS (Java Platform Module System)，
     * 包结构有了很大的变化。此时 @Contended 注解位于：<br>
     * import jdk.internal.vm.annotation.Contended; <br>
     * 并且需要添加 JVM 参数，将 unnamed module 暴露出来。<br>
     * --add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED <br>
     */

    @State(Scope.Group)
    public static class StateContended {
        int readOnly;

//        @sun.misc.Contended
        int writeOnly;
    }

    @Benchmark
    @Group("contended")
    public int reader(StateContended s) {
        return s.readOnly;
    }

    @Benchmark
    @Group("contended")
    public void writer(StateContended s) {
        s.writeOnly++;
    }

    /**
     * 注意观察性能对比。
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_22_FalseSharing.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors())
                .build();

        new Runner(opt).run();
    }

}