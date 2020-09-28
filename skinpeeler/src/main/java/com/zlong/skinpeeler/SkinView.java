package com.zlong.skinpeeler;

import android.view.View;

import java.util.Map;

/**
 * Time: 2020/9/24 0024
 * Author: zoulong
 */
public class SkinView {
    private View targetView;
    private Map<String, String> attrNV;

    public SkinView() {
    }

    public SkinView(View targetView, Map<String, String> attrNV) {
        this.targetView = targetView;
        this.attrNV = attrNV;
    }

    public View getTargetView() {
        return targetView;
    }

    public void setTargetView(View targetView) {
        this.targetView = targetView;
    }

    public Map<String, String> getAttrNV() {
        return attrNV;
    }

    public void setAttrNV(Map<String, String> attrNV) {
        this.attrNV = attrNV;
    }
}
