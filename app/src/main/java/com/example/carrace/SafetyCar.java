package com.example.carrace;

public class SafetyCar extends Car {

    private boolean active = false; // Define se o Safety Car está ativo

    public SafetyCar(String name, int initialX, int initialY, double speed, Track track, RaceManager raceManager) {
        super(name, initialX, initialY, speed, track, raceManager);
    }

    // Método para ativar o Safety Car
    public void activate() {
        active = true;
        System.out.println(name + " foi ativado.");
    }

    // Método para desativar o Safety Car
    public void deactivate() {
        active = false;
        stop(); // Para a execução do Safety Car
        System.out.println(name + " foi desativado.");
    }

    @Override
    public void run() {
        // Configura prioridade máxima para a thread do Safety Car
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        // Executa o método run da superclasse (Car)
        super.run();
    }

    @Override
    public void move() {
        if (!active) {
            return; // Não se move se o Safety Car não estiver ativo
        }

        // Reduz a velocidade para um limite ao estar ativo
        speed = Math.min(speed, 50); // Limita a velocidade a 50

        // Executa a lógica de movimentação padrão
        super.move();
    }
}
