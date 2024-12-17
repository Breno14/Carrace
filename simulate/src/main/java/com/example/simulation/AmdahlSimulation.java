package com.example.simulation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AmdahlSimulation {
    private final double sequentialFraction; // Fração sequencial do programa (S)
    private final int totalTasks; // Número total de tarefas
    private final int[] processors; // Configurações de processadores

    public AmdahlSimulation(double sequentialFraction, int totalTasks, int[] processors) {
        this.sequentialFraction = sequentialFraction;
        this.totalTasks = totalTasks;
        this.processors = processors;
    }

    // Simula o tempo de execução para diferentes números de processadores
    public void simulate() {
        System.out.println("Simulacao da Lei de Amdahl:");
        for (int cores : processors) {
            double speedup = calculateSpeedup(cores);
            System.out.printf("Processadores: %d, Aceleracao Teorica: %.2fx%n", cores, speedup);

            long executionTime = simulateExecution(cores);
            System.out.printf("Tempo de Execucao com %d Nucleos: %d ms%n%n", cores, executionTime);
        }
    }

    // Calcula a aceleração teórica usando a Lei de Amdahl
    private double calculateSpeedup(int numProcessors) {
        return 1 / (sequentialFraction + (1 - sequentialFraction) / numProcessors);
    }

    // Simula a execução paralela
    private long simulateExecution(int numProcessors) {
        ExecutorService executor = Executors.newFixedThreadPool(numProcessors);
        long startTime = System.currentTimeMillis();

        // Simula tarefas paralelas
        for (int i = 0; i < totalTasks; i++) {
            executor.submit(() -> {
                try {
                    if (Math.random() < sequentialFraction) {
                        // Simula tarefa sequencial
                        TimeUnit.MILLISECONDS.sleep(5);
                    } else {
                        // Simula tarefa paralelizável
                        TimeUnit.MILLISECONDS.sleep(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return System.currentTimeMillis() - startTime;
    }

    public static void main(String[] args) {
        double sequentialFraction = 0.3; // 30% do programa é sequencial
        int totalTasks = 10000; // Número total de tarefas
        int[] processors = {1, 2, 4, 8, 16}; // Configurações de núcleos

        AmdahlSimulation simulation = new AmdahlSimulation(sequentialFraction, totalTasks, processors);
        simulation.simulate();
    }
}
