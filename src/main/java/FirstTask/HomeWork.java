package FirstTask;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;

public class HomeWork {
    public static void main(String[] args) {

        // task first, to fill tow-dimensional array with random ints
        // I would prefer to use ThreadLocalRandom or new class RandomGenerator in real prod code,
        // but working that solution out was also pretty fun =).

        Integer[][] randomIntArray = new Integer[10][10];
        Arrays.setAll(randomIntArray, x -> {
            Arrays.setAll(randomIntArray[x], y -> y = DummyRandom.randomInt());
            return randomIntArray[x];
        });

        Arrays.stream(randomIntArray)
                .flatMap(Arrays::stream)
                //.parallel()  be having our combiner we could easily use parallel in case immense amount of data, but now there is no need
                .collect(minMaxAverage(Integer::compare, x -> x,
                        (min, max, avg) -> "Min: " + min + "," + "\n" + "Max: " + max + "," + "\n" + "Average: " + avg + "."))
                .ifPresent(System.out::println);

    }
    static class DummyRandom {
        private static final byte _0 = 0;
        private static final byte _1 = 1;
        private static final byte _2 = 2;
        private static final int DEEP_LONG = 0x40;
        private static final int DEEP_INT = 0x20;
        private static final int SHIFT_LONG = 0x3F;
        private static final int SHIFT_INT = 0x1F;

        public static long randomLong() {
            return illusionOfRandomLong();
        }

        public static int randomInt() {
            return illusionOfRandomInt();
        }

        //These two methods just random cut 'surplus' bits create kinda 0000000101010101 from 110101010101100010
        // or 0000000000000110 from 0100101001111000011
        private static long illusionOfRandomLong() {
            long chop = getRandom(_0, DEEP_LONG);
            chop = chop >> (DEEP_LONG - ((System.nanoTime() >> 2) & SHIFT_LONG));
            return chop;
        }

        private static int illusionOfRandomInt() {
            int chop = (int) getRandom(_0, DEEP_INT);
            chop = chop >> (DEEP_INT - ((System.nanoTime() >> 2) & SHIFT_INT));
            return chop;
        }

        // this method depends on even and odd create something similar to 101011010101010101 via recursion.
        // deep relies on either this is integer or long that method uses 32 or 64 deep level respectively
        private static long getRandom(long bits, int deep) {
            if (deep == 0) return bits;
            if (isEven()) bits = (bits << _1) | _1;
            else bits = (bits << _1) | _0;
            return getRandom(bits, deep - 1);
        }

        private static boolean isEven() {
            //System.nanoTime() always gives us time that end with last two zero,
            // for example 189425938811900 / 189425938812600 / 189425938813400 / 189425938814200
            // I have just removed them here by >> 2
            return (System.nanoTime() >> _2) % 2 == 0;
        }
    }

    // Note that C# has 16 overloaded "delegates"(their implementation of functional style)
    // up to 16 params right out-of-the-box! yahoo!!
    // and I have to write my own already upon 3rd...
    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);

        default <V> TriFunction<A, B, C, V> andThen(
                Function<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (A a, B b, C c) -> after.apply(apply(a, b, c));
        }
    }

    // Custom collector for purpose compute Min Max and Average in one run
    static <T, R> Collector<? super T, ?, Optional<R>> minMaxAverage(
            Comparator<? super T> comparator,
            ToIntFunction<? super T> mapper,
            TriFunction<? super T, ? super T, Double, R> finisher) {
        class Report {
            private T min;
            private T max;
            private final long[] avg = new long[2];
            private boolean present;

            private double average() {
                return (double) avg[1] / avg[0];
            }

            void add(T t) {
                if (present) {
                    if (comparator.compare(t, min) < 0) min = t;
                    if (comparator.compare(t, max) > 0) max = t;
                } else {
                    min = max = t;
                    present = true;
                }
                avg[0]++;
                avg[1] += mapper.applyAsInt(t);
            }

            Report combine(Report another) {
                if (!another.present) return this;
                if (!present) return another;
                if (comparator.compare(another.min, min) < 0) min = another.min;
                if (comparator.compare(another.max, max) < 0) max = another.max;
                this.avg[1] += another.avg[1];
                this.avg[0] += another.avg[0];
                return this;
            }
        }
        return Collector.of(Report::new, Report::add, Report::combine,
                report -> report.present
                        ? Optional.of(finisher.apply(report.min, report.max, report.average()))
                        : Optional.empty());
    }
}
