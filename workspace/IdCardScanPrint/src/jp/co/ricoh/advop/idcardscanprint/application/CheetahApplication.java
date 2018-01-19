/*
 *  Copyright (C) 2013-2015 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.application;

import android.content.Intent;
import android.os.Bundle;

import jp.co.ricoh.advop.cheetahutil.impexport.ImExportManager;
import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.common.SmartSDKApplication;

/**
 * スキャンサンプルアプリのアプリケーションクラスです。
 * 設定情報を保持し、スキャンサービスとジョブの管理を行います。
 * Application class of Scan sample application.
 * Saves the setting information and manages scan service and job.
 */
public class CheetahApplication extends SmartSDKApplication {

    /**
     * システム状態監視
     * System state monitor
     */
    private SystemStateMonitor mSystemStateMonitor;
    private PreferencesUtil preferences;
    
    @Override
    public void onCreate() {
        LogC.init("IDSP");
        LogC.i("Application onCreate");
        super.onCreate();
        new CHolder(this).init();
        
        mSystemStateMonitor = new SystemStateMonitor(this);
        mSystemStateMonitor.start();
        CHolder.instance().getScanManager().onAppInit();
        CHolder.instance().getPrintManager().onAppInit();
        
        preferences = PreferencesUtil.getInstance();
        
        ImExportManager.setEventListener(new ImExportListenerImpl(this));
    }

    @Override
    public void onTerminate() {
        LogC.i("Application onTerminate");
        mSystemStateMonitor.stop();
        super.onTerminate();
        CHolder.instance().getScanManager().onAppDestroy();
        CHolder.instance().getPrintManager().onAppDestroy();
        mSystemStateMonitor = null;
    }
    
    /**
     * システム状態監視クラスのインスタンスを取得します。
     * Obtains the instance for the class to system state monitor.
     */
    public SystemStateMonitor getSystemStateMonitor() {
        return mSystemStateMonitor;
    }
    
    public boolean waitHDDStart() {
        long time = 5000;
        long interval = 100;
        while (mSystemStateMonitor.getHddAvailable() != SystemStateMonitor.HDD_STATE_AVAILABLE) {
            if (time <= 0) {
                LogC.w("wait HDD available timeout!");
                return false;
            }
            LogC.d("wait HDD available");
            CUtil.sleep(interval);
            time -= interval;
        }
        return true;
    }
    
    public PreferencesUtil getPreferences() {
        return preferences;
    }

    public Boolean lockLogout() {
        if (!mSystemStateMonitor.isLogin()) {
            LogC.d("no user login, ignore lockLogout");
            return true;
        }
        
        final String packageName = mApplicationContext.getPackageName();

        final Intent intent = new Intent();
        intent.setAction("jp.co.ricoh.isdk.sdkservice.auth.LOCK_LOGOUT");
        intent.putExtra("PACKAGE_NAME", packageName);

        LogC.d("lockLogout() PACKAGE_NAME=" + packageName);

        final Bundle resultExtra = syncExecSendOrderedBroadCast(intent);
        if (resultExtra == null) {
            LogC.e("lockLogout request : No response.(timeout)");
            return false;
        }
        return (Boolean) resultExtra.get("RESULT");
    }

    public Boolean unlockLogout() {
        final String packageName = mApplicationContext.getPackageName();

        final Intent intent = new Intent();
        intent.setAction("jp.co.ricoh.isdk.sdkservice.auth.UNLOCK_LOGOUT");
        intent.putExtra("PACKAGE_NAME", packageName);

        LogC.d("unlockLogout() PACKAGE_NAME=" + packageName);

        mApplicationContext.sendBroadcast(intent, APP_CMD_PERMISSION);
        return true;
    }
    
    public Boolean lockPanelOff() {
        final String packageName = mApplicationContext.getPackageName();

        final Intent intent = new Intent();
        intent.setAction("jp.co.ricoh.isdk.sdkservice.system.PowerMode.LOCK_PANEL_OFF");
        intent.putExtra("PACKAGE_NAME", packageName);

        LogC.d("lockPanelOff() PACKAGE_NAME=" + packageName);

        final Bundle resultExtra = syncExecSendOrderedBroadCast(intent);
        if (resultExtra == null) {
            LogC.e("lockPanelOff request : No response.(timeout)");
            return false;
        }
        return (Boolean) resultExtra.get("RESULT");
    }

    public Boolean unlockPanelOff() {
        final String packageName = mApplicationContext.getPackageName();

        final Intent intent = new Intent();
        intent.setAction("jp.co.ricoh.isdk.sdkservice.system.PowerMode.UNLOCK_PANEL_OFF");
        intent.putExtra("PACKAGE_NAME", packageName);

        LogC.d("unlockPanelOff() PACKAGE_NAME=" + packageName);

        mApplicationContext.sendBroadcast(intent, APP_CMD_PERMISSION);
        return true;
    }
    
    // async
    public boolean initLoginUserInfo() {
        boolean ret = false;
        
        // login setting
        final Intent intentLogin = new Intent();
        intentLogin.setAction("jp.co.ricoh.isdk.sdkservice.auth.GET_AUTH_STATE");

        LogC.d("getLoginUserInfo()");

        final Bundle resultExtraLogin = syncExecSendOrderedBroadCast(intentLogin);
        if (resultExtraLogin == null) {
            LogC.e("getLoginUserInfo request : No response.(timeout)");
            return false;
        }
        mSystemStateMonitor.setLoginUser(resultExtraLogin);
        ret = (Boolean) resultExtraLogin.get("RESULT");
        if (!ret) {
            return ret;
        }
        
        // admin auth setting
        final Intent intent = new Intent();
        intent.setAction("jp.co.ricoh.isdk.sdkservice.auth.GET_AUTH_SETTING_DETAIL");

        LogC.d("getLoginUserInfo() GET_AUTH_SETTING_DETAIL");

        final Bundle resultExtra = syncExecSendOrderedBroadCast(intent);
        if (resultExtra == null) {
            LogC.e("getLoginUserInfo GET_AUTH_SETTING_DETAIL request : No response.(timeout)");
            return false;
        }
        mSystemStateMonitor.setAdminAuthON(resultExtra);
        return (Boolean) resultExtra.get("RESULT");
    }
}
