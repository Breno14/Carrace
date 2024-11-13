package com.example.carrace;

public abstract class Vehicle implements Runnable {
    protected String name;
    protected int x, y;
    protected double speed;
    protected int laps;
    protected int fuel;
    protected int distance;
    protected int penalty;
    protected boolean isRunning = true;

    public Vehicle(String name, int initialX, int initialY, double speed) {
        this.name = name;
        this.x = initialX;
        this.y = initialY;
        this.speed = speed;
        this.laps = 0;
        this.fuel = 1000;
        this.distance = 0;
        this.penalty = 0;
    }

    // Método para iniciar a corrida do veículo (definido para polimorfismo)
    public void start() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start(); // Inicia o veículo em uma nova Thread
        }
    }

    // Método para parar a corrida do veículo
    public void stop() {
        isRunning = false;
    }

    // Método abstrato para movimentação que deve ser implementado por subclasses
    public abstract void move();

    // Novo método abstrato para incrementar penalidade
    public abstract void incrementPenalty();

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    // Métodos de acesso comuns a todos os veículos
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getSpeed() { return speed; }
    public int getDistance() { return distance; }
    public int getPenalty() { return penalty; }
    public int getLaps() { return laps; }
}
