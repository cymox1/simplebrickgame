package com.ndunda.simplebrickgame;

import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by simon on 4/10/16.
 */
public class Brick {
    //Brick Types
    private static int TYPE_LINE = 1;
    private static int TYPE_TEE = 3;
    private static int TYPE_L = 4;
    private static int TYPE_INV_L = 5;
    private static int TYPE_PLUS = 6;
    private static int TYPE_STEP = 7;
    private static int TYPE_INV_STEP = 8;
    private static int ALPHA = 220;
    private static int INITIAL_Y_POS = 0;
    private static int[] BRICK_TYPES = new int[]{TYPE_LINE, TYPE_TEE, TYPE_L, TYPE_INV_L, TYPE_PLUS, TYPE_STEP, TYPE_INV_STEP};
    public static int[] COLORS = new int[]{
            Color.argb(ALPHA, 255, 20, 14),
            Color.argb(ALPHA, 0, 229, 238),
            Color.argb(ALPHA, 0, 238, 118),
            Color.argb(ALPHA, 255, 153, 18),
            Color.argb(ALPHA, 255, 0, 0),
            Color.argb(ALPHA, 191, 62, 255),
            Color.argb(ALPHA, 127, 255, 0)
    };
    public static int ROW_COUNT = 20;

    private int brickYPosition;
    private double cellSize;
    private int brickXPosition;
    private int journeySeconds = 6;
    public int screenWidth;
    public int screenHeight;
    private Wall wall;
    private long startTime;
    private int brickType;
    private int rotation;
    public int color;


    public Brick(int screenWidth, int screenHeight, Wall wall) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.wall = wall;
        cellSize = (float) (1 * screenWidth) / ROW_COUNT;
        float gap = (float) (0 * screenWidth) / (ROW_COUNT - 1);
        int brickRow = ROW_COUNT / 2;
        int brick_index = (int) (Math.random() * BRICK_TYPES.length);
        brickType = BRICK_TYPES[brick_index];
        color = COLORS[brick_index];
        rotation = (int) (Math.random() * 4) * 90;
//        brickType = TYPE_LINE;
//        rotation = 90;
        brickXPosition = (int) Math.ceil(brickRow * (cellSize + gap));
        brickYPosition = INITIAL_Y_POS;
    }

    public void setYPosition(int yposition) {
        brickYPosition = yposition;
    }

    public int getBrickXPosition() {
        return brickXPosition;
    }

    public double getCellSize() {
        return cellSize;
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
            if (leftIntersection && rightIntersection) {
                break;//rotation not possible
            } else if (!(leftIntersection || rightIntersection)) {
                validated = true;//rotation possible;
                break;
            } else if (leftIntersection) {
                xtranslate += cellSize;
                translateRightTried = true;
            } else if (rightIntersection) {
                xtranslate -= cellSize;
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

    public void translate(float hdistance, float starting_position) {
        float move_distance = (starting_position + hdistance - brickXPosition);
        do_translate((int) (move_distance / cellSize));
        validatePosition();
    }

    public void do_translate(int rows) {
        for (int k = 1; k <= Math.abs(rows); k++) {
            //Translate one row at a time
            int direction = (rows / Math.abs(rows));
            brickXPosition += direction * cellSize;

            for (Rect brickRect : getCells()) {
                Rect wallIntersect = intersectsWall(brickRect);
                if (wallIntersect != null) {
                    //translated into a wall, go back one step and cancel translation
                    brickXPosition += -direction * cellSize;
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
            do_translate((int) Math.ceil(-leftmost / cellSize));
        } else if (rightmost > screenWidth) {
            do_translate((int) Math.ceil(-(rightmost - screenWidth) / cellSize));
        }
    }

    public boolean stepDown() {
        long currentTime = System.currentTimeMillis();
        int lastYPosition = brickYPosition;
        this.brickYPosition = (int) (((float) (currentTime - startTime) / (journeySeconds * 1000)) * screenHeight);
        if (brickYPosition == lastYPosition) {
            return true;
        }
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

    public boolean gameIsOver() {
        return !stepDown() && brickYPosition < INITIAL_Y_POS;
    }

    private Rect intersectsWall(Rect brickRect) {
        for (Cell c : wall.getWallRects()) {
            if (Rect.intersects(c.rect, brickRect)) {
                return c.rect;
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
        int bsize = (int) Math.ceil(cellSize);
        int xpos = brickXPosition + xtranslation;
        int ypos = brickYPosition;
        ArrayList<Rect> brickCells = new ArrayList<Rect>();
        if (brickType == TYPE_LINE) {
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
        } else if (brickType == TYPE_STEP) {
            if (rotation % 180 == 90) {
                ypos += bsize;
            }
            for (int k = 0; k < 4; k++) {
                brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                if (k == 1) {
                    if (rotation % 180 == 0) {
                        xpos += bsize;
                    } else {
                        ypos -= bsize;
                    }
                } else {
                    if (rotation % 180 == 0) {
                        ypos += bsize;
                    } else {
                        xpos += bsize;
                    }
                }
            }
        } else if (brickType == TYPE_INV_STEP) {
            if (rotation % 180 == 0) {
                xpos += bsize;
            }
            for (int k = 0; k < 4; k++) {
                brickCells.add(new Rect(xpos, ypos, xpos + bsize, ypos + bsize));
                if (k == 1) {
                    if (rotation % 180 == 0) {
                        xpos -= bsize;
                    } else {
                        ypos += bsize;
                    }
                } else {
                    if (rotation % 180 == 0) {
                        ypos += bsize;
                    } else {
                        xpos += bsize;
                    }
                }
            }
        } else {
            Log.e("getCells", "Unknown brick types " + brickType);
        }

        return brickCells;
    }
}
