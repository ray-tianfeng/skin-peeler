package com.zlong.skinpeeler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Time: 2020/9/25 0025
 * Author: zoulong
 */
public class SkinPeeler {
    private static ISkin mSkin;

    public static ISkin getInstance(){
        if(mSkin == null){
            synchronized(SkinPeeler.class){
                if(mSkin == null){
                    final SkinIns skinIns = SkinIns.getInstance();
                    mSkin = (ISkin) Proxy.newProxyInstance(skinIns.getClass().getClassLoader(), skinIns.getClass().getInterfaces(), new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            return method.invoke(skinIns, args);
                        }
                    });
                }
            }
        }
        return mSkin;
    }

    public static interface OnSkinChangeListener{
        public void onSkinChanged();
    }
}
