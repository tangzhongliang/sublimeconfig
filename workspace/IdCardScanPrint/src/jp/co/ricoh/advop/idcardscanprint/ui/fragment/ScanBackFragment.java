package jp.co.ricoh.advop.idcardscanprint.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;


import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;
import jp.co.ricoh.advop.idcardscanprint.model.GlobalDataManager.FragmentState;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.IdCardScanPrintActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.view.SettingShowView;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

public class ScanBackFragment extends Fragment {
    Activity ctx;
    private SettingShowView setting;
    private RelativeLayout description;
    private BaseOnClickListener startListener;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_back, container, false); 
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        ctx = getActivity();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setting = (SettingShowView) ctx.findViewById(R.id.layout_step2_property);
        description = (RelativeLayout) ctx.findViewById(R.id.img_step2_description);
        
        description.setBackgroundResource(Util.initializeSupportedAms(FragmentState.ScanSide2));
        
        startListener = new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                LogC.d("start scan front page!");
                if(!CHolder.instance().getApplication().getSystemStateMonitor().hasScanPermission()) {
                    MultiButtonDialog msgDlg = MultiButtonDialog.createMsgDialog(getActivity(), R.string.error_permission_denied);
                    msgDlg.show();
                    return;
                }
                CHolder.instance().getJobManager().startScanBackAndCombine(new ExeCallback() {
                    
                    @Override
                    public void onComplete(EXE_RESULT ret) {
                        //CHolder.instance().getScanManager().getStateMachine().closeScanningDialog();
                        CHolder.instance().getScanManager().getStateMachine().getState().actCloseScanningDialog(CHolder.instance().getScanManager().getStateMachine(), null);
                        if (ret == EXE_RESULT.SUCCESSED) {
                            LogC.d("scan Back file success! ");
                            IdCardScanPrintActivity act = (IdCardScanPrintActivity) getActivity();
                            if(act != null) {
                                act.goToPreview();
                            }
                        } else if(ret == EXE_RESULT.ERR_COPY_FILE || ret == EXE_RESULT.FAILED) {
                            if(getActivity() != null) {
                                MultiButtonDialog msgDlg = MultiButtonDialog.createMsgDialog(getActivity(), R.string.scan_fail);
                                msgDlg.show();
                            }
                        } else if (ret == EXE_RESULT.ERR_COMBINED_FILE) {
                            if(getActivity() != null) {
                                MultiButtonDialog msgDlg = MultiButtonDialog.createMsgDialog(getActivity(), R.string.combine_file_fail);
                                msgDlg.show();
                            }
                        }
                        
                    }
                });
                

            }
        };

        setting.setStartBtnListener(startListener, null);
    }
    
    @Override
    public void onResume() {
        
        super.onResume();
        setting.setSettingItemDisable();
    }
}
