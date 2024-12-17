package com.example.carrace;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements RaceUpdateListener {

    private RaceManager raceManager;
    private Track track;
    private Car[] cars;
    private TextView lapsInfo;
    private TextView penaltiesInfo;
    private EditText numCarsInput;
    private ArrayList<View> carViews = new ArrayList<>();
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Carregar a imagem da pista
        Bitmap trackImage = BitmapFactory.decodeResource(getResources(), R.drawable.track);
        if (trackImage == null) {
            Toast.makeText(this, "Erro ao carregar a imagem da pista.", Toast.LENGTH_SHORT).show();
            return;
        }
        track = new Track(trackImage);

        // Inicializar componentes de UI
        lapsInfo = findViewById(R.id.lapsInfo);
        penaltiesInfo = findViewById(R.id.penaltiesInfo);
        numCarsInput = findViewById(R.id.numCarsInput);
        Button startButton = findViewById(R.id.startButton);
        Button pauseButton = findViewById(R.id.pauseButton);
        Button finishButton = findViewById(R.id.finishButton);
        Button testRealTimeRaceButton = findViewById(R.id.testRealTimeRaceButton); // Botão Teste Real-Time Race

        // Configurar o botão Test Real-Time Race
        testRealTimeRaceButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Teste Real-Time Race em andamento...", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                RealTimeRaceManagerWithEquations manager = new RealTimeRaceManagerWithEquations();
                manager.initializeCars();
                manager.startRace();
                manager.calculateEquations();

                // Chamar métodos de exportação explicitamente
                manager.exportProcessorUsage("processor_usage.csv");
                manager.exportRaceData("race_data.csv");

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Teste Real-Time Race finalizado. Dados exportados!", Toast.LENGTH_LONG).show();
                });
            }).start();
        });

        // Configurar botões Start, Pause e Finish
        startButton.setOnClickListener(v -> startRace());

        pauseButton.setOnClickListener(v -> {
            if (raceManager != null) {
                raceManager.pauseRace();
                stopRaceExecutor();
            }
        });

        finishButton.setOnClickListener(v -> {
            if (raceManager != null) {
                raceManager.finishRace();
                updateRaceInfo();
                stopRaceExecutor();
            }
        });
    }


    private void startRace() {
        String numCarsText = numCarsInput.getText().toString();
        try {
            int numCars = Integer.parseInt(numCarsText);
            if (numCars <= 0) throw new NumberFormatException();

            cars = new Car[numCars];

            FrameLayout carContainer = findViewById(R.id.trackContainer);
            carContainer.removeAllViews();
            carViews.clear();

            // Passar o Context junto com o Track
            raceManager = new RaceManager(this, track);
            raceManager.setRaceUpdateListener(this);

            // Inicializar o Safety Car com prioridade máxima
            SafetyCar safetyCar = new SafetyCar("Safety Car", 320, 500, 8, track, raceManager);
            raceManager.addVehicle(safetyCar);

            // Definir a prioridade da thread do Safety Car
            Thread safetyCarThread = new Thread(safetyCar);
            safetyCarThread.setPriority(Thread.MAX_PRIORITY); // Prioridade máxima para o Safety Car
            safetyCarThread.start(); // Inicia a thread do Safety Car

            // Configura o ExecutorService para os demais carros
            executorService = Executors.newFixedThreadPool(numCars);
            for (int i = 0; i < numCars; i++) {
                Car car = new Car("Carro " + (i + 1), 310 + (i * 20), 640, 5 + (i / 2), track, raceManager);
                cars[i] = car;
                raceManager.addVehicle(car);

                View carView = new View(this);
                int colorResId = getResources().getIdentifier("carColor" + i, "color", getPackageName());
                if (colorResId == 0) {
                    colorResId = R.color.defaultCarColor; // Cor padrão
                }
                carView.setBackgroundColor(getResources().getColor(colorResId));
                carView.setId(View.generateViewId());

                int pointSize = 20;
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pointSize, pointSize);
                carView.setLayoutParams(params);

                carContainer.addView(carView);
                carViews.add(carView);

                executorService.submit(car); // Envia os carros regulares para o ExecutorService
            }
            raceManager.startRace();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Número de carros inválido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRaceExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private long lastUpdateTime = 0;

    @Override
    public void onRaceUpdate() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 500) {
            runOnUiThread(() -> {
                updateRaceInfo();
                updateCarPositions();
            });
            lastUpdateTime = currentTime;
        }
    }

    private void updateRaceInfo() {
        StringBuilder lapsText = new StringBuilder("Voltas: ");
        StringBuilder penaltiesText = new StringBuilder("Penalidades: ");
        for (Car car : cars) {
            lapsText.append(car.getName()).append(": ").append(car.getLaps()).append(" ");
            penaltiesText.append(car.getName()).append(": ").append(car.getPenalty()).append(" ");
        }
        lapsInfo.setText(lapsText.toString());
        penaltiesInfo.setText(penaltiesText.toString());
    }

    private void updateCarPositions() {
        for (int i = 0; i < cars.length; i++) {
            Car car = cars[i];
            View carView = carViews.get(i);
            if (carView.getX() != car.getX() || carView.getY() != car.getY()) {
                carView.setX(car.getX());
                carView.setY(car.getY());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRaceExecutor(); // Encerra o ExecutorService ao destruir a atividade
    }
}
