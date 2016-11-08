package eu.power_switch.shared.gui.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import eu.power_switch.shared.R;
import eu.power_switch.shared.ThemeHelper;

/**
 * Simple "wave" like circle indicator view
 * <p>
 * Created by Markus on 08.11.2016.
 */
public class CircleWaveAlertView extends View {

    private static final String TAG = "CircleWaveAlertView";

    private static final int DEFAULT_STROKE_WIDTH = 3;
    private static final int DEFAULT_DURATION_MILLISECONDS = 3000;
    private static final int DEFAULT_WAVE_COUNT = 3;

    private float startDiameter;
    private float targetDiameter;
    private int color;
    private int strokeWidth;
    private int duration;
    private int delayBetweenWaves;
    private int waveCount;

    private float[] currentDiameters;
    private Paint[] paints;

    private ValueAnimator[] colorAnimators;
    private ValueAnimator[] sizeAnimators;


    public CircleWaveAlertView(Context context) {
        this(context, null);
    }

    public CircleWaveAlertView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleWaveAlertView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        readArguments(context, attrs);
    }

    @TargetApi(21)
    public CircleWaveAlertView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        readArguments(context, attrs);
    }

    private void readArguments(Context context, AttributeSet attrs) {
        // read XML attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleWaveAlertView,
                0, 0);

        try {
            int defaultColor = ThemeHelper.getThemeAttrColor(context, R.attr.colorAccent);

            startDiameter = a.getDimensionPixelSize(R.styleable.CircleWaveAlertView_startDiameter, 0);
            targetDiameter = a.getDimensionPixelSize(R.styleable.CircleWaveAlertView_targetDiameter, -1);
            color = a.getColor(R.styleable.CircleWaveAlertView_color, defaultColor);
            strokeWidth = a.getColor(R.styleable.CircleWaveAlertView_strokeWidth, DEFAULT_STROKE_WIDTH);
            duration = a.getInt(R.styleable.CircleWaveAlertView_durationMilliseconds, DEFAULT_DURATION_MILLISECONDS);
            delayBetweenWaves = a.getInt(R.styleable.CircleWaveAlertView_delayMillisecondsBetweenWaves, -1);
            waveCount = a.getInt(R.styleable.CircleWaveAlertView_waveCount, DEFAULT_WAVE_COUNT);

            if (waveCount > 10) {
                Log.w(TAG, "Maximum amount of circles is 10, ignoring higher value and dropping to 10.");
                waveCount = 10;
            }
        } finally {
            a.recycle();
        }
    }

    private void init() {
        currentDiameters = new float[waveCount];
        paints = new Paint[waveCount];
        colorAnimators = new ValueAnimator[waveCount];
        sizeAnimators = new ValueAnimator[waveCount];

        Interpolator interpolator = new DecelerateInterpolator();

        for (int i = 0; i < waveCount; i++) {
            paints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paints[i].setColor(color);
            paints[i].setStyle(Paint.Style.STROKE);
            paints[i].setStrokeWidth(strokeWidth);

            sizeAnimators[i] = ValueAnimator.ofFloat(startDiameter, targetDiameter);
            sizeAnimators[i].setDuration(duration);
            sizeAnimators[i].setRepeatCount(ObjectAnimator.INFINITE);
            sizeAnimators[i].setRepeatMode(ValueAnimator.RESTART);
            sizeAnimators[i].setInterpolator(interpolator);
            final int index = i;
            sizeAnimators[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    currentDiameters[index] = (float) animation.getAnimatedValue();

                    // we only need to rerender the view if the first animator updates, as all animators update at the same speed
                    if (index == 0) {
                        invalidate();
                    }
                }
            });

            colorAnimators[i] = ValueAnimator.ofObject(
                    new ArgbEvaluator(), color, Color.argb(0, Color.red(color), Color.green(color), Color.blue(color)));
            colorAnimators[i].setDuration(duration);
            colorAnimators[i].setRepeatCount(ObjectAnimator.INFINITE);
            colorAnimators[i].setRepeatMode(ValueAnimator.RESTART);
            colorAnimators[i].setInterpolator(interpolator);
            colorAnimators[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    paints[index].setColor((int) animation.getAnimatedValue());
                }
            });
        }

        post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < waveCount; i++) {
                    int delay;
                    if (delayBetweenWaves == -1) {
                        delay = i * (duration / waveCount);
                    } else {
                        delay = i * delayBetweenWaves;
                    }
                    sizeAnimators[i].setStartDelay(delay);
                    colorAnimators[i].setStartDelay(delay);

                    sizeAnimators[i].start();
                    colorAnimators[i].start();
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = (int) targetDiameter + this.getPaddingLeft() + this.getPaddingRight();
        int viewHeight = (int) targetDiameter + this.getPaddingTop() + this.getPaddingBottom();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(viewWidth, widthSize);
        } else {
            //Be whatever you want
            width = viewWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(viewHeight, heightSize);
        } else {
            //Be whatever you want
            height = viewHeight;
        }

        setMeasuredDimension(width, height);

        if (targetDiameter == -1) {
            targetDiameter = Math.min(width, height);
        }

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x = getWidth() / 2;
        float y = getHeight() / 2;

        for (int i = 0; i < waveCount; i++) {
            canvas.drawCircle(x, y, currentDiameters[i] / 2, paints[i]);
        }
    }

}
