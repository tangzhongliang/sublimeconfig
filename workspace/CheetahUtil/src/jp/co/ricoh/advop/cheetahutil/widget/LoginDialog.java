package jp.co.ricoh.advop.cheetahutil.widget;

import jp.co.ricoh.advop.cheetahutil.R;
import android.content.Context;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class LoginDialog extends BaseDialog{
	
	private EditText id;
	private EditText password;
	private Button leftButton;
	private Button rightButton; 

	Context context;
	private TextView idText;
	private TextView passwordext;

	public LoginDialog(Context context) {
		super(context);
		this.context = context;
		
		dlg.getWindow().setContentView(R.layout.dialog_login);
		id = (EditText) dlg.findViewById(
				R.id.dialog_status_feedback_edit);
		password = (EditText) dlg.findViewById(
				R.id.dialog_status_feedback_edit1);
		leftButton = (Button) dlg.findViewById(
				R.id.dl_blue_dialog_button1);
		rightButton = (Button) dlg.findViewById(
				R.id.dl_blue_dialog_button3);
		
		idText = (TextView) dlg.findViewById(R.id.dialog_status_title);
		passwordext = (TextView) dlg.findViewById(R.id.dialog_status_title1);
		
		
		FrameLayout inputID = (FrameLayout)dlg.findViewById(R.id.dialog_id);
        setupKeyboardHide(inputID, id);
        
		FrameLayout inputPW = (FrameLayout)dlg.findViewById(R.id.dialog_password);
        setupKeyboardHide(inputPW, password);
		
	}



	public void setLeftButtonClick(OnClickListener listener) {
		if(leftButton != null) {
			leftButton.setOnClickListener(listener);
		}
	}
	
	public void setRightButtonClick(OnClickListener listener) {
		if(rightButton != null) {
			rightButton.setOnClickListener(listener);
		}
	}

	public String getId() {

		return String.valueOf(id.getText());
	}

	public String getPassword() {

		return String.valueOf(password.getText());
	}
	
	public EditText getIdView(){
		return id;
	}
	
	public EditText getPasswordView(){
		return password;
	}
	
	public void setText(String id,String password){
		idText.setText(id);
		passwordext.setText(password);
	}

}
