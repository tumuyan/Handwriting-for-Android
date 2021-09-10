package com.example.softwaretest;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnPinyinQueryed;
import com.googlecode.openwnn.legacy.CLOUDSONG.PinyinQueryResult;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;

@SuppressLint("NewApi")
public class InputViewActivity extends Activity implements OnCandidateSelected, OnHandWritingRecognize, OnPinyinQueryed, OnClickListener {

	HandWritingBoardLayout handWritingBoard;
	TextView inputShow;

	LinearLayout container;
	RelativeLayout candidateContainer;
	Button mShowMore;
	Button btnCleanHandWriting;
	TextView PinYinInput;

	CandidateView mCandidateView;

	private StringBuilder currentInput = new StringBuilder();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_view);


		container = (LinearLayout) findViewById(R.id.container);
		candidateContainer = (RelativeLayout) findViewById(R.id.candidateContainer);
		handWritingBoard = (HandWritingBoardLayout) findViewById(R.id.handwrtingboard);


		btnCleanHandWriting = (Button) findViewById(R.id.clean);
		PinYinInput = (TextView) findViewById(R.id.pinyinInput);
		inputShow = (TextView) findViewById(R.id.candidateselected);

//		ckManager = new CloudKeyboardInputManager();
//		ckManager.setOnPinyinQueryed(this);
		mCandidateView = new CandidateView(this);
		mCandidateView.setOnCandidateSelected(this);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.LEFT_OF, R.id.btn_showMore);

		lp1.width = ViewGroup.LayoutParams.MATCH_PARENT;
		candidateContainer.addView(mCandidateView, lp1);
		System.currentTimeMillis();
		handWritingBoard.setOnHandWritingRecognize(this);


		btnCleanHandWriting.setOnClickListener(this);

		handWritingBoard.setVisibility(View.VISIBLE);
//		ckManager.delAll();
		PinYinInput.setText("");
		mCandidateView.clear();

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
		if (isHandWriting()) {
			resetHandWritingRecognize();
		} else {
//			ckManager.candidateSelected(wnnWord);
		}
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
}
