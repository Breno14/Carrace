package com.example.carrace;

public class SafetyCar extends Car {

    public SafetyCar(String name, int initialX, int initialY, double speed, Track track, RaceManager raceManager) {
        super(name, initialX, initialY, speed, track, raceManager);
    }

    @Override
    public void run() {
        // Configura prioridade máxima para a thread do Safety Car
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        // Executa o método run da superclasse (Car)
        super.run();
    }
}
