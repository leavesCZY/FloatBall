package leavesc.hello.floatball.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ZY on 2016/8/10.
 * 实现的效果为：
 * 双击后小球内的波浪高度从零增加到目标进度值targetProgress
 * 单击后波浪呈现上下浮动且波动渐小的效果
 */
public class ProgressBall extends View {

    //view的宽度
    private int width = 200;
    //view的高度
    private int height = 200;
    //最大进度值
    private static final int maxProgress = 100;
    //当前进度值
    private int currentProgress = 0;
    //目标进度值
    private final int targetProgress = 70;
    //是否为单击
    private boolean isSingleTop;
    //设定波浪总的起伏次数
    private final int Count = 20;
    //当前起伏次数
    private int currentCount;
    //初始振幅大小
    private final int startAmplitude = 15;
    //波浪周期性出现的次数
    private final int cycleCount = width / (startAmplitude * 4) + 1;

    private DoubleTapRunnable doubleTapRunnable = new DoubleTapRunnable();

    private SingleTapRunnable singleTapRunnable = new SingleTapRunnable();

    private Canvas bitmapCanvas;

    private Bitmap bitmap;

    private Path path;

    private Paint ballPaint;

    private Paint progressPaint;

    private Paint textPaint;

    private Context context;

    private Handler handler;

    private GestureDetector gestureDetector;

    public ProgressBall(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ProgressBall(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ProgressBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        //初始化小球画笔
        ballPaint = new Paint();
        ballPaint.setAntiAlias(true);
        ballPaint.setColor(Color.argb(0xff, 0x3a, 0x8c, 0x6c));
        //初始化（波浪）进度条画笔
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(Color.argb(0xff, 0x4e, 0xc9, 0x63));
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //初始化文字画笔
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(25);

        handler = new Handler();
        path = new Path();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        //手势监听
        //重点在于将单击和双击操作分隔开
        SimpleOnGestureListener listener = new SimpleOnGestureListener() {
            //双击
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                //当前波浪起伏次数为零，说明“单击效果”没有影响到现在
                if (currentCount == 0) {
                    //当前进度为零或者已达到目标进度值，说明“双击效果”没有影响到现在，此时可以允许进行双击操作
                    if (currentProgress == 0 || currentProgress == targetProgress) {
                        currentProgress = 0;
                        isSingleTop = false;
                        startDoubleTapAnimation();
                    }
                }
                return super.onDoubleTap(e);
            }

            //单击
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //当前进度值等于目标进度值，且当前波动次数为零，则允许进行单击操作
                if (currentProgress == targetProgress && currentCount == 0) {
                    isSingleTop = true;
                    startSingleTapAnimation();
                }
                return super.onSingleTapConfirmed(e);
            }
        };
        gestureDetector = new GestureDetector(context, listener);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        //接受点击操作
        setClickable(true);
    }

    private class DoubleTapRunnable implements Runnable {
        @Override
        public void run() {
            if (currentProgress < targetProgress) {
                invalidate();
                handler.postDelayed(doubleTapRunnable, 50);
                currentProgress++;
            } else {
                handler.removeCallbacks(doubleTapRunnable);
            }
        }
    }

    //开启双击动作动画
    private void startDoubleTapAnimation() {
        handler.postDelayed(doubleTapRunnable, 50);
    }

    private class SingleTapRunnable implements Runnable {
        @Override
        public void run() {
            if (currentCount < Count) {
                invalidate();
                currentCount++;
                handler.postDelayed(singleTapRunnable, 100);
            } else {
                handler.removeCallbacks(singleTapRunnable);
                currentCount = 0;
            }
        }
    }

    //开启单击动作动画
    private void startSingleTapAnimation() {
        handler.postDelayed(singleTapRunnable, 100);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制圆形
        bitmapCanvas.drawCircle(width / 2, height / 2, width / 2, ballPaint);
        path.reset();
        //高度随当前进度值的变化而变化
        float y = (1 - (float) currentProgress / maxProgress) * height;
        //属性PorterDuff.Mode.SRC_IN代表了progressPaint只显示与下层层叠的部分，
        //所以以下四点虽然连起来是个矩形，可呈现出来的依然是圆形
        //右上角
        path.moveTo(width, y);
        //右下角
        path.lineTo(width, height);
        //左下角
        path.lineTo(0, height);
        //左上角
        path.lineTo(0, y);
        //绘制顶部波浪
        if (!isSingleTop) {
            //是双击
            //根据当前进度大小调整振幅大小，有逐渐减小的趋势
            float tempAmplitude = (1 - (float) currentProgress / targetProgress) * startAmplitude;
            for (int i = 0; i < cycleCount; i++) {
                path.rQuadTo(startAmplitude, tempAmplitude, 2 * startAmplitude, 0);
                path.rQuadTo(startAmplitude, -tempAmplitude, 2 * startAmplitude, 0);
            }
        } else {
            //是单击
            //根据当前次数调整振幅大小，有逐渐减小的趋势
            float tempAmplitude = (1 - (float) currentCount / Count) * startAmplitude;
            //因为想要形成波浪上下起伏的效果，所以根据currentCount的奇偶性来变化贝塞尔曲线转折点位置
            if (currentCount % 2 == 0) {
                for (int i = 0; i < cycleCount; i++) {
                    path.rQuadTo(startAmplitude, tempAmplitude, 2 * startAmplitude, 0);
                    path.rQuadTo(startAmplitude, -tempAmplitude, 2 * startAmplitude, 0);
                }
            } else {
                for (int i = 0; i < cycleCount; i++) {
                    path.rQuadTo(startAmplitude, -tempAmplitude, 2 * startAmplitude, 0);
                    path.rQuadTo(startAmplitude, tempAmplitude, 2 * startAmplitude, 0);
                }
            }
        }
        path.close();
        bitmapCanvas.drawPath(path, progressPaint);
        String text = (int) (((float) currentProgress / maxProgress) * 100) + "%";
        float textWidth = textPaint.measureText(text);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseLine = height / 2 - (metrics.ascent + metrics.descent);
        bitmapCanvas.drawText(text, width / 2 - textWidth / 2, baseLine, textPaint);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
