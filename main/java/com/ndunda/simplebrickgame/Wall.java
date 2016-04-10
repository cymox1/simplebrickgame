package com.ndunda.simplebrickgame;

import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by simon on 4/10/16.
 */
public class Wall {

    private int screenWidth;
    private int screenHeight;
    private int gap = 0;
    private ArrayList<Rect> wallrects = new ArrayList<Rect>();

    public Wall(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void addBrick(Brick brick) {

        for (Rect brickRect: brick.getCells()) {
            wallrects.add(brickRect);
        }
    }

    public boolean brickPositionAllowed(Brick brick) {
        for (Rect brickRect: brick.getCells()) {
            if (brickRect.bottom > screenHeight) {
                brick.setYPosition(screenHeight - (brick.height(brickRect) + gap));
                return false; //hit bottom wall
            }

            for (Rect r : wallrects) {
                if (Rect.intersects(r, brickRect)) {
//                    brick.setYPosition(r.top - (brick.height(brickRect) + gap));
                    return false; //hit other bricks in wall
                }
            }
        }
        return true;
    }

    public ArrayList<Rect> getWallRects() {
        return wallrects;
    }
}
