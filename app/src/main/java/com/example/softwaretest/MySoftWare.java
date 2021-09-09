package com.example.softwaretest;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.CloudKeyboardInputManager;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnPinyinQueryed;
import com.googlecode.openwnn.legacy.CLOUDSONG.PinyinQueryResult;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;

public class MySoftWare extends LinearLayout implements OnCandidateSelected, OnHandWritingRecognize, OnPinyinQueryed, OnClickListener {

	private final Context context;
	private HandWritingBoardLayout handWritingBoard;
	private TextView inputShow;
	private ViewGroup candidateContainer;
	private Button  btnCleanHandWriting;
	private TextView PinYinInput;
	private CandidateView mCandidateView;
	private StringBuilder currentInput = new StringBuilder();


	public MySoftWare(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context =context;

		initView_Method();
	}

	public MySoftWare(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		initView_Method();
	}

	public MySoftWare(Context context){
		super(context);
		this.context =context;

		initView_Method();
	}

	public void initView_Method() {
		LayoutInflater inflate =
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view =inflate.inflate(R.layout.input_view,(ViewGroup) null);
		findViewById(view);
		mCandidateView = new CandidateView(context);
		mCandidateView.setOnCandidateSelected(this);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.LEFT_OF, R.id.btn_showMore);

		lp1.width = ViewGroup.LayoutParams.MATCH_PARENT;
		candidateContainer.addView(mCandidateView, lp1);
		System.currentTimeMillis();
		handWritingBoard.setVisibility(View.VISIBLE);
		PinYinInput.setText("");
		mCandidateView.clear();
		handWritingBoard.setOnHandWritingRecognize(this);
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
			cleanCurrentPinyinView();
			appendCandidate(candidate);
			inputShow.setText(currentInput.toString());
		}
		// mOpenWnnZHCN.commitTextSelected(wnnWord);
		mCandidateView.clear();
			resetHandWritingRecognize();
	}

	private void cleanCurrentPinyinView() {
		PinYinInput.setText("");
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

	@Override
	public void onPinyinQueryed(PinyinQueryResult pyQueryResult) {
		if (pyQueryResult != null) {
			mCandidateView.setSuggestions(pyQueryResult.getCandidateList(), false, false);
			String pinyin = pyQueryResult.getCurrentInput();
			updatePinyin(pinyin);
		}
	}

	private void updatePinyin(String pinyin) {
		PinYinInput.setText(pinyin);
	}

	private void findViewById(View view) {

		candidateContainer =  view.findViewById(R.id.candidateContainer);
		handWritingBoard = (HandWritingBoardLayout) view.findViewById(R.id.handwrtingboard);
		btnCleanHandWriting = (Button) view.findViewById(R.id.clean);
		PinYinInput = (TextView) view.findViewById(R.id.pinyinInput);
		inputShow = (TextView) view.findViewById(R.id.candidateselected);
		btnCleanHandWriting.setOnClickListener(this);
		btnCleanHandWriting.setOnClickListener(this);

	}

}
