package com.example.carrace;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class RealTimeRaceManagerWithEquations {
    private static final Logger LOGGER = Logger.getLogger(RealTimeRaceManagerWithEquations.class.getName());
    private static final int THREAD_POOL_SIZE = 20;
    private static final int DEADLINE_MS = 10000;
    private static final int TOTAL_CARS = 19;

    private final List<Car> cars = new ArrayList<>();
    private final SafetyCar safetyCar;
    private final ExecutorService executor;
    private final PriorityBlockingQueue<Car> schedulingQueue = new PriorityBlockingQueue<>();
    private final List<String> raceData = new ArrayList<>();
    private final Map<String, Double> processorUsage = new HashMap<>();

    public RealTimeRaceManagerWithEquations() {
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.safetyCar = createSafetyCar();
    }

    private SafetyCar createSafetyCar() {
        return new SafetyCar("SafetyCar", 0, 0, 50, null, null);
    }

    public void initializeCars() {
        cars.add(safetyCar);
        schedulingQueue.add(safetyCar);

        for (int i = 0; i < TOTAL_CARS; i++) {
            Car car = new Car("Carro " + (i + 1), 0, 0, 20 + (i % 10), null, null);
            cars.add(car);
            schedulingQueue.add(car);
        }
    }

    public void startRace() {
        long raceStartTime = System.currentTimeMillis();
        LOGGER.info("Corrida iniciada...");

        for (Car car : cars) {
            executor.submit(() -> {
                while (car.isRunning() && System.currentTimeMillis() - raceStartTime < DEADLINE_MS) {
                    car.move();
                    evaluateCarPerformance(car, raceStartTime);
                }
                car.stop();
            });
        }

        stopExecutor();
        generateResults();
    }

    private void evaluateCarPerformance(Car car, long raceStartTime) {
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        double expectedDistance = (elapsedTime / (double) DEADLINE_MS) * car.getDistance();

        if (car.getDistance() < expectedDistance) {
            car.adjustPriority(Thread.MAX_PRIORITY);
            car.adjustSpeed(10);
            LOGGER.info(car.getName() + " esta atrasado e teve prioridade ajustada.");
        } else {
            car.adjustPriority(Thread.NORM_PRIORITY);
        }

        logProcessorUsage(car, elapsedTime);
        logCarState(car, elapsedTime);
    }

    private void logProcessorUsage(Car car, long elapsedTime) {
        if (elapsedTime > 0) {
            double utilization = (car.getProcessingTime() / (double) elapsedTime) * 100;
            processorUsage.put(car.getName(), utilization);
        }
    }

    private void logCarState(Car car, long elapsedTime) {
        raceData.add(car.getName() + "," + elapsedTime + "," + car.getDistance());
    }

    private void stopExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(DEADLINE_MS + 1000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                LOGGER.warning("Forcando parada das threads.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.severe("Execucao interrompida: " + e.getMessage());
        }
        LOGGER.info("Corrida finalizada!");
    }

    private void generateResults() {
        LOGGER.info("Resultado da corrida:");
        for (Car car : cars) {
            LOGGER.info(car.getName() + " - Distancia percorrida: " + car.getDistance());
        }
    }

    public void exportProcessorUsage(String fileName) {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("Carro,Utilizacao do processador (%)");
            for (Map.Entry<String, Double> entry : processorUsage.entrySet()) {
                writer.println(entry.getKey() + "," + String.format(Locale.US, "%.2f", entry.getValue()));
            }
            LOGGER.info("Dados de utilizacao do processador exportados para: " + fileName);
        } catch (Exception e) {
            LOGGER.severe("Erro ao exportar dados do processador: " + e.getMessage());
        }
    }

    public void exportRaceData(String fileName) {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("Carro,Tempo Decorrido (ms),Distancia Percorrida");
            for (String record : raceData) {
                writer.println(record);
            }
            LOGGER.info("Dados da corrida exportados para: " + fileName);
        } catch (Exception e) {
            LOGGER.severe("Erro ao exportar dados da corrida: " + e.getMessage());
        }
    }

    public void exportAsJson(String fileName) {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            Gson gson = new Gson();
            writer.println(gson.toJson(raceData));
            LOGGER.info("Dados da corrida exportados em JSON para: " + fileName);
        } catch (Exception e) {
            LOGGER.severe("Erro ao exportar dados em JSON: " + e.getMessage());
        }
    }

    public void calculateEquations() {
        LOGGER.info("Calculos para Escalonamento Real-Time:");

        for (Car car : cars) {
            double utilization = car.getProcessingTime() / (double) DEADLINE_MS;
            LOGGER.info(car.getName() + " - Utilizacao: " + String.format(Locale.US, "%.2f", utilization));
        }

        double totalUtilization = cars.stream()
                .mapToDouble(car -> car.getProcessingTime() / (double) DEADLINE_MS)
                .sum();

        LOGGER.info("Utilizacao total do processador: " + String.format(Locale.US, "%.2f", totalUtilization));
        LOGGER.info("Sistema escalonavel: " + (totalUtilization <= 1.0 ? "Sim" : "Não"));
    }

    public static void main(String[] args) {
        RealTimeRaceManagerWithEquations manager = new RealTimeRaceManagerWithEquations();
        manager.initializeCars();
        manager.startRace();
        manager.calculateEquations();

        // Exportação dos resultados
        manager.exportProcessorUsage("processor_usage.csv");
        manager.exportRaceData("race_data.csv");
        manager.exportAsJson("race_data.json");
    }
}
