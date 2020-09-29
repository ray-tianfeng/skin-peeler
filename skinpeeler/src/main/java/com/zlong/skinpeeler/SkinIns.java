package com.zlong.skinpeeler;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zlong.skinpeeler.adapter.ActivityLifecycleCallbackAdapter;
import com.zlong.skinpeeler.adt.BaseAttrADT;
import com.zlong.skinpeeler.adt.AttrADTManager;
import com.zlong.skinpeeler.utils.IdUtils;
import com.zlong.skinpeeler.utils.LayoutInflaterHelper;
import com.zlong.skinpeeler.utils.LogUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 主要实现类
 * 技术点：
 * 1. 设置View时实际调用了LayoutInflater的createViewFromTag - > Factory2.onCreateView
 * 2. 我们hook LayoutInflater的Factory2，替换为我们实现的Factory2
 * 3. 在Factory2的onCreateView方法中,我们记录{@link BaseAttrADT#getAttrName()}我们定义的需要替换资源的属性
 * 4. 通过设置的资源包生成一个Resources
 * 5. 换肤时将皮肤中的资源设置到第三步记录的视图上
 * Time: 2020/9/23 0023
 * Author: zoulong
 */
class SkinIns implements ISkin{
    private Application mApplication;
    private static SkinIns mSkinPeeler = null;
    //资源包Resources
    private Resources mResource;
    //资源包包名
    private String skinPackageName;
    //页面数据集合， 页面、页面中需要替换的view，切换监听，当页面销毁时自动销毁数据
    private Map<Activity, PageEntity> pageEntityMap = new HashMap<>();
    public SkinIns(){};

    public static SkinIns getInstance(){
        if(mSkinPeeler == null){
            synchronized(SkinIns.class){
                if(mSkinPeeler == null){
                    mSkinPeeler = new SkinIns();
                }
            }
        }
        return mSkinPeeler;
    }

    public void init(Application mApplication){
        this.mApplication = mApplication;
        //注册activity监听
        registerActivityLifecycleCallbacks(mApplication);
        //通过反射获取所有的id对应映射
        IdUtils.load(mApplication);
        //创建一个皮肤包Resources
        initResources(mApplication) ;
        //设置皮肤包包名
        initSkinPackageName(mApplication);
    }

    private void initSkinPackageName(Application mApplication) {
        try {
            //获取外部Apk（皮肤薄） 包名
            PackageManager packageManager = mApplication.getPackageManager();
            PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(skinPackPath(), PackageManager.GET_ACTIVITIES);
            this.skinPackageName = packageArchiveInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initResources(Application mApplication) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, skinPackPath());
            mResource = new Resources(assetManager, mApplication.getResources().getDisplayMetrics(), mApplication.getResources().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerActivityLifecycleCallbacks(Application mApplication) {
        mApplication.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbackAdapter(){
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                super.onActivityCreated(activity, savedInstanceState);
                LayoutInflater mLayoutInflater = activity.getLayoutInflater();
                LayoutInflaterHelper.setFactorySetFalse(mLayoutInflater);
                LayoutInflaterHelper.setFactory2(activity, mLayoutInflater);
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                super.onActivityDestroyed(activity);
                pageEntityMap.remove(activity);
            }
        });
    }

    /**
     * 记录控件需要处理的属性
     */
    protected void recordPageSkinView(Activity mActivity, SkinView skinView){
        PageEntity pageEntity = pageEntityMap.get(mActivity);
        if(pageEntity == null) pageEntity = new PageEntity();
        pageEntity.skinViews.add(skinView);
        pageEntityMap.put(mActivity, pageEntity);
    }

    /**
     * 给一个View换肤
     */
    protected void skinView(SkinView skinView){
        if(isApply()) return;
        AttrADTManager.getInstance().skin(mResource, skinView, skinPackageName);
    }

    /**
     * 获取皮肤包路径
     */
    private String skinPackPath(){
        SharedPreferences sp = mApplication.getSharedPreferences("skin-peeler", Context.MODE_PRIVATE);
        return sp.getString("skin-path", "");
    }

    /**
     * 设置皮肤包路径
     */
    private void setSkinPackPath(String skinPath){
        SharedPreferences sp = mApplication.getSharedPreferences("skin-peeler", Context.MODE_PRIVATE);
        sp.edit().putString("skin-path", skinPath).commit();
    }

    /**
     * 皮肤发生改变，在代码中设置皮肤包中的图片等操作
     */
    private void skinChanged(){
        for(PageEntity pageEntity : pageEntityMap.values()){
            if(pageEntity.mOnSkinChangeListener != null){
                pageEntity.mOnSkinChangeListener.onSkinChanged();
            }
        }
    }

    public void skin(String skinPath){
        if(!new File(skinPath).isFile()){
            LogUtils.e("skinpath is'nt file!");
            return;
        }
        setSkinPackPath(skinPath);
        initResources(mApplication);
        initSkinPackageName(mApplication);
        for(PageEntity pageEntity : pageEntityMap.values()){
            for(SkinView skinView : pageEntity.skinViews){
                if(isApply()) return;
                AttrADTManager.getInstance().skin(mResource, skinView, skinPackageName);
            }
        }
        skinChanged();
    }

    public void restore() {
        setSkinPackPath("");
        mResource = mApplication.getResources();
        for(PageEntity pageEntity : pageEntityMap.values()){
            for(SkinView skinView : pageEntity.skinViews){
                AttrADTManager.getInstance().restore(mApplication.getResources(), skinView, skinPackageName);
            }
        }
        skinChanged();
    }

    public void addSkinChangeListener(Activity mActivity, SkinPeeler.OnSkinChangeListener mOnSkinChangeListener){
        if(mActivity == null || mOnSkinChangeListener == null) return;
        PageEntity pageEntity = pageEntityMap.get(mActivity);
        if(pageEntity == null){
            LogUtils.e(mActivity.getClass().getSimpleName() + "is empty");
            return;
        }
        pageEntity.mOnSkinChangeListener = mOnSkinChangeListener;
    }

    @Override
    public Resources getSkinResources() {
        if(isApply()) return mApplication.getResources();
        return mResource;
    }

    @Override
    public String getSkinPackageName() {
        if(isApply()) return mApplication.getPackageName();
        return skinPackageName;
    }

    @Override
    public <P extends BaseAttrADT> void addAttrADT(P attrADT) {
        AttrADTManager.getInstance().addAttrADT(attrADT);
    }

    public boolean isApply(){
        return mResource == null || TextUtils.isEmpty(skinPackageName) || TextUtils.isEmpty(skinPackPath());
    }

    private class PageEntity{
        ArrayList<SkinView> skinViews = new ArrayList<>();
        SkinPeeler.OnSkinChangeListener mOnSkinChangeListener;

        public PageEntity() {
        }

        public PageEntity(ArrayList<SkinView> skinViews, SkinPeeler.OnSkinChangeListener mOnSkinChangeListener) {
            this.skinViews = skinViews;
            this.mOnSkinChangeListener = mOnSkinChangeListener;
        }
    }
}
