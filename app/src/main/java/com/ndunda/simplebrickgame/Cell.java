package com.ndunda.simplebrickgame;

import android.graphics.Rect;

/**
 * Created by simon on 4/20/16.
 */
public class Cell {
    int color;
    Rect rect;

    public Cell(Rect r, int color) {
        this.rect = r;
        this.color = color;
    }


}
