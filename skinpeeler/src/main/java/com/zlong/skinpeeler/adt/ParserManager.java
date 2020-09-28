package com.zlong.skinpeeler.adt;

import android.content.res.Resources;

import com.zlong.skinpeeler.SkinView;
import com.zlong.skinpeeler.utils.LogUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * 管理所有的属性适配器
 * - 动态添加新的属性适配器
 * - 调用所有的属性适配器进行换肤
 * - 调用所有的属性适配器进行还原
 * Time: 2020/9/24 0024
 * Author: zoulong
 */
public class ParserManager {
    private static ParserManager mParserManager = null;
    private ArrayList<BaseAttrADT> attrParsers = new ArrayList<>();
    public ParserManager(){
        attrParsers.add(new AutoAttrADT());
    };

    public static ParserManager getInstance(){
            if(mParserManager == null){
                mParserManager = new ParserManager();
            }
        return mParserManager;
    }

    /**
     * 添加属性适配器
     * @param attrParser 属性适配器
     * @param <P> 实现了{@link BaseAttrADT}的类
     */
    public <P extends BaseAttrADT> void addParser(P attrParser){
        if(attrParser == null){
            LogUtils.e("attrParser is'nt null");
            return;
        }
        attrParsers.add(attrParser);
    }

    public boolean isAdtAttr(String name){
        for(BaseAttrADT attrParser : attrParsers){
            if(attrParser.getAttrName().contains(name)) return true;
        }
        return false;
    }

    public void skin(Resources mResource, SkinView skinView, String skinPackageName) {
        Map<String, String> attrNV = skinView.getAttrNV();
        for(String attrName : attrNV.keySet()){
            String attrValue = attrNV.get(attrName);
            for(BaseAttrADT attrParser : attrParsers){
                try {
                    attrParser.applySkin(skinView.getTargetView(), mResource, skinPackageName, attrName, attrValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void restore(Resources mResource, SkinView skinView, String skinPackageName) {
        Map<String, String> attrNV = skinView.getAttrNV();
        for(String attrName : attrNV.keySet()){
            String attrValue = attrNV.get(attrName);
            for(BaseAttrADT attrParser : attrParsers){
                attrParser.restore(skinView.getTargetView(), mResource, attrName, attrValue);
            }
        }
    }
}
