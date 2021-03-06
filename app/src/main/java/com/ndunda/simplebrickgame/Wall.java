package com.ndunda.simplebrickgame;

import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 4/10/16.
 */
public class Wall {
    private ArrayList<Cell> wallrects = new ArrayList<Cell>();
    public int completedLines = 0;
    public int bricks_used = 0;

    public void addBrick(Brick brick) {
        for (Rect brickRect : brick.getCells()) {
            wallrects.add(new Cell(brickRect, brick.color));
        }
        checkCompleteLines(brick.screenHeight, (int) Math.ceil(brick.getCellSize()));
        bricks_used += 1;
    }

    public ArrayList<Cell> getWallRects() {
        return wallrects;
    }

    private void checkCompleteLines(int screenHeight, int cellSize) {
        int rowCount = (int) (screenHeight / cellSize);
        List<List<Cell>> lines = new ArrayList<>(rowCount);
        for (int k = 0; k < rowCount; k++) {
            lines.add(k, new ArrayList<Cell>());
        }
        for (Cell r : wallrects) {
            int rowNo = r.rect.top / cellSize;
            if (rowNo > 0 && rowNo < lines.size()) {
                lines.get(r.rect.top / cellSize).add(r);
            }
        }
        for (int k = lines.size() - 1; k >= 0; k--) {
            if (lines.get(k).size() == Brick.ROW_COUNT) {
                wallrects.removeAll(lines.get(k));
                for (int l = k - 1; l >= 0; l--) {
                    for (Cell c : lines.get(l)) {
                        Rect r = c.rect;
                        r.set(r.left, r.top + cellSize, r.right, r.bottom + cellSize);
                    }
                }
                completedLines += 1;
            }

        }

    }
}
