package com.example.carrace;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class RaceManager {
    private Track track;
    private List<Vehicle> vehicles = new ArrayList<>();
    private boolean isRaceActive;
    private RaceUpdateListener listener;
    private Handler handler = new Handler();
    private final int UPDATE_INTERVAL = 100;
    private Context context;

    // Semáforo para controlar a zona crítica
    private final Semaphore criticalZoneSemaphore = new Semaphore(1); // Apenas 1 carro por vez

    // Firebase Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RaceManager(Context context, Track track) {
        this.context = context;
        this.track = track;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void setRaceUpdateListener(RaceUpdateListener listener) {
        this.listener = listener;
    }

    public void startRace() {
        isRaceActive = true;
        loadRaceState(); // Carrega o último estado salvo dos carros

        for (Vehicle vehicle : vehicles) {
            vehicle.start();
        }

        handler.post(raceRunnable);
    }

    public Semaphore getCriticalZoneSemaphore() {
        return criticalZoneSemaphore;
    }

    public void pauseRace() {
        isRaceActive = false;
        for (Vehicle vehicle : vehicles) {
            vehicle.stop(); // Para cada veículo
        }
        saveRaceState(); // Salva o estado dos carros ao pausar
    }

    public void finishRace() {
        // Parar a corrida
        isRaceActive = false;

        // Parar o Runnable de atualização de corrida
        handler.removeCallbacks(raceRunnable);

        // Parar cada veículo e verificar seu status
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isRunning()) {
                vehicle.stop();
            }
            Log.d("RaceManager", vehicle.getName() + " foi parado.");
        }

        // Salvar o estado final da corrida no Firestore
        saveRaceState();

        Log.d("RaceManager", "Corrida finalizada e recursos liberados.");
    }

    // Método para salvar o estado de todos os carros no Firestore
    public synchronized void saveRaceState() {
        for (Vehicle vehicle : vehicles) {
            Map<String, Object> carData = new HashMap<>();
            carData.put("name", vehicle.getName());
            carData.put("x", vehicle.getX());
            carData.put("y", vehicle.getY());
            carData.put("speed", vehicle.getSpeed());
            carData.put("laps", vehicle.getLaps());
            carData.put("penalty", vehicle.getPenalty());
            carData.put("distance", vehicle.getDistance());

            db.collection("raceState").document(vehicle.getName())
                    .set(carData)
                    .addOnSuccessListener(aVoid -> Log.d("RaceManager", vehicle.getName() + " salvo com sucesso."))
                    .addOnFailureListener(e -> {
                        Log.e("RaceManager", "Erro ao salvar " + vehicle.getName() + ": " + e.getMessage());
                        Toast.makeText(context, "Falha ao salvar dados do veículo: " + vehicle.getName(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Método para carregar o estado de todos os carros no Firestore
    public synchronized void loadRaceState() {
        db.collection("raceState").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        int x = document.getLong("x") != null ? document.getLong("x").intValue() : 0;
                        int y = document.getLong("y") != null ? document.getLong("y").intValue() : 0;
                        double speed = document.getDouble("speed") != null ? document.getDouble("speed") : 0.0;
                        int laps = document.getLong("laps") != null ? document.getLong("laps").intValue() : 0;
                        int penalty = document.getLong("penalty") != null ? document.getLong("penalty").intValue() : 0;
                        int distance = document.getLong("distance") != null ? document.getLong("distance").intValue() : 0;

                        for (Vehicle vehicle : vehicles) {
                            if (vehicle.getName().equals(name)) {
                                vehicle.setX(x);
                                vehicle.setY(y);
                                vehicle.setSpeed(speed);
                                vehicle.setLaps(laps);
                                vehicle.setPenalty(penalty);
                                vehicle.setDistance(distance);
                            }
                        }
                    }
                    Log.d("RaceManager", "Estado da corrida carregado com sucesso.");
                })
                .addOnFailureListener(e -> Log.e("RaceManager", "Erro ao carregar o estado da corrida: " + e.getMessage()));
    }

    private final Runnable raceRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRaceActive) {
                detectCollisions();

                if (listener != null) {
                    listener.onRaceUpdate();
                }

                handler.postDelayed(this, UPDATE_INTERVAL);
            } else {
                Log.d("RaceManager", "Runnable foi interrompido.");
            }
        }
    };

    private void detectCollisions() {
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicleA = vehicles.get(i);
            for (int j = i + 1; j < vehicles.size(); j++) {
                Vehicle vehicleB = vehicles.get(j);
                if (Math.abs(vehicleA.getX() - vehicleB.getX()) < 20 && Math.abs(vehicleA.getY() - vehicleB.getY()) < 20) {
                    vehicleA.incrementPenalty();
                    vehicleB.incrementPenalty();
                    Log.d("RaceManager", "Colisão detectada entre " + vehicleA.getName() + " e " + vehicleB.getName());
                }
            }
        }
    }

    public void enterCriticalZone(Vehicle vehicle) {
        try {
            criticalZoneSemaphore.acquire();
            Log.d("RaceManager", vehicle.getName() + " entrou na zona crítica.");
            // Processa lógica específica aqui
        } catch (InterruptedException e) {
            Log.e("RaceManager", "Erro ao acessar zona crítica: " + e.getMessage());
        } finally {
            criticalZoneSemaphore.release();
            Log.d("RaceManager", vehicle.getName() + " saiu da zona crítica.");
        }
    }
}
