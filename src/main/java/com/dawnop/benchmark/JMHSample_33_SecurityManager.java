package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.Policy;
import java.security.URIParameter;
import java.util.concurrent.TimeUnit;

/**
 * 有些测试可能需要安装 SecurityManager。
 * 因为 JMH 本身需要一些特权，因此盲目地安装 SecurityManager 是不行的，
 * 有可能会导致 JMH 运行失败。
 * <p>
 * 在本例子中，我们打算测一测 System.getProperty 在 SecurityManager
 * 安装和不安装两种情况下的的性能差距。
 * 为了完成这个测试，我们准备了两个带有 helper 方法的 @State 类，
 * 一个获取默认的 JMH 安全策略，并且安装 SecurityManager；另一个不安装 SecurityManager。
 * <p>
 * 如果你需要更加严格的安全策略，最少应该允许 /jmh-security-minimal.policy 中的权限，
 * 它是能够使 JMH 运行的最小权限集合。如果还需要额外的权限，
 * 需要把额外的权限集合和 JMH 所需的权限集合取交集。
 */


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JMHSample_33_SecurityManager {


    @State(Scope.Benchmark)
    public static class SecurityManagerInstalled {
        @Setup
        public void setup() throws IOException, NoSuchAlgorithmException, URISyntaxException {
            URI policyFile = JMHSample_33_SecurityManager.class.getResource("/jmh-security.policy").toURI();
            Policy.setPolicy(Policy.getInstance("JavaPolicy", new URIParameter(policyFile)));
            System.setSecurityManager(new SecurityManager());
        }

        @TearDown
        public void tearDown() {
            System.setSecurityManager(null);
        }
    }

    @State(Scope.Benchmark)
    public static class SecurityManagerEmpty {
        @Setup
        public void setup() throws IOException, NoSuchAlgorithmException, URISyntaxException {
            System.setSecurityManager(null);
        }
    }

    @Benchmark
    public String testWithSM(SecurityManagerInstalled s) throws InterruptedException {
        return System.getProperty("java.home");
    }

    @Benchmark
    public String testWithoutSM(SecurityManagerEmpty s) throws InterruptedException {
        return System.getProperty("java.home");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_33_SecurityManager.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}