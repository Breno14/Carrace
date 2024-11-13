package com.example.carrace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Car extends Vehicle {
    private Map<String, Integer> sensor = new HashMap<>();
    private double angle;
    private Track track;
    private RaceManager raceManager;

    public Car(String name, int initialX, int initialY, double speed, Track track, RaceManager raceManager) {
        super(name, initialX, initialY, speed);
        this.angle = 0;
        this.track = track;
        this.raceManager = raceManager;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                move(); // Movimento do carro
                Thread.sleep(100); // Controla a velocidade do movimento
            } catch (InterruptedException e) {
                System.out.println(name + " foi interrompido: " + e.getMessage());
                Thread.currentThread().interrupt(); // Retoma o estado de interrupção da Thread
            } catch (Exception e) {
                System.out.println("Erro inesperado no carro " + name + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void move() {
        try {
            updateSensors(track); // Atualiza sensores antes de se mover
            executeAvoidance();   // Ajusta direção conforme sensores

            // Calcula a nova posição baseada no ângulo e na velocidade
            int deltaX = (int) (Math.cos(Math.toRadians(angle)) * speed);
            int deltaY = (int) (Math.sin(Math.toRadians(angle)) * speed);

            int newX = x + deltaX;
            int newY = y + deltaY;

            boolean inCriticalZone = newX >= 140 && newX <= 160;

            if (inCriticalZone) {
                enterCriticalZone(newX, newY); // Entra na zona crítica se aplicável
            } else {
                if (track.isOnTrack(newX, newY)) { // Verifica se a nova posição está na pista
                    if (newX >= 0 && newX < track.getWidth() && newY >= 0 && newY < track.getHeight()) {
                        x = newX;
                        y = newY;
                        distance++;
                    }
                } else {
                    incrementPenalty();
                    adjustDirection(90); // Reverte a direção ao sair da pista
                }
            }
        } catch (Exception e) {
            System.out.println("Erro no movimento do carro " + name + ": " + e.getMessage());
        }
    }

    @Override
    public void incrementPenalty() {
        penalty++;
    }

    // Método para atualizar as leituras dos sensores
    public void updateSensors(Track track) {
        sensor.put("Frente", checkProximity(x + (int)(Math.cos(Math.toRadians(angle)) * 5),
                y + (int)(Math.sin(Math.toRadians(angle)) * 5), track));
        sensor.put("Esquerda", checkProximity(x + (int)(Math.cos(Math.toRadians(angle - 90)) * 5),
                y + (int)(Math.sin(Math.toRadians(angle - 90)) * 5), track));
        sensor.put("Direita", checkProximity(x + (int)(Math.cos(Math.toRadians(angle + 90)) * 5),
                y + (int)(Math.sin(Math.toRadians(angle + 90)) * 5), track));
    }

    // Método auxiliar para verificar a proximidade de obstáculos
    private int checkProximity(int x, int y, Track track) {
        return track.isOnTrack(x, y) ? 1 : 0; // Retorna 1 se estiver na pista, 0 se for obstáculo ou limite
    }

    // Método para ajustar direção com base nos sensores
    private void executeAvoidance() {
        Integer frente = sensor.get("Frente");
        Integer esquerda = sensor.get("Esquerda");
        Integer direita = sensor.get("Direita");

        if (frente != null && frente == 0) { // Obstáculo à frente
            if (esquerda != null && esquerda == 1) {
                adjustDirection(-8); // Gira levemente para a esquerda
            } else if (direita != null && direita == 1) {
                adjustDirection(8); // Gira levemente para a direita
            } else {
                adjustDirection(45); // Reverte se não há outra direção
            }
        } else {
            if (esquerda != null && esquerda == 1 && Math.random() > 0.5) {
                adjustDirection(-5); // Ajusta levemente à esquerda
            } else if (direita != null && direita == 1 && Math.random() > 0.5) {
                adjustDirection(5); // Ajusta levemente à direita
            }
        }
    }

    private void enterCriticalZone(int newX, int newY) {
        try {
            if (raceManager.getCriticalZoneSemaphore().tryAcquire(500, TimeUnit.MILLISECONDS)) { // Com timeout
                System.out.println(name + " entrou na zona crítica.");
                x = newX;
                y = newY;
                distance++;
            } else {
                System.out.println(name + " não conseguiu entrar na zona crítica.");
            }
        } catch (InterruptedException e) {
            System.out.println(name + " foi interrompido na zona crítica: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            raceManager.getCriticalZoneSemaphore().release();
            System.out.println(name + " saiu da zona crítica.");
        }
    }

    public void adjustDirection(int adjustAngle) {
        angle += adjustAngle;
        angle %= 360;  // Mantém o ângulo entre 0 e 359
    }

    // Getters para obter informações
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getSpeed() { return speed; }
    public int getDistance() { return distance; }
    public int getPenalty() { return penalty; }
    public int getLaps() { return laps; }

    // Método para retornar os dados do sensor
    public Map<String, Integer> getSensorData() {
        return sensor;
    }
}
