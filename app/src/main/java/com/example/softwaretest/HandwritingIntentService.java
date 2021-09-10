package com.example.softwaretest;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.permission.PermissionUtils;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HandwritingIntentService extends IntentService  implements OnCandidateSelected, OnHandWritingRecognize, View.OnClickListener{

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.softwaretest.action.FOO";
    private static final String ACTION_BAZ = "com.example.softwaretest.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.softwaretest.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.softwaretest.extra.PARAM2";


    private View windows;
    HandWritingBoardLayout handWritingBoard;
    TextView inputShow;

    LinearLayout container;
    RelativeLayout candidateContainer;

    Button btnCleanHandWriting;

    CandidateView mCandidateView;

    private StringBuilder currentInput = new StringBuilder();

    private String token, ime_app, ime_cls;

    private boolean inited = false;



    public HandwritingIntentService() {
        super("HandwritingIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, HandwritingIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, HandwritingIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            } else{

                token = intent.getStringExtra("token");
                ime_app = intent.getComponent().getPackageName();
                ime_cls = intent.getComponent().getClassName();

                Log.w("onCreate ","intent.token="+token+" app="+ime_app + " cls="+ime_cls );
                PermissionUtils.checkPermission(this);

                load();

            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void load(){
        if(inited) return;
        inited = true;

        EasyFloat.with(getApplicationContext()).
                setLayout(R.layout.input_view_windows)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setGravity(Gravity.BOTTOM)
                .setMatchParent(true,false)
//                .setDragEnable(false)
                .setBorder(100,0,100,0)
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void touchEvent(View view, MotionEvent motionEvent) {

                    }

                    @Override
                    public void show(View view) {

                    }

                    @Override
                    public void hide(View view) {

                    }

                    @Override
                    public void dragEnd(View view) {

                    }

                    @Override
                    public void drag(View view, MotionEvent motionEvent) {

                    }

                    @Override
                    public void dismiss() {

                    }

                    @Override
                    public void createdResult(boolean b, String s, View view) {

                        windows = view;
                        container = windows. findViewById(R.id.container);
                        candidateContainer = windows.findViewById(R.id.candidateContainer);
                        handWritingBoard = windows. findViewById(R.id.handwrtingboard);

                        btnCleanHandWriting =windows. findViewById(R.id.clean);

                        inputShow = windows. findViewById(R.id.candidateselected);

                        mCandidateView = new CandidateView(getApplicationContext());

                        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp1.addRule(RelativeLayout.LEFT_OF, R.id.btn_showMore);

                        lp1.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        candidateContainer.addView(mCandidateView, lp1);
                        System.currentTimeMillis();

                        init();
                        mCandidateView.clear();
                    }
                })
                .show();
    }

    public void init(){
        mCandidateView.setOnCandidateSelected(this);
        handWritingBoard.setOnHandWritingRecognize(this);
        btnCleanHandWriting.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.clean:
                resetHandWritingRecognizeClicked();
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

            Intent intent = new Intent();
            try {
//                intent.setComponent(new ComponentName(ime_app,ime_cls));
                intent.setAction("com.osfans.trime.commit");
                intent.putExtra("text",candidate);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // mOpenWnnZHCN.commitTextSelected(wnnWord);
        mCandidateView.clear();
        if (isHandWriting()) {
            resetHandWritingRecognize();
        } else {
//			ckManager.candidateSelected(wnnWord);
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

    /*
     * 删除和上屏都要区分手写和字母输入；
     */
    private boolean isHandWriting() {
        return handWritingBoard.getVisibility() == View.VISIBLE;
    }


}