package com.ndunda.simplebrickgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ScoresActivity extends Activity {
    private Button mNextLevelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores_activity);
        mNextLevelButton = ((Button) findViewById(R.id.next_level_button));
        mNextLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextLevel();
            }
        });

        Intent intent = getIntent();
        String message;

        int success = intent.getIntExtra("success", 1);
        int level = intent.getIntExtra("level", 1);

        if (success == 1) {
            message = "Congrats, you completed level " + level + "!";
        } else {
            message = "Sorry, you failed level " + level + "!";
        }

        ((TextView) findViewById(R.id.app_title)).setText(message);
        ((TextView) findViewById(R.id.level)).setText(Scores.getScores(ScoresActivity.this));
    }

    private void goToNextLevel() {
        Intent intent = new Intent(ScoresActivity.this, SimpleBrickGame.class);
        startActivity(intent);
        finish();
    }
}
