package com.yang.viewpager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yang.viewpager.R;

import java.util.List;

/**
 * Created by YangHaiPing on 2016/3/21.
 */
public class Indicator extends HorizontalScrollView implements ViewTreeObserver.OnGlobalLayoutListener {
    private Paint mPaint;
    //导航栏
    private LinearLayout mContainer;
    //路径(用于画三角形)
    private Path mPath;
    //三角形的宽
    private int mTriangleWidth;
    //三角形的高
    private int mTriangleHeight;
    //默认的指示器与导航栏模块的比例值
    public static final float RADIO_TRIANGLE_WIDTH = 1 / 6F;
    //导航栏中view的个数
    private int mChildCount;
    //一开始显示的偏移量
    private int mInitTranslationX;
    //屏幕宽度
    private int mScreenWidth;
    //移动的水平距离
    private int mTranslationX;
    //可见导航栏模块的数量
    private int mTabVisibleCount;
    //导航栏模块的宽度
    private int mTabWidth;
    //默认的可见导航栏的数量
    private static final int DEFAULT_VISIBLE_COUNT = 3;
    //根据xml文件定义的导航栏模块可见数量与实际中子View的数量比较,选取最小值作为最终导航栏模块的数量
    private int mFinalVisibleCount = -1;
    //如果导航栏模块实际数量大于可见导航栏的模块数量,则需要做滚动处理使其可见
    private boolean isModelNeedScroll;
    //从中间位置开始滚动
    private int mMiddlePosition;
    private boolean isOnce = false;
    private Context mContext;

    public Indicator(Context context) {
        this(context, null);
    }

    public Indicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setHorizontalScrollBarEnabled(false);
        initPaint();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Indicator);
        mTabVisibleCount = typedArray.getInt(R.styleable.Indicator_visible_tab_count, DEFAULT_VISIBLE_COUNT);
        if (mTabVisibleCount <= 0 || mTabVisibleCount >= Integer.MAX_VALUE >> 2) {
            mTabVisibleCount = DEFAULT_VISIBLE_COUNT;
        }
        typedArray.recycle();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Indicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setHorizontalScrollBarEnabled(false);
        initPaint();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Indicator);
        mTabVisibleCount = typedArray.getInt(R.styleable.Indicator_visible_tab_count, DEFAULT_VISIBLE_COUNT);
        if (mTabVisibleCount <= 0 || mTabVisibleCount >= Integer.MAX_VALUE >> 2) {
            mTabVisibleCount = DEFAULT_VISIBLE_COUNT;
        }
        typedArray.recycle();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        //为了三角形绘制的时候角不要太尖锐，设置圆角
        mPaint.setPathEffect(new CornerPathEffect(3));
    }

    private void initTriangle() {
        mTriangleHeight = mTriangleWidth / 3;
        mPath = new Path();
        //移动到左下角
        mPath.moveTo(0, 0);
        //移动到右下角
        mPath.lineTo(mTriangleWidth, 0);
        //移动到顶点
        mPath.lineTo(mTriangleWidth / 2, -mTriangleHeight);
        //闭合
        mPath.close();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        canvas.translate(mInitTranslationX + mTranslationX, getHeight());
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    /**
     * 指示器跟着ViewPager滚动
     *
     * @param position
     * @param offset
     */
    public void scroll(int position, float offset) {//offset 0~1
        if (mChildCount != 0) {
            //指示器偏移位置随位置变化而变化
            mTranslationX = (int) (mTabWidth * (offset + position));
            //导航栏文字则需要再判断
            if (mFinalVisibleCount > 1) {
                //当指示器移动到当前导航栏未滚动时的中间导航模块时
                //如果指示器还没有移动到滚动页面最后一页的中间导航模块时,滚动导航模块使其可见
                if (position >= mFinalVisibleCount - 1 - mMiddlePosition && position < mChildCount - 1 - mMiddlePosition && offset > 0 && isModelNeedScroll) {
                    this.scrollTo((int) ((position - (mFinalVisibleCount - 1 - mMiddlePosition) + offset) * mTabWidth), 0);
                }

            } else if (mFinalVisibleCount == 1) {
                //跟随指示器偏移位置(此时指示器看起来像静止的效果)
                this.scrollTo((int) ((position + offset) * mTabWidth), 0);
            }
            invalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildAt(0) instanceof LinearLayout)
            mContainer = (LinearLayout) getChildAt(0);
        for (int i = 0, childCount = mContainer.getChildCount(); i < childCount; i++) {
            View view = mContainer.getChildAt(i);
            final int current = i;
            if (view instanceof TextView) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mViewPager != null) {
                            mViewPager.setCurrentItem(current);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        if (mContainer == null) {
            mContainer = new LinearLayout(mContext);
            mContainer.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mContainer.setOrientation(LinearLayout.HORIZONTAL);
        }
        mChildCount = mContainer.getChildCount();
        if (!isOnce && mChildCount > 0) {
            if (mFinalVisibleCount == -1)
                mFinalVisibleCount = mChildCount < mTabVisibleCount ? mChildCount : mTabVisibleCount;
            isModelNeedScroll = mChildCount > mFinalVisibleCount;
            mMiddlePosition = mFinalVisibleCount / 2;
            //如果最终可见模块数量大于默认模块数量则指示器宽度随着可见模块数量变化
            if (mFinalVisibleCount >= DEFAULT_VISIBLE_COUNT) {
                mTriangleWidth = (int) (mScreenWidth / mFinalVisibleCount * RADIO_TRIANGLE_WIDTH);
            }
            //如果最终可见模块数量过小,则以默认数量时指示器的宽度为实际宽度
            else {
                mTriangleWidth = (int) (mScreenWidth / DEFAULT_VISIBLE_COUNT * RADIO_TRIANGLE_WIDTH);
            }
            //指示器开始时水平偏移的位子，为模块中间
            mInitTranslationX = mScreenWidth / mFinalVisibleCount / 2 - mTriangleWidth / 2;
            mTabWidth = mScreenWidth / mFinalVisibleCount;
            initTriangle();
            //如果子view的数量大于最终的可见模块数量,则需要将所有子view的权重取消,通过设置宽度显示出最终可见的模块
            if (mChildCount > mFinalVisibleCount) {
                if (mChildCount >= Integer.MAX_VALUE >> 2)
                    throw new RuntimeException("控件数量过于庞大");
                for (int i = 0; i < mChildCount; i++) {
                    View view = mContainer.getChildAt(i);
                    LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) view.getLayoutParams();
                    param.weight = 0;
                    param.width = mScreenWidth / mFinalVisibleCount;
                    view.setLayoutParams(param);
                }
            }
            isOnce = true;
        }

    }

    public void setTabTitle(List<String> titles, int visibleCount) {
        if (mContainer == null) {
            mContainer = new LinearLayout(mContext);
            mContainer.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mContainer.setOrientation(LinearLayout.HORIZONTAL);
        }
        if (titles != null && titles.size() > 0) {
            mContainer.removeAllViews();
            if (visibleCount > 0 && visibleCount <= titles.size()) {
                mFinalVisibleCount = visibleCount;
            } else {
                mFinalVisibleCount = titles.size() < DEFAULT_VISIBLE_COUNT ? titles.size() : DEFAULT_VISIBLE_COUNT;
            }
            for (int i = 0; i < titles.size(); i++) {
                final int current = i;
                String title = titles.get(i);
                TextView textView = new TextView(mContext);
                textView.setText(title);
                textView.setTextColor(Color.GRAY);
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(current);
                    }
                });
                mContainer.addView(textView);
            }
        }
    }

    private ViewPager mViewPager;

    public void setViewPager(ViewPager viewPager, int position) {
        mViewPager = viewPager;
        mViewPager.setCurrentItem(position);
        //如果是setOnPageChangeListener则需要定义一个接口供该对象使用代替原来的接口
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scroll(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                changeTitleState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        changeTitleState(position);
    }

    private void changeTitleState(int position) {
        for (int i = 0, childCount = mContainer.getChildCount(); i < childCount; i++) {
            View view = mContainer.getChildAt(i);
            if (view instanceof TextView) {
                if (i != position) {
                    ((TextView) view).setTextColor(Color.GRAY);
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                } else {
                    ((TextView) view).setTextColor(Color.BLUE);
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                }
            }
        }

    }
}
