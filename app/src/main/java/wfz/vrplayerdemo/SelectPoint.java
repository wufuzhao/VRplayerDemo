package wfz.vrplayerdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class SelectPoint extends View {
    private int width, height;
    private Paint paint, ratePaint, textPaint;
    private int startA, endA;
    private ValueAnimator animator;
    private String showText;
    public SelectPoint(Context context) {
        super(context);
        init();
    }

    public SelectPoint(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SelectPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.WHITE);

        ratePaint = new Paint();
        ratePaint.setColor(getResources().getColor(R.color.colorAccent));
        ratePaint.setStrokeWidth(3);
        ratePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(16);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = (float) (width / 2.0);
        float cy = (float) (height /2.0);
        canvas.drawPoint(cx, cy, paint);
        int r = 8;
        if (startA != endA){
            canvas.drawArc(cx-r, cy-r, cx+r, cy+r,
                    startA, endA, false, ratePaint);
        }

        //top=15
        if (showText != null && showText.length() > 0){
            float top = cy + 15;
            Rect bounds = new Rect();
            textPaint.getTextBounds(showText, 0, showText.length(), bounds);
            int padding = 5;
            float hW = bounds.right/2;
            int textH = -bounds.top;
            textPaint.setColor(Color.argb(128,0,0,0));
            canvas.drawRect(cx - hW - padding, top, cx + hW + padding, top + textH + 2 * padding, textPaint);
            textPaint.setColor(Color.WHITE);
            canvas.drawText(showText, cx - hW, top + textH + padding, textPaint);
        }
    }

    public void startRate(long duration){
        if (animator != null){
            animator.cancel();
        } else {
            animator = ValueAnimator.ofInt(0, 360);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    endA = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        }
        animator.setDuration(duration);
        animator.start();
    }

    public void cancelRate(){
        if (animator != null && animator.isRunning()){
            animator.cancel();
        }
    }

    public void showText(String showText){
        this.showText = showText;
        postInvalidate();
    }

    public void hideText(){
        this.showText = null;
        postInvalidate();
    }

    public void reset(){
        this.showText = null;
        if (animator != null && animator.isRunning()){
            animator.cancel();
        } else {
            postInvalidate();
        }
    }
}
