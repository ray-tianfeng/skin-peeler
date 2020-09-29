package com.zlong.skinpeeler;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zlong.skinpeeler.adt.AttrADTManager;
import com.zlong.skinpeeler.utils.ViewProducer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Time: 2020/9/23 0023
 * Author: zoulong
 */
public class SkinLayoutFactory implements LayoutInflater.Factory2 {
    private WeakReference<Activity> targetActivity;
    public SkinLayoutFactory(Activity targetActivity) {
        this.targetActivity = new WeakReference<>(targetActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = ViewProducer.createViewFromTag(context, name, attrs);
        if(view == null) return view;
        Map<String, String> attrNV = new HashMap<>();
        for(int i = 0; i < attrs.getAttributeCount(); i++){
            String attrName = attrs.getAttributeName(i);
            if(!AttrADTManager.getInstance().isAdtAttr(attrName)) continue;
            String attrValue = attrs.getAttributeValue(i);
            attrNV.put(attrName, attrValue);
        }
        if(attrNV.size() != 0){
            SkinView skinView = new SkinView(view, attrNV);
            SkinIns.getInstance().recordPageSkinView(targetActivity.get(), skinView);
            SkinIns.getInstance().skinView(skinView);
        }
        return view;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return null;
    }
}
