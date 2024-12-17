package com.example.carrace;

import java.util.HashMap;
import java.util.Map;

public class Car extends Vehicle implements Comparable<Car> {
    private Map<String, Integer> sensor = new HashMap<>();
    private double angle;
    private Track track;
    private RaceManager raceManager;
    private long processingTime; // Tempo total de processamento
    private long responseTime;   // Tempo de resposta para análise de escalonabilidade

    public Car(String name, int initialX, int initialY, double speed, Track track, RaceManager raceManager) {
        super(name, initialX, initialY, speed);
        this.angle = 0;
        this.track = track;
        this.raceManager = raceManager;
        this.processingTime = 0;
        this.responseTime = 0;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (isRunning) {
            try {
                long moveStart = System.nanoTime();
                move(); // Movimento do carro
                long moveEnd = System.nanoTime();

                processingTime += (moveEnd - moveStart); // Atualiza o tempo de processamento
                Thread.sleep(100); // Controla a velocidade do movimento
            } catch (InterruptedException e) {
                System.out.println(name + " foi interrompido.");
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void move() {
        long startTime = System.nanoTime();

        consumeFuel(1); // Consome combustível a cada movimento
        if (fuel <= 0) {
            stop();
            return;
        }

        try {
            updateSensors(track);
            executeAvoidance();

            int deltaX = (int) (Math.cos(Math.toRadians(angle)) * speed);
            int deltaY = (int) (Math.sin(Math.toRadians(angle)) * speed);

            int newX = x + deltaX;
            int newY = y + deltaY;

            if (track.isOnTrack(newX, newY)) {
                x = newX;
                y = newY;
                distance++;
            } else {
                incrementPenalty();
                adjustDirection(45); // Ajusta a direção ao sair da pista
                System.out.println(name + " saiu da pista e foi penalizado.");
            }
        } catch (Exception e) {
            System.out.println("Erro no movimento de " + name + ": " + e.getMessage());
        }

        long endTime = System.nanoTime();
        responseTime = endTime - startTime; //tempo de resposta
    }

    @Override
    public void incrementPenalty() {
        penalty++;
    }

    // Implementação do método compareTo exigido pelo PriorityBlockingQueue
    @Override
    public int compareTo(Car otherCar) {
        return Integer.compare(this.getDistance(), otherCar.getDistance());
    }

    public synchronized void updateSensors(Track track) {
        sensor.put("Frente", checkProximity(x + (int)(Math.cos(Math.toRadians(angle)) * 5),
                y + (int)(Math.sin(Math.toRadians(angle)) * 5), track));
        sensor.put("Esquerda", checkProximity(x + (int)(Math.cos(Math.toRadians(angle - 90)) * 5),
                y + (int)(Math.sin(Math.toRadians(angle - 90)) * 5), track));
        sensor.put("Direita", checkProximity(x + (int)(Math.cos(Math.toRadians(angle + 90)) * 5),
                y + (int)(Math.sin(Math.toRadians(angle + 90)) * 5), track));
    }

    private int checkProximity(int x, int y, Track track) {
        return track.isOnTrack(x, y) ? 1 : 0;
    }

    private void executeAvoidance() {
        int frente = sensor.getOrDefault("Frente", 1);
        int esquerda = sensor.getOrDefault("Esquerda", 1);
        int direita = sensor.getOrDefault("Direita", 1);

        if (frente == 0) {
            if (esquerda == 1) {
                adjustDirection(-15);
            } else if (direita == 1) {
                adjustDirection(15);
            } else {
                adjustDirection(180);
            }
        } else if (esquerda == 0 && direita == 1) {
            adjustDirection(10);
        } else if (direita == 0 && esquerda == 1) {
            adjustDirection(-10);
        }
    }

    public void adjustDirection(int adjustAngle) {
        angle += adjustAngle;
        angle %= 360;
    }

    public void adjustPriority(int priority) {
        Thread.currentThread().setPriority(priority);
    }

    public void adjustSpeed(int increment) {
        this.speed += increment;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public Map<String, Integer> getSensorData() {
        return sensor;
    }

    @Override
    public String toString() {
        return name + " - Distância: " + distance + ", Penalidades: " + penalty;
    }
}
