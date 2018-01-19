package jp.co.ricoh.advop.idcardscanprint.logic;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import dalvik.system.DexClassLoader;
import jp.co.ricoh.advop.cheetahutil.util.CUIUtil;
import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.application.MachineStatus;
import jp.co.ricoh.advop.idcardscanprint.model.GlobalDataManager;
import jp.co.ricoh.advop.idcardscanprint.print.PrintManager;
import jp.co.ricoh.advop.idcardscanprint.print.PrintStateMachine.PrintEvent;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanManager;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanStateMachine.ScanEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SplashActivity;

import java.util.ArrayList;

public class CHolder {

    private static CHolder instance;
    private JobManager jobManager;
    private ScanManager scanManager;
    private PrintManager printManager;
    private CheetahApplication application;
    private Handler mainUIHandler;
    private GlobalDataManager globalDataManager;
    private MachineStatus machineStatusData;
    private LockManager lockManager;
    private JobData jobData;
    private boolean isForeground;
    private Activity activity;
    private ArrayList<Entry> entries;
    private Entry[] linkedSelects;
    private Entry[] selects;
    private DexClassLoader classLoader;
    private boolean isRestarting;
    private Entry lastInputEntry;
    private boolean isRestarting4Language;


    public CHolder(CheetahApplication app) {
        // do not modify this function!
        // do init work in init()
        application = app;
        instance = this;
        isRestarting = false;
        isRestarting4Language = false;
    }
    
    public void init() {
        mainUIHandler = new Handler();
        jobManager = new JobManager();
        scanManager = new ScanManager();
        printManager = new PrintManager();
        globalDataManager = new GlobalDataManager();
        machineStatusData = new MachineStatus(application);
        lockManager = new LockManager();
        jobData = new JobData();
        isForeground = true;
        classLoader = null;
    }
    
    
    public MachineStatus getMachineStatusData() {
        return machineStatusData;
    }
    public static CHolder instance() {
        return instance;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public CheetahApplication getApplication() {
        return application;
    }

    public Handler getMainUIHandler() {
        return mainUIHandler;
    }

    public ScanManager getScanManager() {
        return scanManager;
    }

    public PrintManager getPrintManager() {
        return printManager;
    }
    
    public GlobalDataManager getGlobalDataManager() {
        return globalDataManager;
    }

    public LockManager getLockManager() {
        return lockManager;
    }

    public JobData getJobData() {
        return jobData;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean isForeground) {
        this.isForeground = isForeground;
        if(isForeground) {
            CUtil.setPrelongTime(0);
        }
    }
    
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    public Entry[] getLinkedSelects() {
        return linkedSelects;
    }

    public void setLinkedSelects(Entry[] linkedSelects) {
        this.linkedSelects = linkedSelects;
        setSelects(linkedSelects);
    }

    public Entry[] getSelects() {
        return selects;
    }

    public DexClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(DexClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private void setSelects(Entry[] linkedSelects) {
        if (linkedSelects == null) {
            this.selects = null;
            return;
        }
        Entry[] tmpSelects = new Entry[linkedSelects.length];
        System.arraycopy(linkedSelects, 0, tmpSelects, 0, linkedSelects.length);
        int count = 0;
        for (int i = 0; i < tmpSelects.length; i++) {
            if (tmpSelects[i] != null) {
                tmpSelects[count] = tmpSelects[i];
                count++;
            }
        }
        this.selects = new Entry[count];
        System.arraycopy(tmpSelects, 0, this.selects, 0, count);
    }
    
    public void restartApp() {
        LogC.i("restart app");
        isRestarting = true;
        if (CUIUtil.isForegroundApp(getApplication())) {
            LogC.i("APP Foreground: not cancel app");
            return;
        }
        jobManager.cancelInitTask();
    }
    
    public void restartApp4Language() {
        LogC.i("restartApp4Language");
        if (getJobData().getCurrentJob() != null) {
            LogC.i("job is running, restartApp4Language wait");
            isRestarting4Language = true;
            if (CHolder.instance().getScanManager().isJobRunning()) {
                CHolder.instance().getScanManager().getStateMachine()
                        .procScanEvent(ScanEvent.REQUEST_JOB_CANCEL);
            }
            if (CHolder.instance().getPrintManager().isJobRunning()) {
                CHolder.instance().getPrintManager().getStateMachine()
                        .procPrintEvent(PrintEvent.REQUEST_JOB_CANCEL);
            }
        } else {
            restartApp();
        }
        
    }
    
    public void restartAppWhenJobEnd() {

        if(!isRestarting4Language) {
            return;
        }
        isRestarting4Language = false;
        if (!CUIUtil.isForegroundApp(getApplication())) {
            restartApp();
            return;
        }
        
        // restart when Foreground
        mainUIHandler.post(new Runnable() {
            @Override
            public void run() {
                isRestarting = true;
                if (!(getActivity() instanceof SplashActivity)) {
                    LogC.d("restart FLAG_ACTIVITY_CLEAR_TOP");
                    Intent intentFinish = new Intent(getActivity(), SplashActivity.class);
                    intentFinish.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(intentFinish);
                }
//                MultiButtonDialog.createMsgDialog(getActivity(), "Language Changed! \n Click [OK] to restart app!", new BaseDialogOnClickListener() {
//                    
//                    @Override
//                    public void onWork(BaseDialog dialog) {
//                        dialog.dismiss();
//                        isRestarting = true;
//                        if (!(getActivity() instanceof SplashActivity)) {
//                            LogC.d("restart FLAG_ACTIVITY_CLEAR_TOP");
//                            Intent intentFinish = new Intent(getActivity(), SplashActivity.class);
//                            intentFinish.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            getActivity().startActivity(intentFinish);
//                        }
//                    }
//                }).show();
            }
        });
    }

    public boolean isRestarting4Language() {
        return isRestarting4Language;
    }

    public boolean isRestarting() {
        return isRestarting;
    }

    public void setRestarting(boolean isRestarting) {
        this.isRestarting = isRestarting;
    }

    public Entry getLastInputEntry() {
        return lastInputEntry;
    }

    public void setLastInputEntry(Entry lastInputEntry) {
        this.lastInputEntry = lastInputEntry;
    }
    
    
}
