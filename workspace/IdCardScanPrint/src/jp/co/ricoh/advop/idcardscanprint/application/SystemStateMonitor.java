/*
 *  Copyright (C) 2015 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.idcardscanprint.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;

/**
 * SDKServiceから通知されるシステム状態を監視するクラスです。
 * 下記の4種類を監視対象としています。
 *  1. 電力モード
 *  2. オフラインモード
 *  3. HDD利用可否状態
 *  4. ネットワーク接続状態
 * This class monitors system states notified from SDKService.
 * The following 4 types of states are monitored:
 *  1. Power mode
 *  2. Offline mode
 *  3. HDD availability state
 *  4. Network connection state
 */
public class SystemStateMonitor {

    /**
     * 電力モードの定義
     *  0: 通常待機状態
     *  1: 低電力状態
     *  2: 待機定着OFF状態
     *  3: 静音状態
     *  4: エンジンOFF状態、BASIC OFF状態(スリープモード)
     *  5: コントローラSTR状態(スリープモード)
     * -1: 状態不明(通知受信前)
     * Power mode definition
     *  0: Normal standby state
     *  1: Low-power state
     *  2: Fusing unit off state
     *  3: Silent state
     *  4: Engine off state, basic off state (sleep mode)
     *  5: Off mode (sleep mode)
     * -1: State unknown (Before notified)
     */
    public static final int POWER_MODE_NORMAL_STANDBY = 0;
    public static final int POWER_MODE_LOW_POWER = 1;
    public static final int POWER_MODE_FUSING_UNIT_OFF = 2;
    public static final int POWER_MODE_SILENT = 3;
    public static final int POWER_MODE_ENGINE_OFF = 4;
    public static final int POWER_MODE_OFF_MODE = 5;
    public static final int POWER_MODE_UNKNOWN = -1;

    /**
     * オフラインモードの定義
     *  0: オンライン
     *  1: オフライン
     *  2: 条件付オフライン
     *  3: コントローラリブートを伴うオフライン
     * -1: 状態不明(通知受信前)
     * Offline mode definition
     *  0: Online
     *  1: Offline
     *  2: Offline with condition
     *  3: Offline with controller reboot
     * -1: State unknown (Before notified)
     */
    public static final int OFFLINE_MODE_ONLINE = 0;
    public static final int OFFLINE_MODE_OFFLINE = 1;
    public static final int OFFLINE_MODE_OFFLINE_WITH_CONDITION = 2;
    public static final int OFFLINE_MODE_OFFLINE_WITH_CONTROLLER_REBOOT = 3;
    public static final int OFFLINE_MODE_UNKNOWN = -1;

    /**
     * HDD利用可否状態の定義
     *  0: 利用不可能
     *  1: 利用可能
     * -1: 状態不明(通知受信前)
     * HDD availability state definition
     *  0: Unavailable
     *  1: Available
     * -1: State unknown (Before notified)
     */
    public static final int HDD_STATE_AVAILABLE = 1;
    public static final int HDD_STATE_UNAVAILABLE = 0;
    public static final int HDD_STATE_UNKNOWN = -1;

    /**
     * ネットワーク接続状態の定義
     *  0: 切断
     *  1: 接続
     * -1: 状態不明(通知受信前)
     * Network connection state definition
     *  0: Disconnected
     *  1: Connected
     * -1: State unknown (Before notified)
     */
    public static final int NETWORK_STATE_CONNECTED = 1;
    public static final int NETWORK_STATE_DISCONNECTED = 0;
    public static final int NETWORK_STATE_UNKNOWN = -1;


    /**
     * 受信アクション: 電力モード通知
     * Receive action: Power Mode Notification
     */
    private static final String ACTION_POWER_MODE_RESULT = "jp.co.ricoh.isdk.sdkservice.system.PowerMode.POWER_MODE_RESULT";
    /**
     * 受信アクション: オフラインモード通知
     * Receive action: Offline Mode Notification
     */
    private static final String ACTION_OFFLINE_RESULT = "jp.co.ricoh.isdk.sdkservice.system.OfflineManager.OFFLINE_RESULT";
    /**
     * 受信アクション: HDD利用可否通知
     * Receive action: HDD Availability Notification
     */
    private static final String ACTION_CTL_HDD_AVAILABILITY = "jp.co.ricoh.isdk.sdkservice.system.Hdd.CTL_HDD_AVAILABILITY";
    private static final String ACTION_GET_CTL_HDD_AVAILABILITY = "jp.co.ricoh.isdk.sdkservice.system.Hdd.GET_CTL_HDD_AVAILABILITY";
    /**
     * 受信アクション: ネットワーク接続通知
     * Receive action: Network Connection Notification
     */
    private static final String ACTION_NETWORK_CONNECTED = "jp.co.ricoh.isdk.sdkservice.system.SystemManager.NETWORK_CONNECTED";
    
    private static final String ACTION_AUTH_STATE_CHANGED = "jp.co.ricoh.isdk.sdkservice.auth.SYS_NOTIFY_AUTH_STATE_CHANGED";
    private static final String ACTION_PANEL_STATE_RESULT = "jp.co.ricoh.isdk.sdkservice.panel.PANEL_STATE_RESULT";
    private static final String ACTION_AUTH_SETTING_CHANGED_DETAIL = "jp.co.ricoh.isdk.sdkservice.auth.SYS_NOTIFY_AUTH_SETTING_CHANGED_DETAIL";

    private Context mContext;
    private SystemStateReceiver mReceiver;
    private boolean mRunning = false;

    private volatile int mPowerMode = POWER_MODE_UNKNOWN;
    private volatile int mOfflineMode = OFFLINE_MODE_UNKNOWN;
    private volatile int mHddAvailable = HDD_STATE_UNKNOWN;
    private volatile int mNetworkConnected = NETWORK_STATE_UNKNOWN;

    private boolean isMachineAdmin = false;
    private String loginUserName;
    boolean isLogin = false;
    private boolean hasScanPermission = true;
    private boolean isAdminAuthON = true;
    
    
    public SystemStateMonitor(Context context) {
        this.mContext = context;
        this.mReceiver = new SystemStateReceiver();
    }

    /**
     * システム状態の監視を開始します。
     * Starts monitoring system states.
     * @return 監視開始した場合にtrue
     *         "true" when monitoring starts.
     */
    public boolean start() {
        if (mRunning) {
            return false;
        }
        mRunning = true;
        
        // reg receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_POWER_MODE_RESULT);
        filter.addAction(ACTION_OFFLINE_RESULT);
        filter.addAction(ACTION_CTL_HDD_AVAILABILITY);
        filter.addAction(ACTION_NETWORK_CONNECTED);
        filter.addAction(ACTION_AUTH_STATE_CHANGED);
        filter.addAction(ACTION_PANEL_STATE_RESULT);
        filter.addAction(ACTION_AUTH_SETTING_CHANGED_DETAIL);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        mContext.registerReceiver(mReceiver, filter, "jp.co.ricoh.isdk.sdkservice.common.SdkService.APP_EVENT_PERMISSION", null);
        
        // get hdd state
        Intent intent = new Intent();
        intent.setAction(ACTION_GET_CTL_HDD_AVAILABILITY);
//        intent.putExtra("PACKAGE_NAME", mContext.getPackageName());
        mContext.sendBroadcast(intent); 

        return true;
    }

    /**
     * システム状態の監視を終了します。
     * Ends monitoring system states.
     * @return 監視終了した場合にtrue
     *         "true" when monitoring ends.
     */
    public boolean stop() {
        if (!mRunning) {
            return false;
        }
        mRunning = false;

        mContext.unregisterReceiver(mReceiver);
        return true;
    }

    public boolean isRunning() {
        return mRunning;
    }


    /**
     * 現在の電力モードを取得します。
     * Obtains the current power mode.
     * @return 電力モード(POWER_MODE_xxxx)
     *         Power mode (POWER_MODE_xxxx)
     */
    public int getPowerMode() {
        return mPowerMode;
    }

    /**
     * 現在のオフラインモードを取得します。
     * Obtains the current offline mode.
     * @return オフラインモード(OFFLINE_MODE_xxxx)
     *         Offline mode (OFFLINE_MODE_xxxx)
     */
    public int getOfflineMode() {
        return mOfflineMode;
    }

    /**
     * 現在のHDD利用可否状態を取得します。
     * Obtains the current HDD availability state.
     * @return HDD利用可否状態(HDD_STATE_xxxx)
     *         HDD availability state (HDD_STATE_xxxx)
     */
    public int getHddAvailable() {
        return mHddAvailable;
    }

    /**
     * 現在のネットワーク接続状態を取得します。
     * Obtains the current network connection state.
     * @return ネットワーク接続状態(NETWORK_STATE_xxxx)
     *         Network connection state (NETWORK_STATE_xxxx)
     */
    public int getNetworkConnected() {
        return mNetworkConnected;
    }
    
    public synchronized void setLoginUser(Bundle extras) {
        if (extras != null) {
            isLogin = extras.getBoolean(jp.co.ricoh.isdk.sdkservice.auth.intent.Constants.EXTRA_LOGIN_STATUS);
            if (isLogin) {
                // login
                loginUserName = extras.getString(jp.co.ricoh.isdk.sdkservice.auth.intent.Constants.EXTRA_USER_NAME);

                String admin = extras.getString(jp.co.ricoh.isdk.sdkservice.auth.intent.Constants.EXTRA_MACHINE_ADMIN_PERMISSION);
                if ("all".equals(admin)) {
                    isMachineAdmin = true;
                } else {
                    isMachineAdmin = false;
                }
                
                hasScanPermission = extras.getBoolean(jp.co.ricoh.isdk.sdkservice.auth.intent.Constants.EXTRA_IS_SCANNER_AVAILABLE, true);
             } else {
                // logout
                loginUserName = "";
                isMachineAdmin = false;
                
                hasScanPermission = true;
            }
        }
    }

    /**
     * SDKServiceからの通知を受信するためのブロードキャストレシーバです。
     * A broadcast receiver to receive notifications from SDKService.
     */
    private class SystemStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_POWER_MODE_RESULT.equals(action)) {
                receivePowerModeResult(intent);
            } else if (ACTION_OFFLINE_RESULT.equals(action)) {
                receiveOfflineResult(intent);
            } else if (ACTION_CTL_HDD_AVAILABILITY.equals(action)) {
                receiveCtlHddAvailability(intent);
            } else if (ACTION_NETWORK_CONNECTED.equals(action)) {
                receiveNetworkConnected(intent);
            } else if (ACTION_AUTH_STATE_CHANGED.equals(action)) {
                String loginUserNameTmp = intent.getExtras().getString(jp.co.ricoh.isdk.sdkservice.auth.intent.Constants.EXTRA_USER_NAME);
                LogC.d("------------------ACTION_AUTH_STATE_CHANGED------" + loginUserNameTmp);
                
                // finish act
                if(loginUserNameTmp == null || !loginUserNameTmp.equals(loginUserName)) {     
                    CHolder.instance().restartApp();
                }
                setLoginUser(intent.getExtras());
            } else if (ACTION_PANEL_STATE_RESULT.endsWith(action)) {
                LogC.d("------------------ACTION_PANEL_STATE_RESULT------");
                int temp = intent.getExtras().getInt(jp.co.ricoh.isdk.sdkservice.panel.intent.Constants.EXTRA_PANEL_STATE, 0);
                if (temp != 0) { // panel state != on(0)
                    CHolder.instance().restartApp();
                }
            } else if (ACTION_AUTH_SETTING_CHANGED_DETAIL.endsWith(action)) {
                LogC.d("------------------ACTION_AUTH_SETTING_CHANGED_DETAIL------");
                boolean temp = intent.getExtras().getBoolean(jp.co.ricoh.isdk.sdkservice.auth.intent.Constants.EXTRA_MACHINE_ADMIN_AUTH, true);
                if (temp != isAdminAuthON) {
                    CHolder.instance().restartApp();
                }
                setAdminAuthON(intent.getExtras());
            } else if (Intent.ACTION_LOCALE_CHANGED.endsWith(action)) {
                CHolder.instance().restartApp4Language();
            }
        }

        private void receivePowerModeResult(Intent intent) {
            mPowerMode = intent.getIntExtra("POWER_MODE", POWER_MODE_UNKNOWN);
        }

        private void receiveOfflineResult(Intent intent) {
            // on->off restart
            int newMode = intent.getIntExtra("OFFLINE_MODE", OFFLINE_MODE_UNKNOWN);
            LogC.d(String.format("OFFLINE MODE: %d - > %d", mOfflineMode, newMode));
            if (mOfflineMode == OFFLINE_MODE_ONLINE && newMode != OFFLINE_MODE_ONLINE) {
                CHolder.instance().restartApp();
            }
            mOfflineMode = newMode;
        }

        private void receiveCtlHddAvailability(Intent intent) {
            boolean hddAvailable = intent.getBooleanExtra("HDD_AVAILABLE", false);
            if (hddAvailable) {
                mHddAvailable = 1;
            } else {
                mHddAvailable = 0;
            }
        }

        private void receiveNetworkConnected(Intent intent) {
            boolean connected = intent.getBooleanExtra("CONNECTED", false);
            if (connected) {
                mNetworkConnected = 1;
            } else {
                mNetworkConnected = 0;
            }
        }

    }


    public boolean isMachineAdmin() {
        return isMachineAdmin;
    }
    
    public boolean isLogin() {
        return isLogin;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public boolean hasScanPermission() {
        return hasScanPermission;
    }
    public boolean isAdminAuthON() {
        return isAdminAuthON;
    }
    
    public boolean needShowSetting() {
        if (!isAdminAuthON()) {
            // 機器管理者設定無効の場合、Setting有効
            return true;
        }
        return isMachineAdmin();
    }

    public synchronized void setAdminAuthON(Bundle extras) {
        this.isAdminAuthON = extras.getBoolean(jp.co.ricoh.isdk.sdkservice.auth.intent.Constants.EXTRA_MACHINE_ADMIN_AUTH, true);
    }

}
