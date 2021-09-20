/*
 * Copyright (C) 2008,2009  OMRON SOFTWARE Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.softwaretest;

import static com.example.softwaretest.BaseApplication.context;
import static com.example.softwaretest.BaseApplication.copyFile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableStringBuilder;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.CandidatesViewManager;
import com.googlecode.openwnn.legacy.ComposingText;
import com.googlecode.openwnn.legacy.DefaultSoftKeyboard;
import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.OpenWnn;
import com.googlecode.openwnn.legacy.SymbolList;
import com.googlecode.openwnn.legacy.TextCandidatesViewManager;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.ZH.CN.TutorialZHCN;
import com.googlecode.openwnn.legacy.ZH.DefaultSoftKeyboardZH;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;

import java.util.ArrayList;


public class InputService extends InputMethodService implements OnCandidateSelected, OnHandWritingRecognize, View.OnClickListener {


    /**
     * IME's status for {@code mStatus} input/no candidates).
     */
    private static final int STATUS_INIT = 0x0000;
    private final ComposingText mComposingText;
    private final DefaultSoftKeyboardZH mInputViewManager;

    /**
     * IME's status
     */
    protected int mStatus = STATUS_INIT;

    /**
     * Whether exact match searching or not
     */
    protected boolean mExactMatchMode = false;

    /**
     * Spannable string builder for displaying the composing text
     */
    protected SpannableStringBuilder mDisplayText;

    /**
     * Instance of this service
     */
    private static InputService mSelf = null;


    /**
     * Conversion engine for listing symbols
     */
    private SymbolList mConverterSymbolEngineBack;


    /**
     * Tutorial
     */
    private TutorialZHCN mTutorial;
    private CandidatesViewManager mCandidatesViewManager;
    private Object mInputConnection;

    /**
     * Constructor
     */
    public InputService() {
        super();
        mSelf = this;
        mComposingText = new ComposingText();
        mCandidatesViewManager = new TextCandidatesViewManager(-1);
        mInputViewManager = new DefaultSoftKeyboardZH();
        try {
            copyFile(context, "writableZHCN.dic", false);
            copyFile(context, "writableZHCN.dic-journal", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDisplayText = new SpannableStringBuilder();

    }

    /**
     * Constructor
     *
     * @param context The context
     */
    public InputService(Context context) {
        this();
        attachBaseContext(context);
    }

    /**
     * @see OpenWnn#onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: ");

    }

    /**
     * @see OpenWnn#onCreateInputView
     */
    @Override
    public View onCreateInputView() {


        iconFloatView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.input_view_windows, null);

        iconFloatView.clearAnimation();
        iconFloatView.setAlpha(0);
        iconFloatView.setVisibility(View.VISIBLE);
        iconFloatView.animate().alpha(1).setDuration(500)
                .start();
        isAddView = true;
        load();


        return iconFloatView;
    }

    /**
     * @see OpenWnn#onStartInputView
     */
    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {

        super.onStartInputView(attribute, restarting);

        /* initialize views */
        mCandidatesViewManager.clearCandidates();

//		mLayoutParams = (WindowManager.LayoutParams) iconFloatView.getLayoutParams();
//		mLayoutParams.height = 600;

    }

    /**
     * @see OpenWnn#onEvaluateFullscreenMode
     */
    @Override
    public boolean onEvaluateFullscreenMode() {
        /* never use full-screen mode */
        return false;
    }

    /**
     * @see OpenWnn#onEvaluateInputViewShown
     */
    @Override
    public boolean onEvaluateInputViewShown() {
        super.onEvaluateInputViewShown();
        return true;
    }

    /**
     * Get the instance of this service. <br>
     * Before using this method, the constructor of this service must be
     * invoked.
     *
     * @return The instance of this service
     */
    public static InputService getInstance() {
        return mSelf;
    }


    /**
     * @see OpenWnn#onFinishInput
     */
    @Override
    public void onFinishInput() {
        if (mInputConnection != null) {

        }
        super.onFinishInput();
    }


    private LinearLayout iconFloatView;
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
    int back_color = Color.LTGRAY, text_color = Color.BLACK, candidate_text_color = Color.DKGRAY;

    RelativeLayout.LayoutParams candidateViewLp;

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
            if (candidate_view_height > 0)
                candidateViewLp.height = candidate_view_height;
            candidateContainer.addView(mCandidateView, candidateViewLp);
            System.currentTimeMillis();

            mCandidateView.clear();

            mCandidateView.setOnCandidateSelected(this);
            handWritingBoard.setOnHandWritingRecognize(this);

            iconFloatView.findViewById(R.id.clean).setOnClickListener(this);
            iconFloatView.findViewById(R.id.exit).setVisibility(View.GONE);
            iconFloatView.findViewById(R.id.delete).setOnClickListener(this);

            iconFloatView.findViewById(R.id.btn_group).setPadding(0, 150, 5, 150);

        } else {
            if (candidate_view_height > 0)
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

            case R.id.delete:
                del();
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
        final @Nullable InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(text, 1);
        }
    }

    private void del(){

        final InputConnection ic = getCurrentInputConnection();
        if(ic==null)
            return;

        ic.deleteSurroundingText(1, 0);
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
