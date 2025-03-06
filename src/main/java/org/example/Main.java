package org.example;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) {
        final int ARRAY_SIZE = 500000000;
        final int MAX_ABS_VALUE = 1000;
        final int THREAD_COUNT = 6;

        int[] array = initializeArray(ARRAY_SIZE, MAX_ABS_VALUE);

        long startTime = System.nanoTime();

        //Seq
         Result result = processArraySequential(array);

        //MultiThread lock
//        Result result = processArrayMultithreaded(array, THREAD_COUNT);

        //MultiThread atomic
//        Result result = processArrayAtomic(array, THREAD_COUNT);

        long endTime = System.nanoTime();
        long executionTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.printf("Execution completed in %d ms.\n", executionTime);
        System.out.printf("Sum of elements divisible by 11: %d\n", result.sumOfDivisibleBy11);
        System.out.printf("Minimum element divisible by 11: %d\n", result.minDivisibleBy11);
    }

    private static int[] initializeArray(int size, int limit) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(2 * limit + 1) - limit;
        }
        return array;
    }

    //Seq
    private static Result processArraySequential(int[] array) {
        int minDivisibleBy11 = Integer.MAX_VALUE;
        int sumOfDivisibleBy11 = 0;

        for (int value : array) {
            if (value % 11 == 0) {
                sumOfDivisibleBy11 += value;
                if (value < minDivisibleBy11) {
                    minDivisibleBy11 = value;
                }
            }
        }
        return new Result(minDivisibleBy11, sumOfDivisibleBy11);
    }

    //MultiThread lock
    private static Result processArrayMultithreaded(int[] array, int threadCount) {
        final Lock lock = new ReentrantLock();
        final int[] minDivisibleBy11 = {Integer.MAX_VALUE};
        final int[] sumOfDivisibleBy11 = {0};

        Thread[] threads = new Thread[threadCount];
        int segmentSize = array.length / threadCount;

        for (int i = 0; i < threadCount; i++) {
            final int start = i * segmentSize;
            final int end = (i == threadCount - 1) ? array.length : start + segmentSize;

            threads[i] = new Thread(() -> {
                int localMin = Integer.MAX_VALUE;
                int localSum = 0;

                for (int j = start; j < end; j++) {
                    if (array[j] % 11 == 0) {
                        localSum += array[j];
                        if (array[j] < localMin) {
                            localMin = array[j];
                        }
                    }
                }

                lock.lock();
                try {
                    sumOfDivisibleBy11[0] += localSum;
                    if (localMin < minDivisibleBy11[0]) {
                        minDivisibleBy11[0] = localMin;
                    }
                } finally {
                    lock.unlock();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new Result(minDivisibleBy11[0], sumOfDivisibleBy11[0]);
    }

    // Multithread atomic (CAS with do-while loop)
    private static Result processArrayAtomic(int[] array, int threadCount) {
        final AtomicInteger minDivisibleBy11 = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicLong sumOfDivisibleBy11 = new AtomicLong(0);

        Thread[] threads = new Thread[threadCount];
        int segmentSize = array.length / threadCount;

        for (int i = 0; i < threadCount; i++) {
            final int start = i * segmentSize;
            final int end = (i == threadCount - 1) ? array.length : start + segmentSize;

            threads[i] = new Thread(() -> {
                int localMin = Integer.MAX_VALUE;
                long localSum = 0;

                for (int j = start; j < end; j++) {
                    if (array[j] % 11 == 0) {
                        localSum += array[j];
                        if (array[j] < localMin) {
                            localMin = array[j];
                        }
                    }
                }

                // sum (CAS)
                long prevSum, newSum;
                do {
                    prevSum = sumOfDivisibleBy11.get();
                    newSum = prevSum + localSum;
                } while (!sumOfDivisibleBy11.compareAndSet(prevSum, newSum));

                // min (CAS)
                int prevMin, newMin;
                do {
                    prevMin = minDivisibleBy11.get();
                    newMin = Math.min(prevMin, localMin);
                } while (!minDivisibleBy11.compareAndSet(prevMin, newMin));

            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new Result(minDivisibleBy11.get(), (int) sumOfDivisibleBy11.get());
    }


    private static class Result {
        int minDivisibleBy11;
        int sumOfDivisibleBy11;

        Result(int min, int sum) {
            this.minDivisibleBy11 = min;
            this.sumOfDivisibleBy11 = sum;
        }
    }
}
