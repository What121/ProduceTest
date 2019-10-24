package com.bestom.producetest.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.bestom.producetest.R;

import java.util.Map;
import java.util.TreeMap;

public class MusicPlayer {
    private Context mContext;
    private static MusicPlayer sInstance;

    public static class Type {
        public final static int MUSIC_CLICK = 1;
        public final static int MUSIC_FOCUSED = 2;
    }

    private SoundPool mSp;
    private Map sSpMap;

    private MusicPlayer(Context context) {
        mContext = context;
        sSpMap = new TreeMap();
        mSp = new SoundPool(5, AudioManager.STREAM_MUSIC, 100);
        sSpMap.put(Type.MUSIC_CLICK, mSp.load(mContext, R.raw.music, 1));
        sSpMap.put(Type.MUSIC_FOCUSED, mSp.load(mContext, R.raw.music, 1));
    }

    public static MusicPlayer getInstance(Context context) {
        if (sInstance == null)
            sInstance = new MusicPlayer(context);
        return sInstance;
    }

    public void play(int type) {
        if (sSpMap.get(type) == null) return;
        mSp.play((Integer) sSpMap.get(type), 1, 1, 0, 0, 1);
    }
}
