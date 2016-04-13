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
    private int journeySeconds = 6;
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
//        brickType = TYPE_FLAT_LINE;
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
        int angle = (rotation + 90) % 360;
        if (validateRotation(angle)) {
            rotation = angle;
            validatePosition();
        }
    }

    public boolean validateRotation(int angle) {
        boolean validated = false;
        int xtranslate = 0;
        boolean tranlateLeftTried = false;
        boolean translateRightTried = false;
        for (int trials = 0; trials < 20; trials++) {
            if (tranlateLeftTried && translateRightTried) {
                break; //could not rotate even after trying translation in either side
            }
            boolean leftIntersection = false;
            boolean rightIntersection = false;
            //get the rotated image of the this brick, check if it intersects wall
            for (Rect brickCell : getCells(angle, xtranslate)) {
                if (brickCell.bottom > screenHeight) {
                    return false;//rotation will bury brick in the ground beyond screen
                }
                if (!leftIntersection && brickCell.left < brickXPosition + xtranslate) {
                    //left brick
                    if (brickCell.left < 0 || (intersectsWall(brickCell) != null)) {
                        leftIntersection = true;
                    }
                }
                if (!rightIntersection && brickCell.left > brickXPosition + xtranslate) {
                    //right brick
                    if (brickCell.right > screenWidth || (intersectsWall(brickCell) != null)) {
                        rightIntersection = true;
                    }
                }
            }
            Log.i("Rotate", "leftIntersection " + leftIntersection + " rightIntersection " + rightIntersection + " translation " + xtranslate);
            if (leftIntersection && rightIntersection) {
                break;//rotation not possible
            } else if (!(leftIntersection || rightIntersection)) {
                validated = true;//rotation possible;
                break;
            } else if (leftIntersection) {
                xtranslate += brickSize;
                translateRightTried = true;
            } else if (rightIntersection) {
                xtranslate -= brickSize;
                tranlateLeftTried = true;
            }
        }
        if (validated) {
            //rotation possible, finnally, let make sure we will ot bury the brick in the wall
            for (Rect brickCell : getCells(angle, xtranslate)) {
                if ((intersectsWall(brickCell) != null)) {
                    validated = false;
                    break;
                }
            }
        }
        if (validated) {
            brickXPosition += xtranslate;
        }
        return validated;
    }

    public void translate(float hdistance, float vdistance) {
        if (Math.abs(hdistance) > 50) {
            translate((int) (hdistance / 50));
        } else if (vdistance > 50) {
//            journeySeconds = 3;
        }
        validatePosition();
    }

    public void translate(int rows) {
        Log.i("Translating", "Was " + brickXPosition + " Add " + rows);
        for (int k = 1; k <= Math.abs(rows); k++) {
            //Translate one row at a time
            int direction = (rows / Math.abs(rows));
            brickXPosition += direction * brickSize;

            for (Rect brickRect : getCells()) {
                Rect wallIntersect = intersectsWall(brickRect);
                if (wallIntersect != null) {
                    //translated into a wall, go back one step and cancel translation
                    brickXPosition += -direction * brickSize;
                    return;
                }
            }
        }
    }

    public void validatePosition() {
        int leftmost = Integer.MAX_VALUE;
        int rightmost = Integer.MIN_VALUE;
        for (Rect r : getCells()) {
            if (r.left < leftmost) {
                leftmost = r.left;
            }
            if (r.right > rightmost) {
                rightmost = r.right;
            }
        }

        if (leftmost < 0) {
//            Log.i("Beyond", "Was beyond left border: " + leftmost);
            translate((int) Math.ceil(-leftmost / brickSize));
        } else if (rightmost > screenWidth) {
//            Log.i("Beyond", "Was beyond right border: " + rightmost);
            translate((int) Math.ceil(-(rightmost - screenWidth) / brickSize));
        }
    }

    private boolean stepDown() {
        long currentTime = System.currentTimeMillis();
        this.brickYPosition = (int) (((float) (currentTime - startTime) / (journeySeconds * 1000)) * screenHeight);
        for (Rect brickRect : getCells()) {
            if (brickRect.bottom > screenHeight) {
                setYPosition(screenHeight - (height(brickRect)));
                return false; //hit bottom wall
            }
            Rect wallIntersect = intersectsWall(brickRect);
            if (wallIntersect != null) {
                setYPosition(wallIntersect.top - (height(brickRect)));
                return false; //landed on wall
            }
        }
        return true;
    }

    private Rect intersectsWall(Rect brickRect) {
        for (Rect r : wall.getWallRects()) {
            if (Rect.intersects(r, brickRect)) {
                return r;
            }
        }
        return null;
    }

    public Brick update() {
        if (startTime > 0) {
            if (stepDown()) {
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
        return getCells(rotation, 0);
    }

    public ArrayList<Rect> getCells(int rotation, int xtranslation) {
        int bsize = (int) Math.ceil(brickSize);
        int xpos = brickXPosition + xtranslation;
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
            int vypos = ypos + bsize;
            for (int k = 0; k < 5; k++) {
                if (k < 3) {
                    brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
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
