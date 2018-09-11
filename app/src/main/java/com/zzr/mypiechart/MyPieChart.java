package com.zzr.mypiechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.util.List;

public class MyPieChart extends View {
    /**
     * 数据
     */
    private List<PieEntity> pieEntities;
    /**
     * 圆环的画笔
     */
    private Paint paint;
    /**
     * 横线上的文字
     */
    private Paint textPaint;
    /**
     * 横线下的文字
     */
    private Paint textPaint2;
    /**
     * 圆中心的文字
     */
    private Paint centerTextPaint;
    /**
     * 包围圆弧的矩形
     */
    private RectF rectF;
    /**
     * 文字的范围
     */
    private Rect textBounds;
    private Rect textBounds2;
    /**
     * 圆心坐标
     */
    private float centerX;
    private float centerY;
    /**
     * 圆的半径
     */
    private float radius;
    /**
     * 格式化百分比
     */
    private DecimalFormat numberFormat;
    /**
     * 画笔的宽度(圆环的宽度)
     */
    private float circleWidth;
    /**
     * 横线的长度
     */
    private int lineWidth;
    private String text;
    private String text2;
    private String textPercent;
    private Paint percentPaint;
    private Rect percentRect;

    public MyPieChart(Context context) {
        super(context);
        init();
    }

    public MyPieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyPieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lineWidth = DisplayMetricsUtil.dip2px(getContext(), 50);
        circleWidth = DisplayMetricsUtil.dip2px(getContext(), 40);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(circleWidth);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40);

        textPaint2 = new TextPaint();
        textPaint2.setAntiAlias(true);
        textPaint2.setTextSize(40);

        centerTextPaint = new Paint();
        centerTextPaint.setTextSize(40);
        centerTextPaint.setAntiAlias(true);
        centerTextPaint.setColor(Color.BLACK);

        rectF = new RectF();
        textBounds = new Rect();
        textBounds2 = new Rect();
        //百分百数字的格式
        numberFormat = new DecimalFormat("0.0");
        text = "100";
        text2 = "总数(起)";

        textPercent = "23.8%";
        percentPaint = new Paint();
        percentRect = new Rect();
        textPaint.getTextBounds(textPercent, 0, textPercent.length(), percentRect);
    }

    public void setCircleWidth(float circleWidth) {
        this.circleWidth = circleWidth;
    }

    public void setPieEntities(List<PieEntity> pieEntities) {
        this.pieEntities = pieEntities;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    float nRadius;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        centerX = getPivotX();
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        //设置半径为宽高最小值的1/4
        radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 4;
        //设置扇形外接矩形
        rectF = new RectF(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //数据总和
        float total = 0f;
        for (PieEntity pieEntity : pieEntities) {
            total += pieEntity.getNumber();
        }
        centerTextPaint.getTextBounds(text, 0, text.length(), textBounds);
        //画中间文字
        canvas.drawText(text, centerX - textBounds.width() / 2, centerY - textBounds.height(),
                centerTextPaint);
        centerTextPaint.getTextBounds(text2, 0, text2.length(), textBounds2);
        canvas.drawText(text2, centerX - textBounds2.width() / 2, centerY + textBounds2.height(),
                centerTextPaint);
//        if (radius == 0) {
//            radius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;
//        }
        float startC = 0;
        //新的半径，用来计算折现的起始坐标
        nRadius = radius + circleWidth / 2;
        float minTextWidth = (float) Math.sqrt((Math.pow(nRadius, 2))
                - Math.pow(nRadius - textBounds.height(), 2));
        Log.i("peichart", "minTextWidth: " + minTextWidth);
        for (PieEntity pieEntity : pieEntities) {
            //百分比
            String percent = numberFormat.format(pieEntity.getNumber() / total * 100) + "%";
            //角度
            float sweep = (pieEntity.getNumber() / total) * 360;
            textPaint.setColor(ContextCompat.getColor(getContext(), pieEntity.getColorRes()));
            paint.setColor(ContextCompat.getColor(getContext(), pieEntity.getColorRes()));
//            rectF.set(centerX - radius, centerY - radius,
//                    centerX + radius, centerY + radius);
            //画扇形
            canvas.drawArc(rectF, startC - 0.5f, sweep + 0.5f, false, paint);

            //弧线中点与圆心连线 与起始角度的夹角
            float arcCenterC = startC + sweep / 2;
            //折线起点
            float arcCenterX = 0;
            float arcCenterY = 0;
            //折线终点
            float arcCenterX2 = 0;
            float arcCenterY2 = 0;
            //不同象限，夹角和起点终点坐标不一样
            if (arcCenterC >= 0 && arcCenterC < 90) {
                arcCenterX = (float) (centerX + nRadius * Math.cos(Math.toRadians(arcCenterC)));
                arcCenterY = (float) (centerY + nRadius * Math.sin(Math.toRadians(arcCenterC)));
                arcCenterX2 = arcCenterX + lineWidth;
                arcCenterY2 = arcCenterY;
                //画文字
                float minTextX = centerX + minTextWidth + percentRect.width() * 2 / 5;
                float a = arcCenterX + percentRect.width() * 2 / 5;
                if (a < minTextX) {
                    a = minTextX;
                }
                canvas.drawText(percent, a, arcCenterY - textBounds.height() / 4, textPaint);
                canvas.drawText(percent, a, arcCenterY + textBounds.height() * 3 / 2, textPaint);
            } else if (arcCenterC >= 90 && arcCenterC < 180) {
                arcCenterC = 180 - arcCenterC;
                arcCenterX = (float) (centerX - nRadius * Math.cos(Math.toRadians(arcCenterC)));
                arcCenterY = (float) (centerY + nRadius * Math.sin(Math.toRadians(arcCenterC)));
                arcCenterX2 = arcCenterX - lineWidth;
                arcCenterY2 = arcCenterY;
                //画文字
                float minTextX = centerX - minTextWidth;
                float a = arcCenterX - percentRect.width() * 2 / 5 - percentRect.width();
                if (a > minTextX) {
                    a = minTextX;
                }
                canvas.drawText(percent, a, arcCenterY - textBounds.height() / 4, textPaint);
                canvas.drawText(percent, a, arcCenterY + textBounds.height() * 3 / 2, textPaint);
            } else if (arcCenterC >= 180 && arcCenterC < 270) {
                arcCenterC = 270 - arcCenterC;
                arcCenterX = (float) (centerX - nRadius * Math.sin(Math.toRadians(arcCenterC)));
                arcCenterY = (float) (centerY - nRadius * Math.cos(Math.toRadians(arcCenterC)));
                arcCenterX2 = arcCenterX - lineWidth;
                arcCenterY2 = arcCenterY;
                //画文字
                float minTextX = centerX - minTextWidth;
                float a = arcCenterX - percentRect.width() * 2 / 5 - percentRect.width();
                if (a > minTextX) {
                    a = minTextX;
                }
                canvas.drawText(percent, a, arcCenterY - textBounds.height() / 4, textPaint);
                canvas.drawText(percent, a, arcCenterY + textBounds.height() * 3 / 2, textPaint);
            } else if (arcCenterC >= 270 && arcCenterC < 360) {
                arcCenterC = 360 - arcCenterC;
                arcCenterX = (float) (centerX + nRadius * Math.cos(Math.toRadians(arcCenterC)));
                arcCenterY = (float) (centerY - nRadius * Math.sin(Math.toRadians(arcCenterC)));
                arcCenterX2 = arcCenterX + lineWidth;
                arcCenterY2 = arcCenterY;
                //画文字
                float minTextX = centerX + minTextWidth + percentRect.width() * 2 / 5;
                float a = arcCenterX + percentRect.width() * 2 / 5;
                if (a < minTextX) {
                    a = minTextX;
                }
                canvas.drawText(percent, a, arcCenterY - textBounds.height() / 4, textPaint);
                canvas.drawText(percent, a, arcCenterY + textBounds.height() * 3 / 2, textPaint);
            }
            //画短线 弧线中点为起始点 线平行x轴，长10dp
            canvas.drawLine(arcCenterX, arcCenterY, arcCenterX2, arcCenterY2, textPaint);
            pieEntity.setStartC(startC);
            pieEntity.setEndC(startC + sweep);
            startC += sweep;
        }

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX;
        float touchY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX(); //touch点的坐标
                touchY = event.getY();
                //判断touch点到圆心的距离 是否小于半径
                if (Math.pow(touchX - centerX, 2) + Math.pow(touchY - centerY, 2) <= Math.pow(radius, 2)) {
                    //计算 touch点和圆心的连线 与 x轴正方向的夹角
                    float touchC = getSweep(touchX, touchY);
                    //遍历 List<PieEntry> 判断touch点在哪个扇形中
                    for (int i = 0; i < pieEntities.size(); i++) {
                        if (touchC >= pieEntities.get(i).getStartC() && touchC < pieEntities.get(i).getEndC()) {
                            pieEntities.get(i).setSelected(true);
                            if (listener != null)
                                listener.onItemClick(i); //将被点击的扇形id回调出去
                        } else {
                            pieEntities.get(i).setSelected(false);
                        }
                    }
//                    invalidate();//刷新画布
                }
            case MotionEvent.ACTION_UP:
                performClick();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 获取  touch点/圆心连线  与  x轴正方向 的夹角
     *
     * @param touchX x
     * @param touchY y
     */
    private float getSweep(float touchX, float touchY) {
        float xZ = touchX - centerX;
        float yZ = touchY - centerY;
        float a = Math.abs(xZ);
        float b = Math.abs(yZ);
        double c = Math.toDegrees(Math.atan(b / a));
        if (xZ >= 0 && yZ >= 0) {//第一象限
            return (float) c;
        } else if (xZ <= 0 && yZ >= 0) {//第二象限
            return 180 - (float) c;
        } else if (xZ <= 0 && yZ <= 0) {//第三象限
            return (float) c + 180;
        } else {//第四象限
            return 360 - (float) c;
        }
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(int id);
    }
}
