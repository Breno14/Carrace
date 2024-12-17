package com.example.carrace;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class Track {
    private Bitmap trackImage;
    private int[][] trackMap;

    public Track(Bitmap trackImage) {
        this.trackImage = trackImage;
        this.trackMap = new int[trackImage.getWidth()][trackImage.getHeight()];
        loadTrack();
    }

    public int getWidth() {
        return trackImage.getWidth();
    }

    public int getHeight() {
        return trackImage.getHeight();
    }

    // Método para verificar se um pixel é branco ou próximo de branco
    private boolean isWhite(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return red > 200 && green > 200 && blue > 200; // Tolerância para tons próximos ao branco
    }

    // Método para carregar o mapa da pista
    private void loadTrack() {
        try {
            for (int x = 0; x < trackImage.getWidth(); x++) {
                for (int y = 0; y < trackImage.getHeight(); y++) {
                    int pixelColor = trackImage.getPixel(x, y);

                    // Define branco ou próximo de branco como pista
                    if (isWhite(pixelColor)) {
                        trackMap[x][y] = 1;  // Área de pista
                    } else {
                        trackMap[x][y] = 0;  // Obstáculo
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Track", "Erro ao carregar a pista: " + e.getMessage());
            throw new RuntimeException("Falha ao carregar o mapa da pista", e);
        }
    }

    // Método para verificar se a posição está na pista
    public boolean isOnTrack(int x, int y) {
        if (x < 0 || x >= trackMap.length || y < 0 || y >= trackMap[0].length) {
            Log.d("Track", "Coordenadas fora dos limites: (" + x + ", " + y + ")");
            return false;  // Fora dos limites do bitmap
        }
        boolean result = trackMap[x][y] == 1;
        Log.d("Track", "Coordenadas (" + x + ", " + y + ") são " + (result ? "válidas" : "inválidas"));
        return result;
    }
}
