package test.instrumentation;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SplashActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EmmaInstrumentation extends Instrumentation implements FinishListener {

    private static final String TAG = "EmmaInstrumentation";
    private static final String BROADCAST_ACTION_COVERAGE = "jp.co.ricoh.advop.mt.COLLECT_COVERAGE";

    private static final boolean LOGD = true;

    private static final String DEFAULT_COVERAGE_FILE_PATH = "/mnt/hdd/coverage.ec";

    private final Bundle mResults = new Bundle();

    private Intent mIntent;

    private boolean mCoverage = true;

    private String mCoverageFilePath;
    
//    private static EmmaInstrumentation instance;

    /**
     * Constructor
     */
    public EmmaInstrumentation() {

    }
    public static class InstrumentedActivity extends SplashActivity {
        private FinishListener mListener;

        public void setFinishListener(FinishListener listener) {
            mListener = listener;
        }

        @Override
        public void finish() {
            if (LOGD)
                LogC.d(TAG + ".InstrumentedActivity", "finish()");
            super.finish();
            if (mListener != null) {
                mListener.onActivityFinished();
            }
        }

    }
    
    @Override
    public void onCreate(Bundle arguments) {
//        instance = this;
        if (LOGD)
            LogC.d(TAG, "onCreate(" + arguments + ")");
        super.onCreate(arguments);

        if (arguments != null) {
            mCoverage = getBooleanArgument(arguments, "coverage");
            mCoverageFilePath = arguments.getString("coverageFile");
        }

        mIntent = new Intent(getTargetContext(), InstrumentedActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        start();
    }

    @Override
    public void onStart() {
        if (LOGD)
            LogC.d(TAG, "onStart()");
        super.onStart();

        Looper.prepare();
        InstrumentedActivity activity = (InstrumentedActivity) startActivitySync(mIntent);
        activity.setFinishListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION_COVERAGE);
        activity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogC.e(TAG, "onActivityFinished() gen coverage");
                if (mCoverage) {
                    generateCoverageReport();
                }
            }
        }, intentFilter);
    }

    private boolean getBooleanArgument(Bundle arguments, String tag) {
        String tagString = arguments.getString(tag);
        return tagString != null && Boolean.parseBoolean(tagString);
    }

    private void generateCoverageReport() {
        if (LOGD)
            LogC.d(TAG, "generateCoverageReport()");

        java.io.File coverageFile = new java.io.File(getCoverageFilePath());

        // We may use this if we want to avoid refecltion and we include
        // emma.jar
        // RT.dumpCoverageData(coverageFile, false, false);

        // Use reflection to call emma dump coverage method, to avoid
        // always statically compiling against emma jar
        try {
            Class<?> emmaRTClass = Class.forName("com.vladium.emma.rt.RT");
            Method dumpCoverageMethod = emmaRTClass.getMethod(
                    "dumpCoverageData", coverageFile.getClass(), boolean.class,
                    boolean.class);
            dumpCoverageMethod.invoke(null, coverageFile, false, false);
        } catch (ClassNotFoundException e) {
            reportEmmaError("Is emma jar on classpath?", e);
        } catch (SecurityException e) {
            reportEmmaError(e);
        } catch (NoSuchMethodException e) {
            reportEmmaError(e);
        } catch (IllegalArgumentException e) {
            reportEmmaError(e);
        } catch (IllegalAccessException e) {
            reportEmmaError(e);
        } catch (InvocationTargetException e) {
            reportEmmaError(e);
        }
    }

    private String getCoverageFilePath() {
        if (mCoverageFilePath == null) {
            return DEFAULT_COVERAGE_FILE_PATH;
        } else {
            return mCoverageFilePath;
        }
    }

    private void reportEmmaError(Exception e) {
        reportEmmaError("", e);
    }

    private void reportEmmaError(String hint, Exception e) {
        String msg = "Failed to generate emma coverage. " + hint;
        LogC.e(TAG, msg, e);
        mResults.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\nError: "
                + msg);
    }

//    public static void  genReport() {
//        if (instance == null) {
//            Log.e("genReport", "no instance");
//            return;
//        }
//        if (LOGD)
//            Log.d(TAG, "onActivityFinished()");
//        if (instance.mCoverage) {
//            instance.generateCoverageReport();
//        }
//    }
    
    @Override
    public void onActivityFinished() {
        if (LOGD)
            LogC.e(TAG, "onActivityFinished() gen coverage");
        if (mCoverage) {
            generateCoverageReport();
        }
        finish(Activity.RESULT_OK, mResults);
    }
}

interface FinishListener {

    /**
     * Invoked when the Activity finishes.
     */
    void onActivityFinished();

}