package com.example.softwaretest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnPinyinQueryed;
import com.googlecode.openwnn.legacy.CLOUDSONG.PinyinQueryResult;
import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.permission.PermissionUtils;

import java.util.ArrayList;

public class LoaderActivity extends Activity  implements OnCandidateSelected, OnHandWritingRecognize, View.OnClickListener {
    private View windows;
    HandWritingBoardLayout handWritingBoard;
    TextView inputShow;

    LinearLayout container;
    RelativeLayout candidateContainer;

    Button btnCleanHandWriting;

    CandidateView mCandidateView;

    private StringBuilder currentInput = new StringBuilder();

    private String token, ime_app, ime_cls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        ime_app = intent.getComponent().getPackageName();
        ime_cls = intent.getComponent().getClassName();
        Log.w("onCreate ","intent.token="+token+" app="+ime_app + " cls="+ime_cls );

//        setContentView(R.layout.activity_loader);
// 权限检测
        PermissionUtils.checkPermission(this);

        EasyFloat.with(this).
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

//        this.onPause();

    }

    public void init(){
        mCandidateView.setOnCandidateSelected(this);
        handWritingBoard.setOnHandWritingRecognize(this);


        btnCleanHandWriting.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
//        EasyFloat.dismiss(null,true);
        super.onDestroy();
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
                intent.setComponent(new ComponentName(ime_app,ime_cls));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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