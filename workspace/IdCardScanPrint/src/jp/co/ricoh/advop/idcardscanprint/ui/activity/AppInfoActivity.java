package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import jp.co.ricoh.advop.cheetahutil.R;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;

public class AppInfoActivity extends BaseActivity {

    private static final String TAG = AppInfoActivity.class.getSimpleName();

    ViewGroup mBtnBack;
    ViewGroup mBtnShowOssLicense;
    TextView mBtnAppVersionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isCreateMenu = false;
        setEnableBack();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_app_info);
//        ButterKnife.bind(this);
        

        mBtnBack = (ViewGroup)findViewById(R.id.btn_back);
        mBtnShowOssLicense = (ViewGroup)findViewById(R.id.btn_show_oss_license);
        mBtnAppVersionText = (TextView)findViewById(R.id.btn_app_version_text);

        String appVersion = null;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo("jp.co.ricoh.advop.idcardscanprint", PackageManager.GET_META_DATA);
            appVersion = packageInfo.versionName.split("_")[1];
        } catch (PackageManager.NameNotFoundException e){
            LogC.w(TAG, "", e);
        }

        mBtnAppVersionText.setText((appVersion!=null) ? appVersion : "");

        mBtnBack.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View view) {
                
                finish();
                overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);
            }
        });

        mBtnShowOssLicense.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View view) {
                
                startLicenseInfoActivity();
            }
        });
    }

    private void startLicenseInfoActivity(){
        Intent intent = new Intent(this, LicenseInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // 開発ガイドのとおり。
        startActivity(intent);
        overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
    }
}
