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

    private void loadTrack() {
        try {
            for (int x = 0; x < trackImage.getWidth(); x++) {
                for (int y = 0; y < trackImage.getHeight(); y++) {
                    int pixelColor = trackImage.getPixel(x, y);
                    if (pixelColor == Color.WHITE) {
                        trackMap[x][y] = 1;  // Área de pista
                    } else {
                        trackMap[x][y] = 0;  // Tratado como obstáculo
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Track", "Erro ao carregar a pista: " + e.getMessage());
        }
    }

    public boolean isOnTrack(int x, int y) {
        if (x < 0 || x >= trackMap.length || y < 0 || y >= trackMap[0].length) {
            return false;  // Fora dos limites do bitmap
        }
        return trackMap[x][y] == 1;  // Retorna verdadeiro se for área de pista
    }
}
