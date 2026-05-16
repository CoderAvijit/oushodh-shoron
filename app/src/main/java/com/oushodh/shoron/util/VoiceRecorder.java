package com.oushodh.shoron.util;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class VoiceRecorder {

    private static final String TAG = "VoiceRecorder";
    private MediaRecorder recorder;
    private File outputFile;

    public File start(Context ctx, long reminderId) {
        try {
            File dir = new File(ctx.getFilesDir(), "voices");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.w(TAG, "Could not create dir");
            }
            outputFile = new File(dir, "voice_" + reminderId + "_" + System.currentTimeMillis() + ".m4a");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorder = new MediaRecorder(ctx);
            } else {
                recorder = new MediaRecorder();
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(outputFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            return outputFile;
        } catch (IOException e) {
            Log.e(TAG, "Record failed", e);
            return null;
        }
    }

    public File stop() {
        if (recorder == null) return outputFile;
        try {
            recorder.stop();
        } catch (Exception e) {
            Log.w(TAG, "Stop error", e);
        }
        try {
            recorder.reset();
            recorder.release();
        } catch (Exception ignored) {}
        recorder = null;
        return outputFile;
    }

    public void cancel() {
        if (recorder != null) {
            try { recorder.stop(); } catch (Exception ignored) {}
            try { recorder.release(); } catch (Exception ignored) {}
            recorder = null;
        }
        if (outputFile != null && outputFile.exists()) {
            outputFile.delete();
        }
    }
}
