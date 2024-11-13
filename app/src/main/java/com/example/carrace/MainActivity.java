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

        // Configurar botões
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRace();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (raceManager != null) {
                    raceManager.pauseRace();
                    stopRaceExecutor();
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (raceManager != null) {
                    raceManager.finishRace();
                    updateRaceInfo();
                    stopRaceExecutor();
                }
            }
        });
    }

    private void startRace() {
        String numCarsText = numCarsInput.getText().toString();
        if (numCarsText.isEmpty()) {
            Toast.makeText(this, "Insira o número de carros", Toast.LENGTH_SHORT).show();
            return;
        }

        int numCars = Integer.parseInt(numCarsText);
        cars = new Car[numCars];

        FrameLayout carContainer = findViewById(R.id.trackContainer);
        carContainer.removeAllViews();
        carViews.clear();

        raceManager = new RaceManager(track);
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
            carView.setBackgroundColor(getResources().getColor(R.color.carColor + i));
            carView.setId(View.generateViewId());

            int pointSize = 20;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pointSize, pointSize);
            carView.setLayoutParams(params);

            carContainer.addView(carView);
            carViews.add(carView);

            executorService.submit(car); // Envia os carros regulares para o ExecutorService
        }
        raceManager.startRace();
    }


    private void stopRaceExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
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
            carView.setX(car.getX());
            carView.setY(car.getY());
        }
    }

@Override
    protected void onDestroy() {
        super.onDestroy();
        stopRaceExecutor(); // Encerra o ExecutorService ao destruir a atividade
    }
}
