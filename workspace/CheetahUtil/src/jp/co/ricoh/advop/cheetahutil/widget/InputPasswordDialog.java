package jp.co.ricoh.advop.cheetahutil.widget;

import jp.co.ricoh.advop.cheetahutil.R;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InputPasswordDialog  extends BaseDialog {


	private TextView etName , tvProtocolType;
	private TextView etEmail;
	private EditText etPassword;
	private Button btnCancel;
	private Button btnOk;
    private FrameLayout etPasswordFra;
	Context context;
	private ProgressBar mProgressArea;
	
	public static InputPasswordDialog createInputPasswordDialog(Context context, String type, String name, String path){
		InputPasswordDialog dialog = new InputPasswordDialog(context, type, name, path);
	
		return dialog;
	}
	public InputPasswordDialog(Context context, String type, String name, String path) {
		super(context);
		dlg.getWindow().setContentView(R.layout.dialog_password);
		
		mProgressArea = (ProgressBar) dlg.findViewById(R.id.layout_progress);
		
		etPasswordFra = (FrameLayout)dlg.findViewById(R.id.frame_password);
		tvProtocolType = (TextView)dlg.findViewById(R.id.tv_ProtocolType_value);
		etName = (TextView) dlg.findViewById(R.id.et_sender_name);
		etEmail = (TextView) dlg.findViewById(R.id.et_sender_email);
		etPassword = (EditText) dlg.findViewById(R.id.et_password);
		btnCancel = (Button) dlg.findViewById(R.id.btn_cancel);
		btnOk = (Button) dlg.findViewById(R.id.btn_ok);
		etName.setText(name);
		etEmail.setText(path);
		tvProtocolType.setText(type);
		
		setupKeyboardHide(etPasswordFra, etPassword);
		btnOk.setOnClickListener(new BaseOnClickListener() {

			@Override
			public void onWork(View v) {
				etPassword.getText().toString();
				dismiss();
			}
		});
		btnCancel.setOnClickListener(new BaseOnClickListener() {

			@Override
			public void onWork(View v) {
			
				dismiss();
			}
		});
	}
	public void setOKOnClick(View.OnClickListener listener) {
		btnOk.setOnClickListener(listener);
	}
	
	public void setCancelOnClick(View.OnClickListener listener) {
		btnCancel.setOnClickListener(listener);
	}
	
	public String getPassword() {
		
		return etPassword.getText().toString();
	}
	
	 public  void showProgressBar(boolean show) {
	        if (show) {
	        	if (mProgressArea.getVisibility() != 0) {
	        		mProgressArea.setVisibility(View.VISIBLE);
	        	}
	            
	        } else {
	        	if (mProgressArea.getVisibility() == 0) {
	        		mProgressArea.setVisibility(View.GONE);
	        	}
	         
	          
	        }
	    }
	 
	 public void setOkDisabled(boolean able) {
			if (able) {
				btnOk.setEnabled(false);
				btnOk.setClickable(false);
			} else {
				btnOk.setEnabled(true);
				btnOk.setClickable(true);
			}
			 
		 }
}
