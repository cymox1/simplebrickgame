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

    private int brickYPosition = 30;
    private double brickSize;
    private int brickXPosition;
    private int journeySeconds = 10;
    private int screenWidth;
    private int screenHeight;
    private Wall wall;
    private long startTime;
    private int rowCount = 20;
    private int brickType;
    private int rotation = 0;
    private int acceleration = 1;


    public Brick(int screenWidth, int screenHeight, Wall wall) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.wall = wall;
        brickSize = (float) (1 * screenWidth) / rowCount;
        float gap = (float) (0 * screenWidth) / (rowCount - 1);
        int brickRow = rowCount / 2;
        brickType = BRICK_TYPES[(int) (Math.random() * BRICK_TYPES.length)];
        brickType = TYPE_FLAT_LINE;
//        rotation = 90;
        brickXPosition = (int) Math.ceil(brickRow * (brickSize + gap));
    }

    public void setYPosition(int yposition) {
        Log.i("SetPositon", yposition + "");
        brickYPosition = yposition;
    }

    public int width() {
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;

        for (Rect r : getCells()) {
            if (r.left < left) {
                left = r.left;
            }
            if (r.right > right) {
                right = r.right;
            }
        }
        return right - left;
    }

    public int height(Rect fromCell) {
        int top = Integer.MAX_VALUE;
        int bottom = fromCell.bottom;
        Log.i("Bottom", bottom + "");

        for (Rect r : getCells()) {
            if (r.top < top) {
                top = r.top;
            }
        }
        Log.i("Height", "" + (bottom - top));
        return bottom - top;
    }

    public void rotate() {
        rotation = (rotation + 90) % 360;
        validatePosition();
    }

    public void translate(float hdistance, float vdistance) {
        if (Math.abs(hdistance) > 50) {
            translate((int)(hdistance / 50));
        } else if (vdistance > 50) {
//            journeySeconds = 3;
        }
        validatePosition();
    }

    public void translate(int rows) {
        Log.i("Translating", "Was " + brickXPosition + " Add " + rows);
        brickXPosition += rows * brickSize;
    }

    public void validatePosition() {
        int leftmost = Integer.MAX_VALUE;
        int rightmost = Integer.MIN_VALUE;
        for (Rect r : getCells()) {
            Log.i("Validated", "left " + r.left + " right " + r.right);
            if (r.left < leftmost) {
                leftmost = r.left;
            }
            if (r.right > rightmost) {
                rightmost = r.right;
            }
        }
        Log.i("Validated", "leftmost " + leftmost + " rightmost " + rightmost);

        if (leftmost < 0) {
            Log.i("Beyond", "Was beyond left border: " + leftmost);
            translate((int) Math.ceil(-leftmost / brickSize));
        } else if (rightmost > screenWidth) {
            Log.i("Beyond", "Was beyond right border: " + rightmost);
            translate((int) Math.ceil(-(rightmost - screenWidth) / brickSize));
        }
    }

    public Brick update() {
        if (startTime > 0) {
            long currentTime = System.currentTimeMillis();
            int oldYpos = this.brickYPosition;
            this.brickYPosition = (int) (((float) (currentTime - startTime) / (journeySeconds * 1000)) * screenHeight);
            if (wall.brickPositionAllowed(this)) {
                return this;
            } else {
                wall.addBrick(this);
                return new Brick(screenWidth, screenHeight, wall);
            }
        } else {
            startTime = System.currentTimeMillis();
            return this;
        }
    }

    public ArrayList<Rect> getCells() {
        int bsize = (int) Math.ceil(brickSize);
        int xpos = brickXPosition;
        int ypos = brickYPosition;
        ArrayList<Rect> brickCells = new ArrayList<Rect>();
        if (brickType == TYPE_FLAT_LINE) {
            if (rotation % 180 == 90) {
//                xpos += bsize;
            } else {
                xpos -= bsize;
            }
            for (int k = 0; k < 4; k++) {
                brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                if (rotation % 180 == 0) {
                    xpos += bsize;
                } else {
                    ypos += bsize;
                }
            }
        } else if (brickType == TYPE_VERT_LINE) {
            if (rotation % 180 == 0) {
//                xpos += bsize;
            } else {
                xpos -= bsize;
            }
            for (int k = 0; k < 4; k++) {
                brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                if (rotation % 180 == 0) {
                    ypos += bsize;
                } else {
                    xpos += bsize;
                }
            }
        } else if (brickType == TYPE_TEE) {
            if (rotation % 180 == 0) {
                xpos -= bsize;
            }
            if (rotation == 180) {
                ypos += bsize;
            }
            for (int k = 0; k < 4; k++) {
                if (k < 3) {
                    brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                    if (rotation % 180 == 0) {
                        xpos += bsize;
                    } else {
                        ypos += bsize;
                    }
                } else {
                    if (rotation % 180 == 0) {
                        if (rotation == 0) {
                            ypos += bsize;
                        } else {
                            ypos -= bsize;
                        }
                        xpos -= bsize * 2;
                    } else {
                        if (rotation == 90) {
                            xpos += bsize;
                        } else {
                            xpos -= bsize;
                        }
                        ypos -= bsize * 2;
                    }
                    brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                }
            }
        } else if (brickType == TYPE_L) {
            if (rotation % 180 == 90) {
                xpos -= bsize;
            }
            if (rotation == 270) {
                ypos += bsize;
            }
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
                        if (rotation == 0) {
                            xpos += bsize;
                            ypos -= bsize;
                        } else {
                            xpos -= bsize;
                            ypos -= bsize * 3;
                        }
                    } else {
                        if (rotation == 90) {
                            ypos += bsize;
                            xpos -= bsize * 3;
                        } else {
                            xpos -= bsize;
                            ypos -= bsize;
                        }
                    }
                    brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                }
            }
        } else if (brickType == TYPE_INV_L) {
            if (rotation % 180 == 90) {
                xpos -= bsize;
            }
            if (rotation == 90) {
                ypos += bsize;
            }
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
                        if (rotation == 0) {
                            xpos -= bsize;
                            ypos -= bsize;
                        } else {
                            xpos += bsize;
                            ypos -= bsize * 3;
                        }
                    } else {
                        if (rotation == 90) {
                            ypos -= bsize;
                            xpos -= bsize * 3;
                        } else {
                            xpos -= bsize;
                            ypos += bsize;
                        }
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
        } else {
            Log.e("getCells", "Unknow brick types " + brickType);
        }

        return brickCells;
    }
}
