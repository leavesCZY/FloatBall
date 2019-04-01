先来看一张动态图

![](http://upload-images.jianshu.io/upload_images/2552605-04d65b77a17a4a1e?imageMogr2/auto-orient/strip)

昨天跟着视频学了如何自定义View并做成仿360悬浮球与加速球的样式

可以看出来，做成的效果有：

 -  点击按钮后退出Activity，呈现一个圆形的悬浮球，可以随意拖动并会自动依靠到屏幕一侧，且拖动时会变成一张图片
 -  当点击悬浮球时，悬浮球隐藏，底部出现一个加速球，双击加速球时，呈现水量逐渐增高且波动幅度较小的效果，单击时波浪上下波动且幅度渐小
 -  点击屏幕不包含底部加速球的部位，加速球会隐藏，悬浮球重新出现

要做出这么一个效果，需要两个自定义View与一个自定义ViewGroup

![](http://upload-images.jianshu.io/upload_images/2552605-90e192425bbdb734?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

首先，需要先设计悬浮球View——FloatBall
简单起见，为FloatBall指定一个默认宽度和高度——150像素
然后在`onDraw(Canvas canvas)`方法中，判断FloatBall是否正在被拖动isDrag，如果是，则绘制一张默认图片bitmap，否则则根据绘图函数绘制圆形与居中文本

```java
/**
 * Created by ZY on 2016/8/10.
 * 悬浮球
 */
public class FloatBall extends View {

    public int width = 150;

    public int height = 150;
    //默认显示的文本
    private String text = "50%";
    //是否在拖动
    private boolean isDrag;

    private Paint ballPaint;

    private Paint textPaint;

    private Bitmap bitmap;

    public FloatBall(Context context) {
        super(context);
        init();
    }

    public FloatBall(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        ballPaint = new Paint();
        ballPaint.setColor(Color.GRAY);
        ballPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setTextSize(25);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.ninja);
        //将图片裁剪到指定大小
        bitmap = Bitmap.createScaledBitmap(src, width, height, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isDrag) {
            canvas.drawCircle(width / 2, height / 2, width / 2, ballPaint);
            float textWidth = textPaint.measureText(text);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float dy = -(fontMetrics.descent + fontMetrics.ascent) / 2;
            canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + dy, textPaint);
        } else {
            //正在被拖动时则显示指定图片
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    //设置当前移动状态
    public void setDragState(boolean isDrag) {
        this.isDrag = isDrag;
        invalidate();
    }
}
```

因为FloatBall是不存在于Activity中而在屏幕单独显示的，所以需要用WindowManager来添加View并显示
新建一个类，命名为ViewManager，用来总的管理View的显示与删除
私有化构造函数并采用单例模式

```java
    private static ViewManager manager;
    
    //私有化构造函数
    private ViewManager(Context context) {
        this.context = context;
        init();
    }
    
    //获取ViewManager实例
    public static ViewManager getInstance(Context context) {
        if (manager == null) {
            manager = new ViewManager(context);
        }
        return manager;
    }
```

ViewManager包含有显示与隐藏悬浮球与加速球的函数

```java
//显示浮动小球
    public void showFloatBall() {
        if (floatBallParams == null) {
            floatBallParams = new LayoutParams();
            floatBallParams.width = floatBall.width;
            floatBallParams.height = floatBall.height - getStatusHeight();
            floatBallParams.gravity = Gravity.TOP | Gravity.LEFT;
            floatBallParams.type = LayoutParams.TYPE_TOAST;
            floatBallParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatBallParams.format = PixelFormat.RGBA_8888;
        }
        windowManager.addView(floatBall, floatBallParams);
    }

    //显示底部菜单
    private void showFloatMenu() {
        if (floatMenuParams == null) {
            floatMenuParams = new LayoutParams();
            floatMenuParams.width = getScreenWidth();
            floatMenuParams.height = getScreenHeight() - getStatusHeight();
            floatMenuParams.gravity = Gravity.BOTTOM;
            floatMenuParams.type = LayoutParams.TYPE_TOAST;
            floatMenuParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatMenuParams.format = PixelFormat.RGBA_8888;
        }
        windowManager.addView(floatMenu, floatMenuParams);
    }

    //隐藏底部菜单
    public void hideFloatMenu() {
        if (floatMenu != null) {
            windowManager.removeView(floatMenu);
        }
    }
```

将悬浮球置于Service中开启，这样悬浮球就不那么容易被系统去除了
在onCreate()方法中调用showFloatBall()

```java
public class StartFloatBallService extends Service {

    public StartFloatBallService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        ViewManager manager = ViewManager.getInstance(this);
        manager.showFloatBall();
        super.onCreate();
    }
}
```

此时，只要为MainActivity添加一个按钮，并设定当点击按钮后开启Service，此时即可看到屏幕显示了一个悬浮球

```java
    public void startService(View view) {
        Intent intent = new Intent(this, StartFloatBallService.class);
        startService(intent);
        finish();
    }
```

不过此时悬浮球还不支持拖动与点击，还需要为其添加OnTouchListener与OnClickListener

```java
View.OnTouchListener touchListener = new View.OnTouchListener() {
            float startX;
            float startY;
            float tempX;
            float tempY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();

                        tempX = event.getRawX();
                        tempY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x = event.getRawX() - startX;
                        float y = event.getRawY() - startY;
                        //计算偏移量，刷新视图
                        floatBallParams.x += x;
                        floatBallParams.y += y;
                        floatBall.setDragState(true);
                        windowManager.updateViewLayout(floatBall, floatBallParams);
                        startX = event.getRawX();
                        startY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        //判断松手时View的横坐标是靠近屏幕哪一侧，将View移动到依靠屏幕
                        float endX = event.getRawX();
                        float endY = event.getRawY();
                        if (endX < getScreenWidth() / 2) {
                            endX = 0;
                        } else {
                            endX = getScreenWidth() - floatBall.width;
                        }
                        floatBallParams.x = (int) endX;
                        floatBall.setDragState(false);
                        windowManager.updateViewLayout(floatBall, floatBallParams);
                        //如果初始落点与松手落点的坐标差值超过6个像素，则拦截该点击事件
                        //否则继续传递，将事件交给OnClickListener函数处理
                        if (Math.abs(endX - tempX) > 6 && Math.abs(endY - tempY) > 6) {
                            return true;
                        }
                        break;
                }
                return false;
            }
        };
        OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                windowManager.removeView(floatBall);
                showFloatMenu();
                floatMenu.startAnimation();
            }
        };
        floatBall.setOnTouchListener(touchListener);
        floatBall.setOnClickListener(clickListener);
```


加速球ProgressBall的设计较为复杂，需要用到贝塞尔曲线来呈现波浪效果，且单击双击的效果也需要分开呈现
同样是让ProgressBall继承于View
进度值的意义在于限制水面最终上升到的高度，即根据目标进度值与最大进度值的比例来决定水面高度
波浪总的起伏次数Count用于在单击加速球时，水面上下波动的次数

```java
    //view的宽度
    private int width = 200;
    //view的高度
    private int height = 200;
    //最大进度值
    private final int maxProgress = 100;
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
```

初始化画笔与监听函数

```java
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
```

单击或双击后的渐变效果是利用Handler的`postDelayed(Runnable r, long delayMillis)`方法来实现的，可以设定一个延时时间去执行Runnable ，然后在Runnable 中再次调用自身

```java
class DoubleTapRunnable implements Runnable {
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
    public void startDoubleTapAnimation() {
        handler.postDelayed(doubleTapRunnable, 50);
    }

    class SingleTapRunnable implements Runnable {
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
    public void startSingleTapAnimation() {
        handler.postDelayed(singleTapRunnable, 100);
    }
```

onDraw(Canvas canvas)的重点在于根据比例值来计算水面高度

```java
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
```

因为要呈现ProgressBall时不仅仅是其本身，或者还需要背景色或者文本之类的内容，所以可以将其置于ViewGroup中来显示
布局文件

```java
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:id="@+id/layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:background="#556f7f8f"
        android:clickable="true"
        android:gravity="center"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_marginTop="10dp"
            android:gravity="center"
            android:text="叶应是叶\nhttp://blog.csdn.net/new_one_object" />

        <com.czy.floatball.View.ProgressBall
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="20dp" />

    </LinearLayout>
</RelativeLayout>
```

FloatMenu就作为容纳ProgressBall的容器，并为其赋予从下往上滑动显示的动画效果

```java
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
```

这里提供源代码下载：https://github.com/leavesC/FloatBall