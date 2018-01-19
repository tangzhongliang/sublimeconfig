
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import jp.co.ricoh.advop.cheetahutil.R;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.Buzzer;
import jp.co.ricoh.advop.cheetahutil.util.CUIUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.application.SystemStateMonitor;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.LockManager;
import jp.co.ricoh.advop.idcardscanprint.print.PrintManager;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanManager;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.common.SmartSDKApplication;


public class BaseActivity extends Activity {
    private final static String TAG = BaseActivity.class.getSimpleName();

    /**
     * 省エネ復帰要求タスク Return request from energy saving task
     */
    protected ReturnRequestFromEnergySavingTask mReturnRequestFromEnergySavingTask = null;

    protected ScanManager scanManager;
    protected PrintManager printManager;
    protected CheetahApplication application;
    
    private LockManager lockManager;
    private Handler handler;
    
    protected boolean isCreateMenu = false;
    private Configuration config;

    protected boolean isMainAct = false;
    /**
     * メインアクティビティ起動済みフラグ trueであれば、すでにMainActivityが起動済みです。 MainActivity running
     * flag If true, another Mainactivity instance is running.
     */
    private boolean mMultipleRunning = false;
    private boolean isEnable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scanManager = CHolder.instance().getScanManager();
        printManager = CHolder.instance().getPrintManager();
        application = CHolder.instance().getApplication();
        lockManager = CHolder.instance().getLockManager();
        handler = CHolder.instance().getMainUIHandler();
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        config = new Configuration(getResources().getConfiguration());
        isMainAct = false;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        if (!config.locale.equals(newConfig.locale)) {
//            LogC.i(String.format("language changed %s->%s", config.locale.toString(), newConfig.locale));
//            if (!isMainAct) {
//                // ignore language change
//                LogC.i("ignore language changed");
//                super.onConfigurationChanged(config);
//            } else {
//                super.onConfigurationChanged(newConfig);
//                onLanguageChanged();
//            }
//        } else {
//            super.onConfigurationChanged(newConfig);
//        }
        super.onConfigurationChanged(newConfig);
        if (!config.locale.equals(newConfig.locale)) {
            LogC.i(String.format("language changed %s->%s", config.locale.toString(), newConfig.locale));
            onLanguageChanged();
        }
        config = new Configuration(newConfig);
    }
    public void onLanguageChanged() {
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        CHolder.instance().setActivity(this);
        if (CHolder.instance().isRestarting()) {
            if (!(this instanceof SplashActivity)) {
                LogC.d("restart FLAG_ACTIVITY_CLEAR_TOP");
                Intent intentFinish = new Intent(this, SplashActivity.class);
                intentFinish.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(intentFinish);
            }
        }
         
        if (!CHolder.instance().isForeground() && CUIUtil.isForegroundApp(application)) {
            LogC.i("", "background -> foreground");

            CHolder.instance().setForeground(true);
            handler.post(new Runnable() {
                
                @Override
                public void run() {
                    scanManager.getScanSysAlertDisplay().showOnResume();
                    printManager.getPrintSysAlertDisplay().showOnResume();
                    lockManager.goForeground();
                }
            });
        }
    }


    
    @Override
    protected void onStop() {
        super.onStop();
        if (CHolder.instance().isForeground() && !CUIUtil.isForegroundApp(application)) {
            LogC.i("", "foreground -> background");

            CHolder.instance().setForeground(false);
            lockManager.goBackground();
        }
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy(); 
    }

    // only call in front of app's main act
    // if return false, stop remain process
    protected boolean onMainActCreate() {
        isMainAct = true;
        isCreateMenu = false;
        LogC.i("onMainActCreate");
        if (CUIUtil.getNumActivities(application) > 1) {
            LogC.i(TAG, "Another MainActivity instance is already running.");
            mMultipleRunning = true;
            finish();
            return false;
        }
        
        // set application state to Normal
        application.setAppState(SplashActivity.class.getName(),
                SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG,
                SmartSDKApplication.APP_TYPE_SCANNER);
        return true;
    }

    protected boolean onMainResume() {
        LogC.d("onMainResume");
        if (mMultipleRunning) {
            return false;
        }
        startReturnRequestFromEnergySavingTask();
        return true;
    }

    protected boolean onMainActDestroy() {
        LogC.i("onMainActDestroy");
        // if MainActivity another instance is already running, then exit without doing anything
        if (mMultipleRunning) {
            mMultipleRunning = false;
            return false;
        }

        CHolder.instance().getJobManager().destroyApp();
        stopReturnRequestFromEnergySavingTask();
        CHolder.instance().getLockManager().unlock();

        // set application state to Normal
        application.setAppState(SplashActivity.class.getName(),
                SmartSDKApplication.APP_STATE_NORMAL, SmartSDKApplication.APP_STATE_NORMAL_MSG,
                SmartSDKApplication.APP_TYPE_SCANNER);
        return true;
    }

    /**
     * 省エネ復帰要求タスクを開始します。 Starts the return request from energy saving task.
     */
    private void startReturnRequestFromEnergySavingTask() {
        if (mReturnRequestFromEnergySavingTask != null) {
            mReturnRequestFromEnergySavingTask.cancel(true);
        }
        mReturnRequestFromEnergySavingTask = new ReturnRequestFromEnergySavingTask();
        //mReturnRequestFromEnergySavingTask.execute();
        mReturnRequestFromEnergySavingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 省エネ復帰要求タスクをキャンセルします。 Stop the return request from energy saving task.
     */
    private void stopReturnRequestFromEnergySavingTask() {
        if (mReturnRequestFromEnergySavingTask != null) {
            mReturnRequestFromEnergySavingTask.cancel(true);
            mReturnRequestFromEnergySavingTask = null;
        }
    }

    /**
     * 機器本体の電力モードを確認し、必要な場合は省エネ復帰要求行う非同期タスクです。 このタスクでは復帰要求のみを行います。
     * 省エネから復帰したことを確認する場合は、電力モード通知を確認するようにしてください。 This is an asynchronous task
     * to check the machine's power mode and to request to recover from Energy
     * Saver mode if necessary. This task only requests to recover from Energy
     * Saver mode. To check if the machine has recovered from Energy Saver mode,
     * enable to check power mode notification.
     */
    class ReturnRequestFromEnergySavingTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean needsRequest;

            int powerMode = application.getSystemStateMonitor().getPowerMode();
            LogC.d(TAG, "getPowerMode=" + powerMode);

            switch (powerMode) {
                case SystemStateMonitor.POWER_MODE_ENGINE_OFF:
                case SystemStateMonitor.POWER_MODE_OFF_MODE:
                case SystemStateMonitor.POWER_MODE_UNKNOWN:
                    needsRequest = true;
                    break;
                default:
                    needsRequest = false;
                    break;
            }

            Boolean result = Boolean.FALSE;
            if (needsRequest) {
                result = application
                        .controllerStateRequest(SmartSDKApplication.REQUEST_CONTROLLER_STATE_FUSING_UNIT_OFF);
            }
            return result;
        }

    }
    
    // ---------------------------------hide keyboard-------------------------------------------------------
    private boolean isSetupkeyboardHideGlobal = false;
    protected void setupKeyboardHide(ViewGroup container) {
        EditText tmp = null;
        
        // find edittext
        for (int i = 0; i < ((ViewGroup) container).getChildCount(); i++) {
            View innerView = ((ViewGroup) container).getChildAt(i);
            if (innerView instanceof EditText) {
                tmp = (EditText)innerView;
                break;
            }
        }

        final EditText edText = tmp;
        if (edText != null) {
            if (!isSetupkeyboardHideGlobal) {
                isSetupkeyboardHideGlobal = true;
                setupkeyboardHideGlobal(findViewById(android.R.id.content));
            }
            container.setOnClickListener(new BaseOnClickListener() {
                
                @Override
                public void onWork(View v) {
                    
                    showSoftKeyboard(edText);
                }
            });
            
            // release touch guard of setupkeyboardHideGlobal()
            releaseOnTouch(container);
        }
    }
    private void releaseOnTouch(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(null);
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                releaseOnTouch(innerView);
            }
        }
    }
    
    // set click anywhere to hide keyboard
    private void setupkeyboardHideGlobal(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupkeyboardHideGlobal(innerView);
            }
        }
    }
    
    protected void showSoftKeyboard(EditText edText) {
        edText.requestFocus();
        InputMethodManager lManager = (InputMethodManager)BaseActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE); 
        lManager.showSoftInput(edText, edText.getInputType());
    }
    
    protected void hideSoftKeyboard() {
        if(this.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }
    
    public void setEnableBack(){
        isEnable = true;
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {    
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if(isEnable) {
                Buzzer.play();
                
            } else {
                Buzzer.onBuzzer(Buzzer.BUZZER_NACK);
                return true; 
            }
        } 
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isCreateMenu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_show_app_info) {
            

            Intent intent = new Intent(this, AppInfoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // 開発ガイドのとおり。

            startActivity(intent);
            overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);

            return true;
        }
        return false;
    }
    
    

}
