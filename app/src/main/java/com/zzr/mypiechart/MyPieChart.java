package com.zzr.mypiechart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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
    private Paint arcPaint;
    /**
     * 黄线以及横线上文字的画笔
     */
    private Paint dataPaint;

    /**
     * 圆中心上面文字画笔
     */
    private Paint centerTextPaint;
    /**
     * 圆中心下面文字画笔
     */
    private Paint centerTextPaint2;
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
    private float circleWidth = 100;
    /**
     * 横线的长度
     */
    private float lineWidth = 100;
    private float centerTextSize = 50;
    private int centerTextColor = Color.BLACK;
    private float dataTextSize = 40;
    private String textPercent;
    private Paint percentPaint;
    private Rect percentRect;
    private float total;

    public MyPieChart(Context context) {
        this(context, null);
    }

    public MyPieChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyPieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyPieChart);
        centerTextSize = a.getDimension(R.styleable.MyPieChart_centerTextSize, centerTextSize);
        centerTextColor = a.getColor(R.styleable.MyPieChart_centerTextColor, centerTextColor);
        dataTextSize = a.getDimension(R.styleable.MyPieChart_dataTextSize, dataTextSize);
        lineWidth = a.getDimension(R.styleable.MyPieChart_lineWidth, lineWidth);
        a.recycle();
        init();
    }

    private void init() {
//        lineWidth = DisplayMetricsUtil.dip2px(getContext(), 50);
//        circleWidth = DisplayMetricsUtil.dip2px(getContext(), 40);
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStrokeWidth(circleWidth);
        arcPaint.setStyle(Paint.Style.STROKE);

        dataPaint = new TextPaint();
        dataPaint.setAntiAlias(true);
        dataPaint.setTextSize(dataTextSize);

        centerTextPaint = new Paint();
        centerTextPaint.setTextSize(centerTextSize);
        centerTextPaint.setAntiAlias(true);
        centerTextPaint.setColor(centerTextColor);
        centerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        centerTextPaint2 = new Paint();
        centerTextPaint2.setTextSize(centerTextSize);
        centerTextPaint2.setAntiAlias(true);
        centerTextPaint2.setColor(centerTextColor);

        rectF = new RectF();
        textBounds = new Rect();
        textBounds2 = new Rect();
        //百分百数字的格式
        numberFormat = new DecimalFormat("0.0");

        textPercent = "00.0%";
        percentPaint = new Paint();
        percentRect = new Rect();
        dataPaint.getTextBounds(textPercent, 0, textPercent.length(), percentRect);
    }


    public void setCircleWidth(float circleWidth) {
        this.circleWidth = circleWidth;
    }

    public void setPieEntities(List<PieEntity> pieEntities) {
        if (pieEntities == null || pieEntities.isEmpty()) {
            Toast.makeText(getContext(), "沒有数据", Toast.LENGTH_SHORT).show();
            return;
        }
        this.pieEntities = pieEntities;
        //数据总和
        total = 0f;
        for (PieEntity pieEntity : pieEntities) {
            total += pieEntity.getNumber();
        }
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
        //画中间的文字
        drawCenterText(canvas);
        //画圆弧，横线，文字
        calculateAndDraw(canvas);
    }

    private void calculateAndDraw(Canvas canvas) {
        float startC = 0;
        //新的半径，用来计算横线的起始坐标
        nRadius = radius + circleWidth / 2;
        float minTextWidth = (float) Math.sqrt((Math.pow(nRadius, 2))
                - Math.pow(nRadius - textBounds.height(), 2));
        for (PieEntity pieEntity : pieEntities) {
            //百分比
            String percent = numberFormat.format(pieEntity.getNumber() / total * 100) + "%";
            //角度
            float sweep = (pieEntity.getNumber() / total) * 360;
            dataPaint.setColor(ContextCompat.getColor(getContext(), pieEntity.getColorRes()));
            arcPaint.setColor(ContextCompat.getColor(getContext(), pieEntity.getColorRes()));
            //画扇形
            canvas.drawArc(rectF, startC - 0.5f, sweep + 0.5f, false, arcPaint);

            //弧线中点与圆心连线 与起始角度的夹角
            float arcCenterC = startC + sweep / 2;
            //横线起点
            float arcCenterX = 0;
            float arcCenterY = 0;
            //横线终点
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
                    arcCenterX2 = arcCenterX + lineWidth * 2;
                }
                canvas.drawText(percent, a, arcCenterY - percentRect.height() / 4, dataPaint);
                canvas.drawText(percent, a, arcCenterY + percentRect.height() * 5 / 4, dataPaint);
            } else if (arcCenterC >= 90 && arcCenterC < 180) {
                arcCenterC = 180 - arcCenterC;
                arcCenterX = (float) (centerX - nRadius * Math.cos(Math.toRadians(arcCenterC)));
                arcCenterY = (float) (centerY + nRadius * Math.sin(Math.toRadians(arcCenterC)));
                arcCenterX2 = arcCenterX - lineWidth;
                arcCenterY2 = arcCenterY;
                //画文字
                float minTextX = centerX - minTextWidth - percentRect.width() * 2 / 5 - percentRect.width();
                float a = arcCenterX - percentRect.width() * 2 / 5 - percentRect.width();
                if (a > minTextX) {
                    a = minTextX;
                    arcCenterX2 = arcCenterX - lineWidth * 2;
                }
                canvas.drawText(percent, a, arcCenterY - percentRect.height() / 4, dataPaint);
                canvas.drawText(percent, a, arcCenterY + percentRect.height() * 5 / 4, dataPaint);
            } else if (arcCenterC >= 180 && arcCenterC < 270) {
                arcCenterC = 270 - arcCenterC;
                arcCenterX = (float) (centerX - nRadius * Math.sin(Math.toRadians(arcCenterC)));
                arcCenterY = (float) (centerY - nRadius * Math.cos(Math.toRadians(arcCenterC)));
                arcCenterX2 = arcCenterX - lineWidth;
                arcCenterY2 = arcCenterY;
                //画文字
                float minTextX = centerX - minTextWidth - percentRect.width() * 2 / 5 - percentRect.width();
                float a = arcCenterX - percentRect.width() * 2 / 5 - percentRect.width();
                if (a > minTextX) {
                    a = minTextX;
                    arcCenterX2 = arcCenterX - lineWidth * 2;
                }
                canvas.drawText(percent, a, arcCenterY - percentRect.height() / 4, dataPaint);
                canvas.drawText(percent, a, arcCenterY + percentRect.height() * 5 / 4, dataPaint);
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
                    arcCenterX2 = arcCenterX + lineWidth * 2;
                }
                canvas.drawText(percent, a, arcCenterY - percentRect.height() / 4, dataPaint);
                canvas.drawText(percent, a, arcCenterY + percentRect.height() * 5 / 4, dataPaint);
            }
            //画横线
            canvas.drawLine(arcCenterX, arcCenterY, arcCenterX2, arcCenterY2, dataPaint);
            pieEntity.setStartC(startC);
            pieEntity.setEndC(startC + sweep);
            startC += sweep;
        }
    }

    private void drawCenterText(Canvas canvas) {
        String totalText = String.valueOf(total);
        centerTextPaint.getTextBounds(totalText, 0, totalText.length(), textBounds);
        //画上面文字
        canvas.drawText(totalText, centerX - textBounds.width() / 2, centerY - textBounds.height() / 2,
                centerTextPaint);
        String text2 = "总数（起）";
        centerTextPaint.getTextBounds(text2, 0, text2.length(), textBounds2);
        //画下面的文字
        canvas.drawText(text2, centerX - textBounds2.width() / 2, centerY + textBounds2.height(),
                centerTextPaint2);
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
