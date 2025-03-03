package org.example;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        final int ARRAY_SIZE = 100000000;
        final int MAX_ABS_VALUE = 1000;

        int[] array = initializeArray(ARRAY_SIZE, MAX_ABS_VALUE);

        long startTime = System.nanoTime();

        Result result = processArray(array);

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

    private static Result processArray(int[] array) {
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

    private static class Result {
        int minDivisibleBy11;
        int sumOfDivisibleBy11;

        Result(int min, int sum) {
            this.minDivisibleBy11 = min;
            this.sumOfDivisibleBy11 = sum;
        }
    }
}
