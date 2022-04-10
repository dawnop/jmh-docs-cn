package com.dawnop.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JMHSample_02_BenchmarkModes {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JMHSample_02_BenchmarkModes.class.getSimpleName())
                .exclude(JMHSample_02_BenchmarkModes.class.getSimpleName() + ".measureAll")
                .exclude(JMHSample_02_BenchmarkModes.class.getSimpleName() + ".measureMultiple")
                .output("JMHSample_02_BenchmarkModes_result.sampleLog")
                .forks(1)
                .build();

        new Runner(options).run();
    }

    
}
