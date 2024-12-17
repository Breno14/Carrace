package com.example.simulation;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class RealTimeEquations {

    static class Task {
        String name;
        int computationTime; // Ci
        int period; // Pi
        int deadline; // Di
        int jitter; // Ji
        List<Task> dependencies;

        public Task(String name, int computationTime, int period, int deadline, int jitter) {
            this.name = name;
            this.computationTime = computationTime;
            this.period = period;
            this.deadline = deadline;
            this.jitter = jitter;
            this.dependencies = new ArrayList<>();
        }

        public void addDependency(Task dependency) {
            dependencies.add(dependency);
        }
    }

    public static double calculateProcessorUtilization(List<Task> tasks) {
        return tasks.stream()
                .mapToDouble(task -> (double) task.computationTime / task.period)
                .sum();
    }

    public static boolean isSchedulable(List<Task> tasks) {
        double utilization = calculateProcessorUtilization(tasks);
        return utilization <= 1.0;
    }

    public static int calculateResponseTime(Task task, List<Task> higherPriorityTasks) {
        int Wi = task.computationTime;
        boolean converged = false;

        while (!converged) {
            int interference = 0;
            for (Task hpTask : higherPriorityTasks) {
                interference += Math.ceil((double) (Wi + hpTask.jitter) / hpTask.period) * hpTask.computationTime;
            }
            int newWi = task.computationTime + interference;

            if (newWi == Wi) {
                converged = true;
            } else {
                Wi = newWi;
            }
        }

        return Wi + task.jitter;
    }

    public static void exportDependencyGraph(List<Task> tasks, String fileName) {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("digraph Dependencies {");
            for (Task task : tasks) {
                for (Task dependency : task.dependencies) {
                    writer.println("\"" + dependency.name + "\" -> \"" + task.name + "\";");
                }
            }
            writer.println("}");
            System.out.println("Grafo de dependencias exportado para: " + fileName);
        } catch (Exception e) {
            System.out.println("Erro ao exportar o grafico de dependencias: " + e.getMessage());
        }
    }

    public static void exportTaskMetrics(List<Task> tasks, String fileName) {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("Task,Utilizacao do Processador (%)");
            for (Task task : tasks) {
                double utilization = (double) task.computationTime / task.period * 100;
                writer.printf("%s,%.2f%n", task.name, utilization);
            }
            System.out.println("Dados de utilizacao exportados para: " + fileName);
        } catch (Exception e) {
            System.out.println("Erro ao exportar os dados: " + e.getMessage());
        }
    }

    public static void simulateProcessorConfigurations(List<Task> tasks, int[] processorConfigurations) {
        System.out.println("\nSimulacao de configuracoes de processadores:");
        for (int processors : processorConfigurations) {
            double utilization = calculateProcessorUtilization(tasks) / processors;
            System.out.printf("Processadores: %d - Utilizacao: %.2f%% - Sistema escalonavel: %s%n",
                    processors, utilization * 100, (utilization <= 1.0 ? "Sim" : "Nao"));
        }
    }

    public static void main(String[] args) {
        List<Task> tasks = new ArrayList<>();

        // Configuração das tarefas
        Task t1 = new Task("Tarefa 1", 10, 40, 20, 1);
        Task t2 = new Task("Tarefa 2", 20, 30, 85, 2);
        Task t3 = new Task("Tarefa 3", 30, 80, 110, 3);
        t2.addDependency(t1);
        t3.addDependency(t2);

        tasks.add(t1);
        tasks.add(t2);
        tasks.add(t3);

        // Cálculo da utilização do processador
        double utilization = calculateProcessorUtilization(tasks);
        System.out.printf("Utilizacao do Processador: %.2f%%%n", utilization * 100);
        System.out.println("Sistema escalonavel: " + (isSchedulable(tasks) ? "Sim" : "Nao"));

        // Cálculo do tempo de resposta
        for (int i = 0; i < tasks.size(); i++) {
            Task currentTask = tasks.get(i);
            List<Task> higherPriorityTasks = tasks.subList(0, i);
            int responseTime = calculateResponseTime(currentTask, higherPriorityTasks);
            System.out.printf("%s - Tempo de Resposta Maximo: %d / Deadline: %d%n",
                    currentTask.name, responseTime, currentTask.deadline);
        }

        // Exportação do grafo de dependências
        exportDependencyGraph(tasks, "dependency_graph.dot");

        // Exportação dos dados das tarefas
        exportTaskMetrics(tasks, "task_metrics.csv");

        // Simulação de configurações de processadores
        simulateProcessorConfigurations(tasks, new int[]{1, 2, 4, 8});
    }
}
