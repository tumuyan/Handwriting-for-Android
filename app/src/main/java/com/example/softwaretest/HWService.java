package com.example.softwaretest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;


import java.util.ArrayList;


public final class HWService extends Service implements OnCandidateSelected, OnHandWritingRecognize, View.OnClickListener {

    private LinearLayout iconFloatView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private boolean isAddView;

    private static final String TAG = "HWService";

    HandWritingBoardLayout handWritingBoard;
    TextView inputShow;

    LinearLayout container;
    RelativeLayout candidateContainer;

    CandidateView mCandidateView;

    private StringBuilder currentInput = new StringBuilder();

    private Rsa rsa;
    private boolean notsafe;

    private int height = 0, candidate_view_height;

    private boolean inited = false;
    private int back_color, text_color, candidate_text_color;

    RelativeLayout.LayoutParams candidateViewLp;

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) (getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        iconFloatView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.input_view_windows, null);
        Log.d(TAG, "onCreate: ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(null);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent != null) {
            rsa = new Rsa(intent.getSerializableExtra("key"));
            height = intent.getIntExtra("height", -1);
            back_color = intent.getIntExtra("back_color", Color.GRAY);
            text_color = intent.getIntExtra("text_color", Color.BLACK);
            candidate_text_color = intent.getIntExtra("candidate_text_color", Color.DKGRAY);
            candidate_view_height = intent.getIntExtra("candidate_view_height", -1);

            Log.i("intent", "height=" + height + " back_color=" + back_color + " text_color=" + text_color + " candiate_text_color=" + candidate_text_color);

            if (height >= 0) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (rsa.notSafe() != notsafe) {
                        notsafe = rsa.notSafe();
                        if (notsafe)
                            createNotificationChannel("Not safe! Start Service without RSA Key.");
                        else
                            createNotificationChannel(null);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getApplicationContext())) {
                        //启动Activity让用户授权
                        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                    } else {
                        removeView();

                        initLayoutParams();
                    }
                }

//                Toast.makeText(this, "update " + height, Toast.LENGTH_SHORT).show();
                addView();

                mLayoutParams = (WindowManager.LayoutParams) iconFloatView.getLayoutParams();
                mLayoutParams.height = height;

            } else {
                removeView();
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        removeView();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private synchronized void addView() {
        if (!isAddView) {


            iconFloatView.clearAnimation();
            iconFloatView.setAlpha(0);
            iconFloatView.setVisibility(View.VISIBLE);
            iconFloatView.animate().alpha(1).setDuration(500)
                    .start();
            mWindowManager.addView(iconFloatView, mLayoutParams);
            isAddView = true;
            load();
        }
    }

    private synchronized void removeView() {
        if (isAddView) {
            mWindowManager.removeView(iconFloatView);
            isAddView = false;
        }
    }


    /**
     * desc：初始化 mLayoutParams 设置其各字段的值
     */
    private void initLayoutParams() {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);

        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.gravity = Gravity.BOTTOM | Gravity.END;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.height = height;
        mLayoutParams.y = 0;
    }


    private void createNotificationChannel(String content) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 通知渠道的id
        String id = "my_channel_01";
        // 用户可以看到的通知渠道的名字.
        CharSequence name = getString(R.string.channel_name);
//         用户可以看到的通知渠道的描述
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
//         配置通知渠道的属性
        mChannel.setDescription(description);
//         设置通知出现时的闪灯（如果 android 设备支持的话）
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
//         设置通知出现时的震动（如果 android 设备支持的话）
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//         最后在notificationmanager中创建该通知渠道 //
        mNotificationManager.createNotificationChannel(mChannel);
        // 通知渠道的id
        String CHANNEL_ID = "my_channel_01";
        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(this)
                .setContentTitle(name).setContentText(content == null ? description : content)
                .setSmallIcon(R.drawable.ic_launcher)
                .setChannelId(CHANNEL_ID)
                .build();
        startForeground(1, notification);
    }


    private void load() {
        if (!inited) {
            inited = true;
            container = iconFloatView.findViewById(R.id.container);
            candidateContainer = iconFloatView.findViewById(R.id.candidateContainer);
            handWritingBoard = iconFloatView.findViewById(R.id.handwrtingboard);
            inputShow = iconFloatView.findViewById(R.id.candidateselected);

            mCandidateView = new CandidateView(getApplicationContext());

            candidateViewLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            candidateViewLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            if(candidate_view_height>0)
            candidateViewLp.height = candidate_view_height;
            candidateContainer.addView(mCandidateView, candidateViewLp);
            System.currentTimeMillis();

            mCandidateView.clear();

            mCandidateView.setOnCandidateSelected(this);
            handWritingBoard.setOnHandWritingRecognize(this);

            iconFloatView.findViewById(R.id.clean).setOnClickListener(this);
            iconFloatView.findViewById(R.id.exit).setOnClickListener(this);
            iconFloatView.findViewById(R.id.delete).setOnClickListener(this);

        }else{
            if(candidate_view_height>0)
                candidateViewLp.height = candidate_view_height;
        }
        iconFloatView.setBackgroundColor(back_color);

        mCandidateView.setmColorNormal(text_color);
//        Log.i("back_color b",Integer.toHexString(back_color) +(back_color> - 0x33000000 && back_color < 0x33000000) );
        if (back_color >= 0 && back_color < 0x44000000)
            mCandidateView.setBackgroundColor(calBackColor(text_color));
        else
            mCandidateView.setBackgroundColor(back_color);
        mCandidateView.setmColorRecommended(candidate_text_color);
        mCandidateView.setmColorOther(candidate_text_color);
//        mCandidateView.invalidate();
/*
        int h = mCandidateView.getHeight();
        Log.i("candidate", "height=" + h + " candidate_view_height="+candidate_view_height);
        if (candidate_view_height > 0) {
            int clear_height = h - mCandidateView.getPaddingBottom() - mCandidateView.getPaddingTop();
            int padding = (candidate_view_height - clear_height) / 2;
            if (padding >= 0)
                mCandidateView.setPadding(mCandidateView.getPaddingLeft(), padding, mCandidateView.getPaddingRight(), padding);
            Log.i("candidate", "height2=" + mCandidateView.getHeight() +" padding="+padding);

        }*/
    }


    private int calBackColor(int color) {
        Color f = Color.valueOf(color);
        Log.i("calBackColor", Integer.toHexString(color) + " " + f.luminance());
        if (f.luminance() < 0.3)
            return Color.LTGRAY;
        return Color.DKGRAY;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.clean:
                resetHandWritingRecognizeClicked();
                break;
            case R.id.exit:
                removeView();
                break;
            case R.id.delete:
                commit("keycode:KEYCODE_FORWARD_DEL");
                if (currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    inputShow.setText(currentInput.toString());
                }
                break;
        }
    }


    void resetHandWritingRecognizeClicked() {
        resetHandWritingRecognize();
        mCandidateView.clear();
    }

    @Override
    public void candidateSelected(WnnWord wnnWord) {
        String candidate = null;
        if (wnnWord != null) {
            candidate = wnnWord.candidate;
        }
        if (!TextUtils.isEmpty(candidate)) {
            appendCandidate(candidate);
            inputShow.setText(currentInput.toString());
            commit(candidate);
        }
        mCandidateView.clear();
        resetHandWritingRecognize();
    }

    private void commit(String text) {
        if (rsa == null)
            return;
        Intent intent = new Intent();
        try {
            intent.setAction("com.osfans.trime.commit");
            intent.putExtra("text", rsa.encode(text));
            intent.putExtra("ext_app", getPackageName());
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void appendCandidate(String candidate) {
        currentInput.append(candidate);
    }

    @Override
    public void handWritingRecognized(ArrayList<WnnWord> result) {
        mCandidateView.setSuggestions(result, false, false);
    }

    // TODO 整理一下

    private void resetHandWritingRecognize() {
        handWritingBoard.reset_recognize();
    }


}

