package com.czy.floatball.View;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.czy.floatball.Manager.ViewManager;
import com.czy.floatball.R;

/**
 * Created by ZY on 2016/8/10.
 * 底部菜单栏
 */
public class FloatMenu extends LinearLayout {

    private LinearLayout layout;

    private TranslateAnimation animation;

    public FloatMenu(final Context context) {
        super(context);
        View root = View.inflate(context, R.layout.float_menu, null);
        layout = (LinearLayout) root.findViewById(R.id.layout);
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(500);
        animation.setFillAfter(true);
        layout.setAnimation(animation);
        root.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewManager manager = ViewManager.getInstance(context);
                manager.showFloatBall();
                manager.hideFloatMenu();
                return false;
            }
        });
        addView(root);
    }

    public void startAnimation() {
        animation.start();
    }
}
