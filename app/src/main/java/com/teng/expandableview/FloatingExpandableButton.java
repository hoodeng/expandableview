package com.teng.expandableview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teng.expandableviewdemo.R;


public class FloatingExpandableButton extends LinearLayout {

    private View mExpandableView;
    private TextView mCountNumTv;
    private boolean mExpanding = true;
    private boolean mRelayout = true;
    private int mCollapsedWidth;

    private ValueAnimator mAlphaAnimator;

    private ValueAnimator.AnimatorUpdateListener mListener = new ValueAnimator
            .AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            LayoutParams layoutParams = (LayoutParams) mExpandableView.getLayoutParams();
            Float value = (Float) animation.getAnimatedValue();
            layoutParams.width = Math.round(value.floatValue());
            mExpandableView.setLayoutParams(layoutParams);

            float alpha = value / mCollapsedWidth;
            mCountNumTv.setAlpha(alpha);
        }
    };

    public FloatingExpandableButton(Context context) {
        super(context);
        init();
    }

    public FloatingExpandableButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingExpandableButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mRelayout || getVisibility() == View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        mRelayout = false;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mCollapsedWidth = getRealTextViewWidth(mCountNumTv);
        Log.v("ExpandableButton", "mCollapsedWidth --> " + mCollapsedWidth);
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_expandable_text, this);
        mExpandableView = findViewById(R.id.expandable_layout);
        mCountNumTv = findViewById(R.id.count_num_tv);
    }

    private static int getRealTextViewWidth(@NonNull TextView textView) {
        TextPaint paint = textView.getPaint();
        int padding;
        int margin = 0;
        ViewGroup.LayoutParams lp = textView.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            margin = ((MarginLayoutParams) lp).leftMargin + ((MarginLayoutParams) lp).rightMargin;
        }
        padding = textView.getPaddingLeft() + textView.getPaddingRight();
        int w = Math.round(paint.measureText(textView.getText().toString())) + margin + padding;
        return w;
    }


    public void setText(String text) {
        mRelayout = true;

        mCountNumTv.setText(text);
        resetExpandableView();

        boolean willCollapsed = !TextUtils.isEmpty(text);

        if (willCollapsed) {
            this.postExpanding();
        } else {
            this.postClosing();
        }
    }

    private void resetExpandableView() {
        LayoutParams layoutParams = (LayoutParams) mExpandableView.getLayoutParams();
        layoutParams.width = 0;
        mExpandableView.setLayoutParams(layoutParams);
    }

    private void postExpanding() {
        post(() -> expand());
    }

    private void postClosing() {
        post(() -> close());
    }

    public void close() {
        ValueAnimator animator = ValueAnimator.ofFloat(mCollapsedWidth, 0);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mExpanding = false;
            }
        });
        animator.addUpdateListener(mListener);
        animator.start();
    }

    public void expand() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, mCollapsedWidth);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mExpanding = true;
            }
        });
        animator.addUpdateListener(mListener);
        animator.start();
    }

    public boolean isExpanding() {
        return mExpanding;
    }

    public void dismissWithAnimator() {
        float alpha = getAlpha();
        if (mAlphaAnimator != null) {
            mAlphaAnimator.cancel();
        }
        mAlphaAnimator = ValueAnimator.ofFloat(alpha, 0);
        mAlphaAnimator.setInterpolator(new LinearInterpolator());
        mAlphaAnimator.setDuration(100);
        mAlphaAnimator.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            setAlpha(value);
        });
        mAlphaAnimator.start();
    }

    public void displayWithAnimator() {
        if (mAlphaAnimator != null) {
            mAlphaAnimator.cancel();
        }
        mAlphaAnimator = ValueAnimator.ofFloat(0, 1);
        mAlphaAnimator.setInterpolator(new LinearInterpolator());
        mAlphaAnimator.setDuration(1000);
        mAlphaAnimator.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            setAlpha(value);
        });
        mAlphaAnimator.start();
    }
}
