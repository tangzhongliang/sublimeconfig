package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import jp.co.ricoh.advop.cheetahutil.R;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class LicenseInfoActivity extends BaseActivity {

    private static final String TAG = LicenseInfoActivity.class.getSimpleName();

    ViewGroup mBtnBack;
    TextView mOssLicense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isCreateMenu = false;
        setEnableBack();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_oss_license);
//        ButterKnife.bind(this);

        mBtnBack = (ViewGroup)findViewById(R.id.btn_back);
        mOssLicense = (TextView)findViewById(R.id.oss_lisence_text);
        
        mBtnBack.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View view) {
                
                finish();
                overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);
            }
        });

        mOssLicense.setText(readLicenseText());
    }

    /**
     * OSSライセンステキストをAssetから読み込む
     * @return
     */
    private String readLicenseText(){
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("LICENSE.txt"), "UTF-8"));

            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }

        } catch (IOException e) {
            LogC.w(TAG, "", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogC.w(TAG, "", e);
                }
            }
        }

        return builder.toString();
    }
}
