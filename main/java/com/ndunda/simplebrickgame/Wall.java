package com.ndunda.simplebrickgame;

import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by simon on 4/10/16.
 */
public class Wall {
    private ArrayList<Rect> wallrects = new ArrayList<Rect>();

    public void addBrick(Brick brick) {

        for (Rect brickRect: brick.getCells()) {
            wallrects.add(brickRect);
        }
    }

    public ArrayList<Rect> getWallRects() {
        return wallrects;
    }
}
