package com.zzr.mypiechart;

public class PieEntity {
    private float number;  //数值
    private int colorRes;  //颜色资源
    private boolean selected; //是否选中
    private float startC;     //对应扇形起始角度
    private float endC;       //对应扇形结束角度

    public PieEntity(float number, int colorRes, boolean selected) {
        this.number = number;
        this.colorRes = colorRes;
        this.selected = selected;
    }

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
        this.number = number;
    }

    public int getColorRes() {
        return colorRes;
    }

    public void setColorRes(int colorRes) {
        this.colorRes = colorRes;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public float getStartC() {
        return startC;
    }

    public void setStartC(float startC) {
        this.startC = startC;
    }

    public float getEndC() {
        return endC;
    }

    public void setEndC(float endC) {
        this.endC = endC;
    }
}
