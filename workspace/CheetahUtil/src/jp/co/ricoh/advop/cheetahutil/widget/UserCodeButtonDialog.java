package jp.co.ricoh.advop.cheetahutil.widget;

import jp.co.ricoh.advop.cheetahutil.R;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserCodeButtonDialog extends BaseDialog{
	

    public static final String ERROR_REASON_LOGIN_FAILED = "login_failed";
    /** 認証NGの理由：認証には成功したが利用権限がない */
    public static final String ERROR_REASON_PERMISSION_DENIED = "permission_denied";
	
    public static final String USER_CODE_SCAN = "usercode_scan";
    public static final String USER_CODE_PRINT = "userCode_print";
    
	public abstract class UserCodeCheckCallBack implements Runnable{
		public abstract void setJobContinue(boolean isContinue);
		public abstract void onCheckStart(Editable input);
		public void onCheckEnd(final String result, final boolean isAutoReg){
			((Activity)context).runOnUiThread(new Runnable() {
				public void run() {
					isAutoRegister = isAutoReg;
					dialog.setDlgOncheckStatus(result);
				}
			});
		}
	}
	
	private static UserCodeButtonDialog dialog;
	private TextView txtTip1;
	private FrameLayout edtLayout;
	private EditText edt;
	//private TextView txtTip2;
	private Button okButton, cancelBtn;
	private ProgressBar pbChecking;
	boolean isChecked = false;
	Context context;
	boolean isAutoRegister = false;
	boolean needContinue = false;

	private String currentType;
	public static UserCodeButtonDialog createUserCodeDialog(Context context, String type){
		dialog = new UserCodeButtonDialog(context, type);		
		return dialog;
	}
	
	private UserCodeButtonDialog(Context context, String type) {
		super(context);
		this.context = context;
		isChecked = false;
		isAutoRegister = false;
		currentType = type;
		dlg.getWindow().setContentView(R.layout.dialog_user_code);
		txtTip1 = (TextView) dlg.findViewById(R.id.tbd_blue_dialog_guidance30dot);
		edtLayout = (FrameLayout) dlg.findViewById(R.id.dialog_setting_button);
		edt = (EditText) dlg.findViewById(R.id.dialog_setting_button_edit);
		
		//txtTip2 = (TextView) dlg.findViewById(R.id.tbd_blue_dialog_guidance24dot);
		okButton = (Button) dlg.findViewById(R.id.tbd_blue_dialog_button);
		cancelBtn = (Button)dlg.findViewById(R.id.user_code_btn_cancel);
		pbChecking = (ProgressBar) dlg.findViewById(R.id.pb_on_checking);
		//View v = dlg.findViewById(R.id.dlg_viewGroup);
//		edt.clearFocus();
//		v.requestFocus();
		if(currentType.equalsIgnoreCase(USER_CODE_SCAN)) {
			cancelBtn.setVisibility(View.INVISIBLE);
		} else if (currentType.equalsIgnoreCase(USER_CODE_PRINT)) {
			cancelBtn.setVisibility(View.VISIBLE);
		}
		needContinue = false;
		//this.setupkeyboardHideGlobal(v);
        setupKeyboardHide(edtLayout, edt);
	}

	// set content

	public void setDialogCB(final UserCodeCheckCallBack cb){
		dialog.okButton.setOnClickListener(new BaseOnClickListener() {
			
			@Override
			public void onWork(View v) {
				if(CUtil.isStringEmpty(dialog.edt.getText().toString())) {
					MultiButtonDialog.createMustInputCheckDialog(context,
                            R.string.txid_scmn_f_user_code, dialog.edt).show();
                    return;
				}
				if (isChecked) {
					isChecked = false;
					if(needContinue) {
						cb.setJobContinue(true);						
						new Thread(cb).start();
						dialog.dismiss();
					} else {
						cb.setJobContinue(false);
						dialog.txtTip1.setText( R.string.txid_scmn_f_user_code_tip_1);
						dialog.edtLayout.setVisibility(View.VISIBLE);	
						//dialog.txtTip2.setVisibility(View.INVISIBLE);
						edt.setText("");
					}
				} else if (!isChecked) {
					isChecked = true;
					dialog.pbChecking.setVisibility(View.VISIBLE);
					cb.setJobContinue(false);
					cb.onCheckStart(dialog.edt.getText());
					new Thread(cb).start();
				}
			}
		});
		
		dialog.cancelBtn.setOnClickListener(new BaseOnClickListener() {
			
			@Override
			public void onWork(View v) {
				dialog.dismiss();				
			}
		});
	}
	
	private void setDlgOncheckStatus(String result){
		dialog.pbChecking.setVisibility(View.INVISIBLE);
		int tipId = 0;
		int edtLayoutVisibilityId = 0;
		int txtTip2VisibilityId = 0;
		
		if (result == null) {
			result = "";
		}
		
		if (result.equals(ERROR_REASON_LOGIN_FAILED)) {
			if (isAutoRegister) {
				tipId = R.string.txid_scmn_f_user_code_tip_4;
				needContinue = true;
			} else {
				tipId = R.string.txid_scmn_f_user_code_tip_2;
				needContinue = false;
			}
			//cancelBtn.setVisibility(View.VISIBLE);
			edtLayoutVisibilityId = View.INVISIBLE;
			txtTip2VisibilityId = View.VISIBLE;
			
		} else if (result.equals(ERROR_REASON_PERMISSION_DENIED)) {
			needContinue = false;
			tipId = R.string.txid_scmn_f_user_code_tip_3;
			edtLayoutVisibilityId = View.INVISIBLE;
			txtTip2VisibilityId = View.VISIBLE;
		} else {
			dialog.dismiss();
		}
		
		if (dialog.isShowing()) {
			dialog.txtTip1.setText(tipId);
			dialog.edtLayout.setVisibility(edtLayoutVisibilityId);
			//dialog.txtTip2.setVisibility(txtTip2VisibilityId);
		}
	}
}
