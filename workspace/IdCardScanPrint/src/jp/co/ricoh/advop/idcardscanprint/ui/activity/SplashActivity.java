
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


import jp.co.ricoh.advop.cheetahutil.util.BaseDialogOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.widget.BaseDialog;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;

public class SplashActivity extends BaseActivity {
    //private TextView txtDescript;
    private MultiButtonDialog mBootFailedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        if (!onMainActCreate()) {
            return;
        }

        init();
    }    
    
    @Override
    public void onLanguageChanged() {
        ((TextView)findViewById(R.id.tv_title)).setText(R.string.splash_title);
        ((TextView)findViewById(R.id.tv_description)).setText(R.string.splash_description);
        ((TextView)findViewById(R.id.tv_start_msg)).setText(R.string.splash_app_start);
    }
    
    private void init() {
        LogC.i("init app");
        CHolder.instance().setRestarting(false);
        CHolder.instance().getJobManager().initApp(new ExeCallback() {
            private String errorString = "";

            @Override
            public void onComplete(EXE_RESULT ret) {

                if (ret == EXE_RESULT.FAILED) {
                    LogC.e("lock fail!");
                    errorString = "lock fail!";

                }

                if (ret == EXE_RESULT.ERR_HDD_NOT_AVAILABLE) {
                    LogC.e("HDD NOT AVAILABLE!");
                    errorString = "HDD NOT AVAILABLE!";
                }

                if (ret != EXE_RESULT.SUCCESSED) {
                    if (errorString.equalsIgnoreCase("")) {
                        errorString = "init fail!";
                    }
                    
                    if (ret == EXE_RESULT.ERR_RESTRICT_DEVICE_INSTALLED) {
                        mBootFailedDialog = MultiButtonDialog.createMsgDialog(
                                SplashActivity.this, R.string.txid_scmn_d_device_not_supported);
                        mBootFailedDialog.getRightButton().setOnClickListener(new BaseOnClickListener() {
                            @Override
                            public void onWork(View v) {
                                mBootFailedDialog.dismiss();
                                SplashActivity.this.finish();
                            }
                        });
                        mBootFailedDialog.show();
                        return;
                    }
                    
                    // Boot failed dialog
                    mBootFailedDialog = MultiButtonDialog.createMsgDialog(SplashActivity.this, CHolder.instance().getApplication().getString(R.string.txid_cmn_d_cannot_connect), new BaseDialogOnClickListener() {
                        
                        @Override
                        public void onWork(BaseDialog dialog) {
                            
                            mBootFailedDialog.dismiss();
                            SplashActivity.this.finish();                            
                        }
                    });
                    mBootFailedDialog.show();
//                    mBootFailedDialog.setLeftButtonVisible(View.GONE);
//                    mBootFailedDialog.setRightButtonVisible(View.GONE);
//                    mBootFailedDialog.setCenterButtonVisible(View.VISIBLE);
//                    mBootFailedDialog.setButtonCenter("ok");
//                    mBootFailedDialog.setTxtTitle(CHolder.instance().getApplication().getString(R.string.txid_cmn_b_error));
//                    mBootFailedDialog.setTxtMsg(CHolder.instance().getApplication().getString(R.string.txid_cmn_d_cannot_connect));
//                    mBootFailedDialog.show();
//                    mBootFailedDialog.setCenterButtonClick(new BaseOnClickListener() {
//
//                        @Override
//                        public void onWork(View v) {
//                            
//                            mBootFailedDialog.dismiss();
//                            SplashActivity.this.finish();
//
//                        }
//                    });
                    return;
                }

                Intent mainIntent = new Intent(SplashActivity.this, IdCardScanPrintActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                CHolder.instance().getGlobalDataManager().setsActivity(SplashActivity.this);
                // do not destroy main act
                // SplashActivity.this.finish();
            }
        });
    }

    /**
     * アクティビティの再開時に呼び出されます。 エラーの発生有無を非同期で検査し、必要であればシステム警告画面切替えます。
     * 本体の電力モードを非同期で確認し、必要であればシステム状態要求を通知します。 Called when the activity is
     * resumed. Checks error occurrence asynchronously and switches to a system
     * warning screen if necessary. Checks machine's power mode asynchronously
     * and requests system state if necessary.
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        if (!onMainResume()) {
            return;
        }
        
        if (CHolder.instance().isRestarting()) {
            LogC.d("restart");
            if (mBootFailedDialog != null && mBootFailedDialog.isShowing()) {
                mBootFailedDialog.dismiss();
                mBootFailedDialog = null;
            }
            init();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!onMainActDestroy()) {
            return;
        }
        
    }
}
