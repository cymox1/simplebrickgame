package com.ndunda.simplebrickgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class Scores {
    private static String DEFAULT_SCORES = "{\"scores\": []}";
    private static String PREFERENCE_KEY = "game_prefs";
    private static String SCORE_OBJ = "game";

    public static void addScores(Context context, int level, int bricksUsed, int duration) {
        String scores_json;

        try {
            JSONArray scores = getScoresFromPreferences(context);
            JSONObject score = new JSONObject();

            score.put("level", level);
            score.put("bricksUsed", bricksUsed);
            score.put("duration", duration);

            scores.put(score);

            JSONObject writer = new JSONObject();

            writer.put("scores", scores);
            writer.put("level", level);

            scores_json = writer.toString();

            SharedPreferences.Editor editor = getScoresStore(context).edit();
            editor.putString(SCORE_OBJ, scores_json);
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static String getScores(Context context) {
        ArrayList<Score> score_objs = new ArrayList<>();

        try {
            JSONArray scores = getScoresFromPreferences(context);

            for (int i = 0; i < scores.length(); i++) {
                JSONObject score = scores.getJSONObject(i);
                score_objs.add(new Score(score.getInt("level"), score.getInt("bricksUsed"), score.getInt("duration")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(score_objs);

        String score_str = "";
        for (int i = 0; i < score_objs.size(); i++) {
            Score s = score_objs.get(i);
            score_str = score_objs.size() - i + ": Level " + s.level + " - " + s.bricksUsed + " Bricks in " + s.duration + " Seconds\n" + score_str;
        }

        return score_str;
    }

    public static int getLevelFromPreferences(Context context) {
        try {
            return getScoresReader(context).getInt("level");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;

    }

    private static SharedPreferences getScoresStore(Context context) {
        return context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);

    }

    private static JSONObject getScoresReader(Context context) throws JSONException {
        return new JSONObject(getScoresStore(context).getString(SCORE_OBJ, DEFAULT_SCORES));

    }

    private static JSONArray getScoresFromPreferences(Context context) throws JSONException {
        JSONObject reader = getScoresReader(context);
        return reader.getJSONArray("scores");

    }
}

class Score implements Comparable<Score> {
    int level;
    int bricksUsed;
    int duration;

    Score(int level, int bricksUsed, int duration) {
        this.level = level;
        this.bricksUsed = bricksUsed;
        this.duration = duration;
    }

    @Override
    public int compareTo(@NonNull Score score) {
        return score.duration * score.bricksUsed - duration * bricksUsed;
    }
}
