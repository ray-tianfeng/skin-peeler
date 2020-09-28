package com.zlong.skinpeeler.utils;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * 通过反射获取原包资源id 属性类型 名称对应表
 * Time: 2020/9/24 0024
 * Author: zoulong
 */
public class IdUtils {
   static ArrayList<ReID> reIDS = new ArrayList<>();
    public static boolean load(Context mContext){
        try {
            String packageName = mContext.getPackageName();
            Class RClass = Class.forName(packageName + ".R");
            Class[] innerClass = RClass.getDeclaredClasses();
            for(Class resClass : innerClass){
                String name = resClass.getSimpleName();
                for(Field mField : resClass.getDeclaredFields()){
                    mField.setAccessible(true);
                    Object resId = mField.get(null);
                    if(!(resId instanceof Integer)) continue;
                    reIDS.add(new ReID(name, mField.getName(), (Integer) resId));
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ReID findResById(int oldId){
        for(ReID reID : reIDS){
            if(oldId == reID.resId) return reID;
        }
        LogUtils.e("find old res id final id:" + oldId);
        return null;
    }

    public static class ReID{
        private String type;
        private String name;
        private int resId;

        public ReID(String type, String name, int resId) {
            this.type = type;
            this.name = name;
            this.resId = resId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getResId() {
            return resId;
        }

        public void setResId(int resId) {
            this.resId = resId;
        }
    }
}
