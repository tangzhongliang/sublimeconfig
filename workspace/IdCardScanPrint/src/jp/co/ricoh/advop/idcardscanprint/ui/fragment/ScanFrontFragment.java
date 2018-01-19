package jp.co.ricoh.advop.idcardscanprint.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Date;


import jp.co.ricoh.advop.cheetahutil.util.HDDUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;
import jp.co.ricoh.advop.idcardscanprint.model.GlobalDataManager.FragmentState;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanStateMachine;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.IdCardScanPrintActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.InitialSettingScanActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.view.SettingShowView;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

public class ScanFrontFragment extends Fragment {
    private SettingShowView setting;
    private Activity ctx;
    private BaseOnClickListener startListener, settingListener;
    private RelativeLayout description;
    private SettingShowView settingShowView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_scan_front, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ctx = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        setting = (SettingShowView) ctx.findViewById(R.id.layout_step1_property);
        description = (RelativeLayout) ctx.findViewById(R.id.img_step1_description);
        settingShowView = (SettingShowView) ctx.findViewById(R.id.layout_step1_property);
        
        description.setBackgroundResource(Util.initializeSupportedAms(FragmentState.ScanSide1));
        
        startListener = new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                LogC.d("start scan front page!");
                if(!CHolder.instance().getApplication().getSystemStateMonitor().hasScanPermission()) {
                    MultiButtonDialog msgDlg = MultiButtonDialog.createMsgDialog(getActivity(), R.string.error_permission_denied);
                    msgDlg.show();
                    return;
                }
                HDDUtil.deleteFile(Const.SCAN_FILE_PATH);
                CHolder.instance().getJobManager().startScanFront(new ExeCallback() {
                    
                    @Override
                    public void onComplete(EXE_RESULT ret) { 
                        //CHolder.instance().getScanManager().getStateMachine().closeScanningDialog();
                        CHolder.instance().getScanManager().getStateMachine().getState().actCloseScanningDialog(CHolder.instance().getScanManager().getStateMachine(), null);
                        if (ret == EXE_RESULT.SUCCESSED) {
                            LogC.d("scan front file success! ");
                            IdCardScanPrintActivity parActivity = (IdCardScanPrintActivity) getActivity();
                            if(parActivity != null) {
                                parActivity.goToNextScan();
                            }
                        } else if(ret == EXE_RESULT.ERR_COPY_FILE || ret == EXE_RESULT.FAILED){
                            LogC.e("Scan file failed!");
                            if(getActivity() != null) {
                                MultiButtonDialog msgDlg = MultiButtonDialog.createMsgDialog(getActivity(), R.string.scan_fail);
                                msgDlg.show();
                            }
                        }
                        
                    }
                });
                

            }
        };

        settingListener = new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                settingShowView.setPropertySelect(true);
                Intent intent = new Intent(ctx, InitialSettingScanActivity.class);
                if(ctx != null) {
                    ctx.startActivity(intent);
                }
                //ctx.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                
            }
        };
       setting.setStartBtnListener(startListener, settingListener);
    }
    
    @Override
    public void onResume() {
       
        super.onResume();
        settingShowView.setScanSetting();
        settingShowView.setPropertySelect(false);
    }
}
