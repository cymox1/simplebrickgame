package com.ndunda.simplebrickgame;

import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by simon on 4/10/16.
 */
public class Brick {
    //Brick Types
    private static int TYPE_FLAT_LINE = 1;
    private static int TYPE_VERT_LINE = 2;
    private static int TYPE_TEE = 3;
    private static int TYPE_L = 4;
    private static int TYPE_INV_L = 5;
    private static int TYPE_PLUS = 6;
    private static int[] BRICK_TYPES = new int[]{TYPE_FLAT_LINE, TYPE_VERT_LINE, TYPE_TEE, TYPE_L, TYPE_INV_L, TYPE_PLUS};

    private int brickYPosition = 0;
    private double brickSize;
    private int brickXPosition;
    private int journeySeconds = 3;
    private int screenWidth;
    private int screenHeight;
    private Wall wall;
    private long startTime;
    private int rowCount = 18;
    private int brickType;
    private int rotation = 0;


    public Brick(int screenWidth, int screenHeight, Wall wall) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.wall = wall;
        brickSize = Math.ceil((float) (1 * screenWidth) / rowCount);
        float gap = (float) (0 * screenWidth) / (rowCount - 1);
        int brickRow = (int) (Math.random() * rowCount);
        brickType = BRICK_TYPES[(int) (Math.random() * BRICK_TYPES.length)];
        brickType = TYPE_INV_L;
        rotation = 90;
        brickXPosition = (int) (brickRow * (brickSize + gap));
    }

    public ArrayList<Rect> getCells() {
        int bsize = (int) brickSize;
        int xpos = brickXPosition;
        int ypos = brickYPosition;
        ArrayList<Rect> brickCells = new ArrayList<Rect>();
        if (brickType == TYPE_FLAT_LINE) {
            if (rotation % 180 == 90) {
                xpos += bsize;
            }
            for (int k = 0; k < 4; k++) {
                brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                if (rotation % 180 == 0) {
                    xpos += bsize;
                } else {
                    ypos += bsize;
                }
            }
        } else if (brickType == TYPE_TEE) {
            int vxpos = brickXPosition + bsize;
            for (int k = 0; k < 4; k++) {
                if (k < 3) {
                    brickCells.add(new Rect(xpos, brickYPosition, xpos + bsize, brickYPosition + bsize));
                    xpos += bsize;
                } else {
                    ypos += bsize;
                    brickCells.add(new Rect(vxpos, ypos, vxpos + bsize, ypos + bsize));
                }
            }
        } else if (brickType == TYPE_L) {
            int vypos = brickYPosition + bsize + bsize;
            for (int k = 0; k < 4; k++) {
                if (k < 3) {
                    brickCells.add(new Rect(brickXPosition, ypos, brickXPosition + bsize, ypos + bsize));
                    ypos += bsize;
                } else {
                    xpos += bsize;
                    brickCells.add(new Rect(xpos, vypos, xpos + bsize, vypos + bsize));
                }
            }
        } else if (brickType == TYPE_INV_L) {
            for (int k = 0; k < 4; k++) {
                if (k < 3) {
                    brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                    if (rotation % 180 == 0) {
                        ypos += bsize;
                    } else {
                        xpos += bsize;
                    }
                } else {
                    if (rotation % 180 == 0) {
                        xpos -= bsize;
                        ypos -= bsize;
                    } else {
                        ypos -= bsize;
                        xpos -= bsize * 3;
                    }
                    brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                }
            }
        } else if (brickType == TYPE_PLUS) {
            int vypos = brickYPosition + bsize;
            for (int k = 0; k < 5; k++) {
                if (k < 3) {
                    brickCells.add(new Rect(brickXPosition, ypos, brickXPosition + bsize, ypos + bsize));
                    ypos += bsize;
                } else {
                    if (k == 3) {
                        xpos -= bsize;
                    } else {
                        xpos += (bsize + bsize);
                    }
                    brickCells.add(new Rect(xpos, vypos, xpos + bsize, vypos + bsize));
                }
            }
        } else if (brickType == TYPE_VERT_LINE) {
            if (rotation % 180 == 0) {
                xpos += bsize;
            }
            for (int k = 0; k < 4; k++) {
                brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                if (rotation % 180 == 0) {
                    ypos += bsize;
                } else {
                    xpos += bsize;
                }
            }
        } else {
            Log.e("getCells", "Unknow brick type " + brickType);
        }
        return brickCells;
    }

    public int height() {
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;

        for (Rect r : getCells()) {
            if (r.top < top) {
                top = r.top;
            }
            if (r.bottom > bottom) {
                bottom = r.bottom;
            }
        }
        return bottom - top;
    }

    public int height(Rect fromCell) {
        int top = Integer.MAX_VALUE;
        int bottom = fromCell.bottom;

        for (Rect r : getCells()) {
            if (r.top < top) {
                top = r.top;
            }
        }
        return bottom - top;
    }

    public void rotate() {
        rotation = (rotation + 90) % 360;
    }

    public Brick update() {
        if (startTime > 0) {
            long currentTime = System.currentTimeMillis();
            int oldYpos = this.brickYPosition;
            this.brickYPosition = (int) (((float) (currentTime - startTime) / (journeySeconds * 1000)) * screenHeight);
            if (wall.brickPositionAllowed(this)) {
                return this;
            } else {
//                Log.d("Restore", "from "+brickYPosition+" back to "+oldYpos);
//                this.brickYPosition = oldYpos;
                wall.addBrick(this);
                return new Brick(screenWidth, screenHeight, wall);
            }
        } else {
            startTime = System.currentTimeMillis();
            return this;
        }
    }

    public void setYPosition(int yposition) {
        brickYPosition = yposition;
    }
}
