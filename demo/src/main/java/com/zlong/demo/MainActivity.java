package com.zlong.demo;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.zlong.skinpeeler.SkinPeeler;
import com.zlong.skinpeeler.utils.IdUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SkinPeeler.getInstance().addSkinChangeListener(this, new SkinPeeler.OnSkinChangeListener() {
            @Override
            public void onSkinChanged() {
               setStatus();
            }
        });
        setStatus();
    }

    public void setStatus(){
        String packageName =  SkinPeeler.getInstance().getSkinPackageName();

        IdUtils.ReID oldStatusColorResID = IdUtils.findResById(R.color.statu_color);
        int statusColorResId = SkinPeeler.getInstance().getSkinResources().getIdentifier(oldStatusColorResID.getName(), oldStatusColorResID.getType(), packageName);
        int statusColor = SkinPeeler.getInstance().getSkinResources().getColor(statusColorResId);

        IdUtils.ReID statusTypeReID = IdUtils.findResById(R.string.statu_type);
        int statusTypeResId = SkinPeeler.getInstance().getSkinResources().getIdentifier(statusTypeReID.getName(), statusTypeReID.getType(), packageName);
        String statusType = SkinPeeler.getInstance().getSkinResources().getString(statusTypeResId);
        StatusBarUtil.setStatusBarMode(MainActivity.this, statusType.equals("black"), statusColor);
    }


    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId){
            case R.id.black_skin:
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/test-skin1-debug.skin";
                SkinPeeler.getInstance().skin(path);
                break;
            case R.id.default_skin:
                SkinPeeler.getInstance().restore();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}