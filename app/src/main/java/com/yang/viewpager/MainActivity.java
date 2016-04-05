package com.yang.viewpager;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yang.viewpager.fragment.SimpleFragment;
import com.yang.viewpager.view.Indicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private ViewPager mViewPager;
    private Indicator mIndicator;
    private List<String> mTitle = Arrays.asList("推荐", "娱乐", "体育", "视频", "科技", "财经", "汽车", "社会");
    private List<SimpleFragment> mContents = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatActivity下会报错
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initFindViewById();
        initData();
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager, 0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e("test", "" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initData() {
        for (String title : mTitle) {
            SimpleFragment fragment = SimpleFragment.getInstance(title);
            mContents.add(fragment);
        }
//        for (String title : mTitle) {
//            TextView textView = new TextView(this);
//            textView.setText(title);
//            textView.setTextColor(Color.WHITE);
//            textView.setGravity(Gravity.CENTER);
//            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
//            mIndicator.addView(textView);
//        }
        mIndicator.setTabTitle(mTitle, 5);
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mContents.get(position);
            }

            @Override
            public int getCount() {
                return mContents.size();
            }
        };
    }

    private void initFindViewById() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewPager);
        mIndicator = (Indicator) findViewById(R.id.id_indicator);
    }
}
