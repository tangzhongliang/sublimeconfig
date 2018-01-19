package jp.co.ricoh.advop.cheetahutil.widget;

import jp.co.ricoh.advop.cheetahutil.R;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PreviewButtonDialog extends BaseDialog{
	
	private TextView text1, text2;
	private Button leftButton;
	private Button rightButton;
	private Button centerButton;
	private Button rightTopButton;

	Context context;
	
	public PreviewButtonDialog(Context context) {
		super(context);
		this.context = context;

		dlg.getWindow().setContentView(R.layout.dialog_preview);
		text1 = (TextView) dlg.findViewById(R.id.dp_tbd_blue_dialog_guidance30dot);
		text2 = (TextView) dlg.findViewById(R.id.dp_tbd_blue_dialog_guidance24dot);
		leftButton = (Button) dlg.findViewById(R.id.dp_tbd_blue_dialog_button1);
		rightButton = (Button) dlg.findViewById(R.id.dp_tbd_blue_dialog_button3);
		centerButton = (Button) dlg.findViewById(R.id.dp_tbd_blue_dialog_button2);		
			
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
	
	public void setCenterButtonClick(OnClickListener listener) {
		if(rightButton != null) {
			centerButton.setOnClickListener(listener);
		}
	}
	
	/*public void setOnDismissListener(OnDismissListener listener){
		this.setOnDismissListener(listener);
	}*/

	// set content
	public void setText(String string) {
		text2.setText(String.format(context.getString(R.string.dialog_sender_email_info_2), string));
	}
	public void setButtonLeft(String string){
		leftButton.setText(string);
	}
	public void setButtonRight(String string){
		rightButton.setText(string);
	}
	public void setButtonCenter(String string){
		centerButton.setText(string);
	}
	public void setButtonRightTop(String string){
		rightTopButton.setText(string);
	}
	
	public void dismissCountDown(){
		text2.setVisibility(View.INVISIBLE);
	}
	
	/*public void cancel()
	 * {
		this.cancel();
	}*/
	
}
