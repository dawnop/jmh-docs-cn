package com.dawnop.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 这是一些看起来很复杂但很有意思的例子，看看如何在复杂的业务场景下使用 JMH。
 * 到目前为止，我们还没有定制化展示运行结果，很没意思。
 * This example shows the rather convoluted, but fun way to exploit
 * JMH API in complex scenarios. Up to this point, we haven't consumed
 * the results programmatically, and hence we are missing all the fun.
 */
@State(Scope.Thread)
public class JMHSample_25_API_GA {


    private int v;

    @Benchmark
    public int test() {
        return veryImportantCode(1000, v);
    }

    /**
     * 看看这段 naive 的代码，由于用了尾递归所以有很明显的性能障碍，
     * 当前的 HotSpot 没有能力进行尾递归优化 (tail call optimization)。
     */

    public int veryImportantCode(int d, int v) {
        if (d == 0) {
            return v;
        } else {
            return veryImportantCode(d - 1, v);
        }
    }

    /**
     * 或许我们可以通过更好的方法内联策略来弥补不支持 TCO 的缺陷。
     * 但是手动操作优化方式需要知道大量虚拟机底层的知识。
     * 不如构建一个入门级别的遗传算法、并且通过方法内联找到更好的优化方法。
     * <p>
     * 如果你还不了解遗传算法的概念，可以尝试先看看 wiki：
     * http://en.wikipedia.org/wiki/Genetic_algorithm
     * <p>
     * 虚拟机大佬们能猜出哪个选项能获得最好的性能。
     * 跑一跑这个例子看看是否改进了性能。
     */

    public static void main(String[] args) throws RunnerException {
        // 这里有一些基础的选项，每次运行都会遗传这些选项。
        Options baseOpts = new OptionsBuilder()
                .include(JMHSample_25_API_GA.class.getName())
                .warmupTime(TimeValue.milliseconds(200))
                .measurementTime(TimeValue.milliseconds(200))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .verbosity(VerboseMode.SILENT)
                .build();

        // 初始化种群
        Population pop = new Population();
        final int POPULATION = 10;
        for (int c = 0; c < POPULATION; c++) {
            pop.addChromosome(new Chromosome(baseOpts));
        }

        // 进行一些小优化
        final int GENERATIONS = 100;
        for (int g = 0; g < GENERATIONS; g++) {
            System.out.println("Entering generation " + g);

            // 获取 baseline 分数
            // 重新测量获取可靠的当前估计
            RunResult runner = new Runner(baseOpts).runSingle();
            Result baseResult = runner.getPrimaryResult();

            // 漂亮的表格
            System.out.println("---------------------------------------");
            System.out.printf("Baseline score: %10.2f %s%n",
                    baseResult.getScore(),
                    baseResult.getScoreUnit()
            );

            for (Chromosome c : pop.getAll()) {
                System.out.printf("%10.2f %s (%+10.2f%%) %s%n",
                        c.getScore(),
                        baseResult.getScoreUnit(),
                        (c.getScore() / baseResult.getScore() - 1) * 100,
                        c.toString()
                );
            }
            System.out.println();

            Population newPop = new Population();

            // 找到不错的个体
            final int ELITE = 2;
            for (Chromosome c : pop.getAll().subList(0, ELITE)) {
                newPop.addChromosome(c);
            }

            // 杂交剩余的新种群
            while (newPop.size() < pop.size()) {
                Chromosome p1 = pop.selectToBreed();
                Chromosome p2 = pop.selectToBreed();

                newPop.addChromosome(p1.crossover(p2).mutate());
                newPop.addChromosome(p2.crossover(p1).mutate());
            }

            pop = newPop;
        }

    }

    /**
     * 种群
     */
    public static class Population {
        private final List<Chromosome> list = new ArrayList<>();

        public void addChromosome(Chromosome c) {
            list.add(c);
            Collections.sort(list);
        }

        /**
         * 选择繁殖个体
         * 分高的个题更有可能被选中
         */
        public Chromosome selectToBreed() {
            double totalScore = 0D;
            for (Chromosome c : list) {
                totalScore += c.score();
            }

            double thresh = Math.random() * totalScore;
            for (Chromosome c : list) {
                if (thresh < 0) return c;
                thresh = -c.score();
            }

            throw new IllegalStateException("Can not choose");
        }

        public int size() {
            return list.size();
        }

        public List<Chromosome> getAll() {
            return list;
        }
    }

    /**
     * 染色体：对个题信息进行编码
     */
    public static class Chromosome implements Comparable<Chromosome> {

        // 当前分数不能被计算
        double score = Double.NEGATIVE_INFINITY;

        // 基本选项
        final Options baseOpts;

        // 当前 HotSpot 默认选项
        int freqInlineSize = 325;
        int inlineSmallCode = 1000;
        int maxInlineLevel = 9;
        int maxInlineSize = 35;
        int maxRecursiveInlineLevel = 1;
        int minInliningThreshold = 250;

        public Chromosome(Options baseOpts) {
            this.baseOpts = baseOpts;
        }

        public double score() {
            if (score != Double.NEGATIVE_INFINITY) {
                // 已经获取了足够的分数
                return score;
            }

            try {
                // 根据当前个体的编码添加选项
                //  a) 添加基本选项
                //  b) 添加 JVM 参数，使用字符串解析写起来短一点
                Options theseOpts = new OptionsBuilder()
                        .parent(baseOpts)
                        .jvmArgs(toString().split("[ ]"))
                        .build();

                // 运行基准测试获取结果
                RunResult runResult = new Runner(theseOpts).runSingle();
                score = runResult.getPrimaryResult().getScore();
            } catch (RunnerException e) {
                // 个体参数太差 JVM 都看不下去了
                score = Double.MIN_VALUE;
            }

            return score;
        }

        @Override
        public int compareTo(Chromosome o) {
            // Order by score, descending.
            return -Double.compare(score(), o.score());
        }

        @Override
        public String toString() {
            return "-XX:FreqInlineSize=" + freqInlineSize +
                    " -XX:InlineSmallCode=" + inlineSmallCode +
                    " -XX:MaxInlineLevel=" + maxInlineLevel +
                    " -XX:MaxInlineSize=" + maxInlineSize +
                    " -XX:MaxRecursiveInlineLevel=" + maxRecursiveInlineLevel +
                    " -XX:MinInliningThreshold=" + minInliningThreshold;
        }

        public Chromosome crossover(Chromosome other) {
            // 进行杂交
            // 虽然这是一种非常菜的杂交方法，但是还是有点用。

            final double CROSSOVER_PROB = 0.1;

            Chromosome result = new Chromosome(baseOpts);

            result.freqInlineSize = (Math.random() < CROSSOVER_PROB) ?
                    this.freqInlineSize : other.freqInlineSize;

            result.inlineSmallCode = (Math.random() < CROSSOVER_PROB) ?
                    this.inlineSmallCode : other.inlineSmallCode;

            result.maxInlineLevel = (Math.random() < CROSSOVER_PROB) ?
                    this.maxInlineLevel : other.maxInlineLevel;

            result.maxInlineSize = (Math.random() < CROSSOVER_PROB) ?
                    this.maxInlineSize : other.maxInlineSize;

            result.maxRecursiveInlineLevel = (Math.random() < CROSSOVER_PROB) ?
                    this.maxRecursiveInlineLevel : other.maxRecursiveInlineLevel;

            result.minInliningThreshold = (Math.random() < CROSSOVER_PROB) ?
                    this.minInliningThreshold : other.minInliningThreshold;

            return result;
        }

        public Chromosome mutate() {
            // 进行突变
            // 虽然这是一种非常菜的突变方法，但是还是有点用。

            Chromosome result = new Chromosome(baseOpts);

            result.freqInlineSize = (int) randomChange(freqInlineSize);
            result.inlineSmallCode = (int) randomChange(inlineSmallCode);
            result.maxInlineLevel = (int) randomChange(maxInlineLevel);
            result.maxInlineSize = (int) randomChange(maxInlineSize);
            result.maxRecursiveInlineLevel = (int) randomChange(maxRecursiveInlineLevel);
            result.minInliningThreshold = (int) randomChange(minInliningThreshold);

            return result;
        }

        private double randomChange(double v) {
            final double MUTATE_PROB = 0.5;
            if (Math.random() < MUTATE_PROB) {
                if (Math.random() < 0.5) {
                    return v / (Math.random() * 2);
                } else {
                    return v * (Math.random() * 2);
                }
            } else {
                return v;
            }
        }

        public double getScore() {
            return score;
        }
    }

}
