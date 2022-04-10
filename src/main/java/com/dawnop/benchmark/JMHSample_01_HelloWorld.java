package com.dawnop.benchmark;

/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JMHSample_01_HelloWorld {

    /**
     * <p>
     * 仅需在方法上标注 @Benchmark ，JMH 就会自动生成相应的代码。
     * 被 @Benchmark 标注的方法就是要测试的内容。
     * 无需关心它底层是怎样运行的。
     * </p><p>
     * 阅读 @Benchmark 源码获取完整的语义和限制。
     * 方法的名字不重要，只要加上 @Benchmark 注解就行。
     * 一个类中可以有多个标注为 @Benchmark 的方法。
     * </p><p>
     * 如果 Benchmark 方法死循环，JMH 也会一直运行。
     * 如果方法抛出了异常，JMH 会中断当前测试，继续进行下一个。
     * </p><p>
     * 空的方法可以看出 JMH 本身的开销。没有不产生开销的神奇框架。
     * 重要的是知道基准测试本身会产生开销。
     * baseline 测试中会进一步展开。
     * </p>
     */
    @Benchmark
    public void wellHelloThere() {
        // this method was intentionally left blank.
    }

    /**
     * <p>
     * 运行后会重复执行多次，每秒的执行的次数可以估计方法的开销。
     * </p><p>
     * 通过命令行运行：
     * <p>
     *    $ mvn clean install
     * </p><p>
     *    $ java -jar target/benchmarks.jar JMHSample_01_HelloWorld
     * </p>
     * </p>
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JMHSample_01_HelloWorld.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
