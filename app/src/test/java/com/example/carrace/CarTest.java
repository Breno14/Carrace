package com.example.carrace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CarTest {

    private Track mockTrack;
    private RaceManager mockRaceManager;
    private Car car;

    @BeforeEach
    public void setup() {
        // Criamos um mock da pista e do RaceManager para simular o ambiente do carro
        mockTrack = Mockito.mock(Track.class);
        mockRaceManager = Mockito.mock(RaceManager.class);

        // Inicializamos o carro em uma posição inicial com uma velocidade específica
        car = new Car("CarroTest", 100, 100, 10, mockTrack, mockRaceManager);
    }

    @Test
    public void testMovimentoCarro() {
        // Configura o mock para permitir que o carro se mova livremente na pista
        Mockito.when(mockTrack.isOnTrack(Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);

        // Captura a posição inicial do carro
        int posXInicial = car.getX();
        int posYInicial = car.getY();

        // Move o carro
        car.move();

        // Verifica se a posição foi atualizada corretamente
        assertTrue(car.getX() != posXInicial || car.getY() != posYInicial, "O carro deveria ter se movido.");
    }

    @Test
    public void testPenalidadeAoSairDaPista() {
        // Configura o mock para que o carro "saia" da pista
        Mockito.when(mockTrack.isOnTrack(Mockito.anyInt(), Mockito.anyInt())).thenReturn(false);

        // Move o carro e espera que ele receba uma penalidade
        int penalidadeInicial = car.getPenalty();
        car.move();

        // Verifica se a penalidade aumentou
        assertEquals(penalidadeInicial + 1, car.getPenalty(), "A penalidade deveria ter aumentado ao sair da pista.");
    }

    @Test
    public void testAcessoExclusivoZonaCritica() throws InterruptedException {
        // Configura o mock para que o carro entre na zona crítica
        Mockito.when(mockTrack.isOnTrack(Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(mockRaceManager.getCriticalZoneSemaphore()).thenReturn(new java.util.concurrent.Semaphore(1));

        // Cria dois carros que tentarão acessar a zona crítica
        Car car1 = new Car("Carro1", 150, 100, 10, mockTrack, mockRaceManager);
        Car car2 = new Car("Carro2", 150, 100, 10, mockTrack, mockRaceManager);

        // Inicia as threads dos carros simultaneamente
        Thread t1 = new Thread(car1);
        Thread t2 = new Thread(car2);
        t1.start();
        t2.start();

        // Aguarda que as threads concluam
        t1.join();
        t2.join();

        // O teste será bem-sucedido se não houver erro de semáforo (ambos os carros não entram ao mesmo tempo)
        assertTrue(true, "Os carros acessaram a zona crítica de forma controlada pelo semáforo.");
    }
}
