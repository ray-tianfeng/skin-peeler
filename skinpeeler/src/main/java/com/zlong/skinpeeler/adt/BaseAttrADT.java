package com.zlong.skinpeeler.adt;

import android.content.res.Resources;
import android.view.View;

import java.util.List;

/**
 * 属性适配器
 * Time: 2020/9/24 0024
 * Author: zoulong
 */
public interface BaseAttrADT {
    //支持的属性集合
    public List<String> getAttrName();

    /**
     * 应用皮肤
     * @param targetView 目标视图
     * @param skinResources 皮肤Resources
     * @param skinPackageName 皮肤包包名
     * @param attrName 属性名称
     * @param oldValueName 旧值方便通过{@link com.zlong.skinpeeler.utils.IdUtils} 查找皮肤包资源属性和名称
     */
    public void applySkin(View targetView, Resources skinResources, String skinPackageName, String attrName, String oldValueName) throws Exception;

    /**
     * 恢复原始皮肤
     * @param targetView 目标视图
     * @param resources 原始 Resources
     * @param attrName 属性名称
     * @param oldValueName 旧值
     */
    public void restore(View targetView, Resources resources, String attrName, String oldValueName);
}
