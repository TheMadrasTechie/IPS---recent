package com.m.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineView extends View
{
    int degree = 0;
    private Paint paint = new Paint();

    private List<PointF> points = new ArrayList<PointF>();
    Canvas canvas;
//    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public LineView(Context context) {
        super(context);
//        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public LineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        setFocusable(true);
    }

    public LineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        // Let the ScaleGestureDetector inspect all events.
//        getParent().requestDisallowInterceptTouchEvent(true);
//        mScaleDetector.onTouchEvent(ev);
//        return true;
//    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.scale(mScaleFactor, mScaleFactor);
        paint.setColor(Color.RED);

        paint.setStrokeWidth(10);

        for(int i=0; i< points.size(); i++) {
            if(i < points.size()-1){
                PointF pointA = points.get(i);
                PointF pointB = points.get(i+1);
                canvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paint);
            }
            else{
                break;
            }
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.restore();
        }
        super.onDraw(canvas);
    }

    public void rotateCanvas(int degree){
       this.degree = degree;
       invalidate();
    }
    public void setPoints(List<PointF> points)
    {
        this.points = points ;
    }
    public void setPoints(PointF points){
        this.points.add(points);
    }


    public void draw()
    {
        invalidate();
        requestLayout();
    }
//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            mScaleFactor *= detector.getScaleFactor();
//
//            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 2.0f));
//
//            invalidate();
//            return true;
//        }
//        public void onScaleEnd(ScaleGestureDetector detector) {
//        }
//        @Override
//        public boolean onScaleBegin(ScaleGestureDetector detector) {
//            return true;
//        }
//    }
}
