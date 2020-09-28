package com.zlong.skinpeeler;

import android.app.Activity;
import android.content.res.Resources;

/**
 * 对外接口
 * Time: 2020/9/25 0025
 * Author: zoulong
 */
public interface ISkin {
    /**
     * 使用皮肤
     * @param skinPath 皮肤路径
     */
    public void skin(String skinPath);

    /**
     * 还原到原始皮肤
     */
    public void restore();

    /**
     * 监听皮肤切换
     * @param mActivity 监听页面
     * @param mOnSkinChangeListener 监听器
     */
    public void addSkinChangeListener(Activity mActivity, SkinPeeler.OnSkinChangeListener mOnSkinChangeListener);

    /**
     * 获取皮肤Resources
     * 初始化失败时返回为空
     */
    public Resources getSkinResources();

    /**
     * 获取皮肤包包名
     * @return 皮肤包包名
     */
    public String getSkinPackageName();
}
