package com.ndunda.simplebrickgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class SimpleBrickGame extends Activity {

    // gameView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    GameView gameView;
    int level;
    int winningLines = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);

        level = Scores.getLevelFromPreferences(this) + 1;
    }

    // Here is our implementation of GameView
// It is an inner class.
// Note how the final closing curly brace }
// is inside SimpleGameEngine

    // Notice we implement runnable so we have
// A thread and can override the run method.
    class GameView extends SurfaceView implements Runnable {

        // This is our thread
        Thread gameThread = null;

        // This is new. We need a SurfaceHolder
        // When we use Paint and Canvas in a thread
        // We will see it in action in the draw method soon.
        SurfaceHolder ourHolder;

        // A boolean which we will set and unset
        // when the game is running- or not.
        volatile boolean playing;

        int screenWidth;

        int screenHeight;

        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;

        Brick brick;
        Wall wall;

        int start_drag_at;
        long start_time = System.currentTimeMillis();

        private float startTouchX, startTouchY;
        static final int MIN_SWIPE_DISTANCE = 20;

        // When the we initialize (call new()) on gameView
// This special constructor method runs
        public GameView(Context context) {
            // The next line of code asks the
            // SurfaceView class to set up our object.
            // How kind.
            super(context);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
            wall = new Wall();
            brick = new Brick(screenWidth, screenHeight, wall);

            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();
        }

        @Override
        public void run() {
            while (playing) {
                // Update the frame
                update();

                // Draw the frame
                draw();

            }

        }

        // Everything that needs to be updated goes in here
// In later projects we will have dozens (arrays) of objects.
// We will also do other things like collision detection.
        public void update() {
            brick = brick.update();
            Message msg = new Message();
            boolean send_message = false;
            if (brick.gameIsOver()) {
                //We have  a new brick which can not even step down.
                msg.arg1 = 0; // failed
                send_message = true;
            } else if (wall.completedLines >= winningLines) {
                int duration = (int) (System.currentTimeMillis() - start_time) / 1000;
                Scores.addScores(getContext(), level, wall.bricks_used, duration);
                msg.arg1 = 1; // success
                send_message = true;
            }
            if (send_message) {
                playing = false;
                msg.arg2 = level;
                handler.sendMessage(msg);
            }
        }

        // Draw the newly updated scene
        public void draw() {

            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                // Make the drawing surface our canvas object
                canvas = ourHolder.lockCanvas();

                // Draw the background color
//                Drawable d = ResourcesCompat.getDrawable(getResources(), R.drawable.bricks, null);
//                d.setBounds(0, 0, screenWidth, screenHeight);
//                d.draw(canvas);
                canvas.drawColor(Color.argb(255, 255, 255, 255));

                // Choose the brush color for drawing
                paint.setColor(Color.argb(150, 0, 0, 255));
//                paint.setStyle(Paint.Style.STROKE);


                //Draw text scores
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize((int) brick.getCellSize());
                long duration = (System.currentTimeMillis() - start_time) / 1000;
                String lines[] = {"Level: " + level, "Lines: " + wall.completedLines + "/" + winningLines,
                        "Bricks used: " + wall.bricks_used, "Duration: " + duration + " Secs"};
                for (int k = 0; k < lines.length; k++) {
                    canvas.drawText(lines[k], 10, (int) brick.getCellSize() * (k + 1), paint);
                }

                // draw grid
               paint.setColor(Color.argb(20, 0, 0, 255));
                for (int x = 0; x < brick.screenWidth; x += brick.getCellSize()) {
                    // vertical lines
                    canvas.drawLine(x, 0, x, brick.screenHeight, paint);
                }

                for (int y = brick.screenHeight; y > 0; y -= brick.getCellSize()) {
                    // horizontal lines
                    canvas.drawLine(0, y, brick.screenWidth, y, paint);
                }


                //draw wall
                float radius = 15f;
                for (Cell c : wall.getWallRects()) {
                    Rect r = c.rect;
                    paint.setColor(c.color);
//                    canvas.drawRect(r.left, r.top, r.right, r.bottom, paint);
                    canvas.drawRoundRect(new RectF(r), radius, radius, paint);
                }

                //draw brick
                paint.setColor(brick.color);
                for (Rect r : brick.getCells()) {
//                    canvas.drawRect(r, paint);
                    canvas.drawRoundRect(new RectF(r), radius, radius, paint);
                }

                // Draw everything to the screen
                // and unlock the drawing surface
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        // If SimpleGameEngine Activity is paused/stopped
// shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SimpleGameEngine Activity is started theb
// start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }


        // The SurfaceView class implements onTouchListener
// So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            float hdistance = motionEvent.getX() - startTouchX;
            float vdistance = motionEvent.getY() - startTouchY;

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has removed finger from screen
                case MotionEvent.ACTION_MOVE:
                    brick.translate(hdistance, start_drag_at);
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(hdistance) < MIN_SWIPE_DISTANCE && Math.abs(vdistance) < MIN_SWIPE_DISTANCE) {
                        brick.rotate();
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    startTouchX = motionEvent.getX();
                    startTouchY = motionEvent.getY();
                    start_drag_at = brick.getBrickXPosition();
                    break;
            }

            return true;
        }


    }

    // More SimpleGameEngine methods will go here

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = new Intent(SimpleBrickGame.this, ScoresActivity.class);
            intent.putExtra("success", msg.arg1);
            intent.putExtra("level", msg.arg2);
            startActivity(intent);
            finish();
        }
    };

}