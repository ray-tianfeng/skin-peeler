package com.zlong.skinpeeler.adt;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlong.skinpeeler.utils.IdUtils;
import com.zlong.skinpeeler.utils.LogUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 默认实现的属性解析器，如果需要支持其它属性或者自定义属性，需要实现{@link BaseAttrADT} 接口
 * 属性解析器
 * - 返回需要解析的属性
 * - 换肤时从skinResources中获取皮肤包中的资源然后在应用
 * - 还原皮肤
 * 特别说明：
 * 使用换肤工具时，不能使用R.xx.xx的方式去设置资源，以为我们的皮肤包生成了一个Resources，原包的中R.xx.xx和皮肤包的资源id值不同，如果直接使用原包中的id值，会找不到资源
 * 需要先去皮肤包中查找对应资源在皮肤包的id（使用getIdentifier，或者classloader加载皮肤包后，通过反射去查找）
 * 然后通过皮肤包中的资源id去查找响应的资源
 * Time: 2020/9/24 0024
 * Author: zoulong
 */
public class AutoAttrADT implements BaseAttrADT {
    private List<String> adtAttrs = Arrays.asList(new String[]{
            "background",
            "src",
            "textColor",
            "drawableLeft",
            "drawableTop",
            "drawableRight",
            "drawableBottom"
    });
    @Override
    public List<String> getAttrName() {
        return adtAttrs;
    }

    @Override
    public void applySkin(View targetView, Resources skinResources, String skinPackageName, String attrName, String oldResValue) {
        int oldResId = -1;
        int newResId = -1;
        String resType = null;
        try {
            oldResId = Integer.parseInt(oldResValue.substring(1));
            IdUtils.ReID resId = IdUtils.findResById(oldResId);
            if(resId == null) {
                LogUtils.e(String.format("findResById(%d) is null, please check skin res id?", oldResId));
                return;
            }
            resType = resId.getType();
            newResId = skinResources.getIdentifier(resId.getName(), resId.getType(), skinPackageName);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        if(oldResId == -1 || newResId == -1 || TextUtils.isEmpty(resType)) return;
        switch (attrName){
            case "background":
                if(resType.equals("color")){
                    targetView.setBackgroundColor(skinResources.getColor(newResId));
                }else if(resType.equals("drawable")){
                    targetView.setBackgroundDrawable(skinResources.getDrawable(newResId));
                }
                break;
            case "src":
                if(resType.equals("color")){
                    ((ImageView)targetView).setImageDrawable(new ColorDrawable(skinResources.getColor(newResId)));
                }else if(resType.equals("drawable")){
                    ((ImageView)targetView).setImageDrawable(skinResources.getDrawable(newResId));
                }
                break;
            case "textColor":
                ((TextView)targetView).setTextColor(skinResources.getColorStateList(newResId));
                break;
            case "drawableLeft":
                TextView view = (TextView) targetView;
                Drawable drawable = skinResources.getDrawable(newResId);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                Drawable[] compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(drawable, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
                break;
            case "drawableTop":
                view = (TextView) targetView;
                drawable = skinResources.getDrawable(newResId);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(compoundDrawables[0], drawable, compoundDrawables[2], compoundDrawables[3]);
                break;
            case "drawableRight":
                view = (TextView) targetView;
                drawable = skinResources.getDrawable(newResId);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], drawable, compoundDrawables[3]);
                break;
            case "drawableBottom":
                view = (TextView) targetView;
                drawable = skinResources.getDrawable(newResId);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], drawable);
                break;
        }
    }

    @Override
    public void restore(View targetView, Resources skinResources, String attrName, String oldResValue) {
        int resIdPro = -1;
        try {
            resIdPro = Integer.parseInt(oldResValue.substring(1));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if(resIdPro == -1) return;
        switch (attrName){
            case "background":
                targetView.setBackgroundResource(resIdPro);
                break;
            case "src":
                ((ImageView)targetView).setImageResource(resIdPro);
                break;
            case "textColor":
                ((TextView)targetView).setTextColor(skinResources.getColorStateList(resIdPro));
                break;
            case "drawableLeft":
                TextView view = (TextView) targetView;
                Drawable drawable = skinResources.getDrawable(resIdPro);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                Drawable[] compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(drawable, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
                break;
            case "drawableTop":
                view = (TextView) targetView;
                drawable = skinResources.getDrawable(resIdPro);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(compoundDrawables[0], drawable, compoundDrawables[2], compoundDrawables[3]);
                break;
            case "drawableRight":
                view = (TextView) targetView;
                drawable = skinResources.getDrawable(resIdPro);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], drawable, compoundDrawables[3]);
                break;
            case "drawableBottom":
                view = (TextView) targetView;
                drawable = skinResources.getDrawable(resIdPro);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
                compoundDrawables = view.getCompoundDrawables();
                view.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], drawable);
                break;
        }
    }
}
