package jp.co.ricoh.advop.idcardscanprint.logic;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.application.AfterPowerModeLock;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;


public class LockManager {
    private final static String TAG = LockManager.class.getSimpleName();
    private CheetahApplication application;
    private boolean isLockSysReset;
//    private AfterPowerModeLock mAfterPowerModeLock;
    
    
    public LockManager() {
        application = CHolder.instance().getApplication();
        isLockSysReset = false;
//        mAfterPowerModeLock = null;
    }
    
    // async
    public synchronized boolean lock() {
        
        // lock logout
        if (!application.lockLogout()) {
            LogC.w(TAG, "lockLogout failed. ");
            return false;
        }
        
        // lock power mode
        if (!application.lockPowerMode()) {
            LogC.w(TAG, "lockPowerMode failed. ignore");
//            mAfterPowerModeLock = new AfterPowerModeLock();
//            mAfterPowerModeLock.start();
        }
        
        application.lockPanelOff();
        
        // lock offline
        if (!application.lockOffline()) {
            LogC.w(TAG, "lockOffline failed. ");
            return false;
        }
        
        // lock system reset
        application.lockSystemReset();
        isLockSysReset = true;
        
        return true;
    }
    
    public synchronized void unlock() {
//        if (mAfterPowerModeLock != null) {
//            mAfterPowerModeLock.finish();
//            mAfterPowerModeLock = null;
//        }
        
        application.unlockPowerMode();
        application.unlockPanelOff();
        application.unlockOffline();
        application.unlockSystemReset();
        application.unlockLogout();
        isLockSysReset = false;
    }
    
    public synchronized void goBackground() {
        application.unlockSystemReset();
    }
    
    public synchronized void goForeground() {
        if (isLockSysReset) {
            application.lockSystemReset();
        }
    }
    
}
