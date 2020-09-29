## <center>Android动态换肤
现在的很多应用都有换肤的功能，例如QQ。这类应用都是在线下载皮肤包，然后在不重启的情况下直接完成换肤

### 示例
 <img src="demonstrate.gif" width = "600" height = "1150"/>

### 原理
1. Activity setContentView内部调用

关于setContentView的所有方法,这里调用了getWindow()返回了Window，这个Window在activity的attach方法中被赋值为PhoneWindow  
Activity.java源码：
```java

    public void setContentView(@LayoutRes int layoutResID) {
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }

    public void setContentView(View view) {
        getWindow().setContentView(view);
        initWindowDecorActionBar();
    }
    
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().setContentView(view, params);
        initWindowDecorActionBar();
    }
    
    final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback) {
      ...
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
      ...
    }

```

2.  PhoneWindow setContentView内部调用

可以看到实际调用了LayoutInflater.inflate方法  
PhoneWindow.java源码：
```java
 @Override
    public void setContentView(int layoutResID) {
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }
        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        // Note: FEATURE_CONTENT_TRANSITIONS may be set in the process of installing the window
        // decor, when theme attributes and the like are crystalized. Do not check the feature
        // before this happens.
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }

        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            view.setLayoutParams(params);
            final Scene newScene = new Scene(mContentParent, view);
            transitionTo(newScene);
        } else {
            mContentParent.addView(view, params);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }
```

3. LayoutInflater.inflate内部调用

由源码可知，view由Factory2和Factory创建，如果我们hook了Factory2那不是视图的创建可以由我们说了算
LayoutInflater.java源码：
```java
    public View inflate(@LayoutRes int resource, @Nullable ViewGroup root) {
        return inflate(resource, root, root != null);
    }
    
    public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        final Resources res = getContext().getResources();
        if (DEBUG) {
            Log.d(TAG, "INFLATING from resource: \"" + res.getResourceName(resource) + "\" ("
                    + Integer.toHexString(resource) + ")");
        }

        final XmlResourceParser parser = res.getLayout(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }
    
    public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            ...
            final View temp = createViewFromTag(root, name, inflaterContext, attrs);
            result = temp;
            return result;
            ...
        }
    }
    
     View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
            boolean ignoreThemeAttr) {
        ...
            View view;
            if (mFactory2 != null) {
                view = mFactory2.onCreateView(parent, name, context, attrs);
            } else if (mFactory != null) {
                view = mFactory.onCreateView(name, context, attrs);
            } else {
                view = null;
            }
        ...
            return view;
    }
    
    public void setFactory2(Factory2 factory) {
    //由此处可知设置Factory2只能设置一次，所以我们设置时需要将mFactorySet改成false
        if (mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        }
        if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        }
        mFactorySet = true;
        if (mFactory == null) {
            mFactory = mFactory2 = factory;
        } else {
            mFactory = mFactory2 = new FactoryMerger(factory, factory, mFactory, mFactory2);
        }
    }
```

4. Factory2

LayoutInflater.java源码：
```java
public interface Factory2 extends Factory {
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs);
    }
```
可以看到参数里面有AttributeSet，我们可以通过AttributeSet筛选需要做处理的属性，记录view和对应的属性，然后在换肤时替换属性对应的资源，就可以达到换肤的目的了，
具体处理逻辑较为复杂，可以通过后面提供的源码查看

### SkinPeeler库
[库代码传送门](https://github.com/ray-tianfeng/skin-peeler)

SkinPeeler库是基于上面的原理完成的换皮库，使用方法：
1. 导入库

```Gradle
//root build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}

//app build.gradle
dependencies {
    implementation 'com.github.ray-tianfeng:skin-peeler:v1.0.0'
}
```

2. 使用

- **换肤 SkinPeeler.getInstance().skin(String skinPath);**  
  传入制作好的皮肤包，即可完成换肤

- **还原 SkinPeeler.getInstance().restore();**  
  不使用皮肤
- **换肤监听 SkinPeeler.getInstance().addSkinChangeListener(Activity
  mActivity, SkinPeeler.OnSkinChangeListener mOnSkinChangeListener);**  
  皮肤切换监听，完成换皮时回调
- **自定义属性适配器**

     1. 实现[BaseAttrADT.java](skinpeeler/src/main/java/com/zlong/skinpeeler/adt/BaseAttrADT.java)
     ```java
    //支持的属性集合,例如：background、src、textColor
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
   ```

  2. 添加属适配器至管理器**SkinPeeler.getInstance().addAttrADT(BaseAttrADT
     attrADT)**
  3. 常用工具类  
     **IdUtils**：资源Id查找工具类，通过`IdUtils.findResById(int
     id)`,查找原包中id对应的类型、名称
- **自定义属性注意事项**

  1. ID  
     原包中R.xx.xx对应的资源id不可在皮肤包中使用，必须使用皮肤包中对应资源的id，因为原包中的资源对应的id，和皮肤包中同一资源对应的id不同
  2. 资源查找  
     applySkin提供了皮肤包的Resources，那我们可以通过皮肤包资源id获取对应的资源，
     我们把原包中的资源id通过`IdUtils.findResById`查找资源对应的名称和类型，然后通过`Resources.getIdentifier(String
     name, String defType, String
     defPackage)`查找资源在皮肤包中对应的id，最后获取资源就行了

通过第二步我们可以得到资源的id，但是我们不能直接把皮肤包的资源id直接设置到view上，因为原皮肤对应的Resources，肯定没有皮肤包对应的资源id。  
在代码中也不能直接设置资源id，因为换肤后，直接设置资源id，系统直接通过原始Resources查找的资源。需要通过上面的资源查找，直接查找对应的资源，设置到对应的view上  
库内置了[AutoAttrADT.java](skinpeeler/src/main/java/com/zlong/skinpeeler/adt/AutoAttrADT.java)可以对照着来实现自定义属性
- **实现属性**  
  库已经通过[AutoAttrADT.java](skinpeeler/src/main/java/com/zlong/skinpeeler/adt/AutoAttrADT.java)实现了常用属性的适配  
  background、src、textColor、drawableLeft、drawableTop、drawableRight、drawableBottom

- **库使用注意事项**
  - 需要文件读取权限，如果在6.0及以上，需要做权限处理
  - 包名只能是androidManifest中的packageName，不能在gradle使用applicationId，因为IdUtils通过包名查找R类的。

### 扩展1
通过原理我们了解到我们是通过hook
LayoutInflater的Factory2，实现了Factory2的方法后，我们主要做的就是将view和需要处理的属性记录下来。
实际上activity也有onCreateView，我们可以通过重写onCreateView的方式去实现hook的功能，这样能减少因为hook带来的风险。
上面的库为什么没有重写呢，因为减少接入带来的变化。如果自己去实现换肤的功能，可以直接使用activity的onCreateView做一个基类

### 扩展2
在上面的实现过程中有使用到AttributeSet，这个就是当前view的属性集合，我们是不是可以自定义一个属性（圆角背景）。然后在onCreateView解析到此属性时，
通过java代码创建一个drawable，设置给view，注意此处自定义的属性只能在xml中使用，因为View不包含这个自定义的属性的。