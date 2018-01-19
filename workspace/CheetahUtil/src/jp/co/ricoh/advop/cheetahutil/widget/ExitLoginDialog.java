package jp.co.ricoh.advop.cheetahutil.widget;

import jp.co.ricoh.advop.cheetahutil.R;
import jp.co.ricoh.advop.cheetahutil.util.BaseDialogOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ExitLoginDialog extends BaseDialog {
	private TextView text;
	private Button leftButton;
	private Button rightButton;
	private Button centerButton;

	Context context;
	
	public ExitLoginDialog(Context context) {
		super(context);
		this.context = context;
		dlg.getWindow().setContentView(R.layout.dialog_exit_login);
		text = (TextView) dlg.findViewById(R.id.tbd_blue_dialog_guidance30);
		leftButton = (Button) dlg.findViewById(R.id.tbd_blue_dialog_buttonone);
		rightButton = (Button) dlg.findViewById(R.id.tbd_blue_dialog_buttonthree);
		centerButton = (Button) dlg.findViewById(R.id.tbd_blue_dialog_buttontwo);		
	}
	
	public static ExitLoginDialog createMsgDialog(Context context, int msgID) {
		return ExitLoginDialog.createMsgDialog(context, context.getResources().getString(msgID));
	}
	
	public static ExitLoginDialog createMsgDialog(Context context, String msgID) {
		final ExitLoginDialog ret = new ExitLoginDialog(context);
		
		ret.text.setText(msgID);
		ret.leftButton.setVisibility(View.GONE);
		ret.centerButton.setVisibility(View.GONE);
		ret.rightButton.setVisibility(View.VISIBLE);
		ret.rightButton.setText(R.string.bt_ok);
		
		ret.rightButton.setOnClickListener(new BaseOnClickListener() {
			@Override
			public void onWork(View v) {
				ret.dismiss();
			}
		});
		
		return ret;
	}

	public static ExitLoginDialog createMustInputCheckDialog(Context context, int itemID, final EditText editText) {
		Resources res = context.getResources();
		String msg = String.format(res.getString(R.string.err_input), res.getString(itemID));
		return createInputCheckDialog(context, msg, editText);
	}

	public static ExitLoginDialog createInputCheckDialog(Context context, int msgID, final EditText editText) {
		Resources res = context.getResources();
		String msg = res.getString(msgID);
		return createInputCheckDialog(context, msg, editText);
	}
	
	public static ExitLoginDialog createInputCheckDialog(Context context, String msg, final EditText editText) {
		final ExitLoginDialog ret = new ExitLoginDialog(context);

		ret.text.setText(msg);
		
		ret.leftButton.setVisibility(View.GONE);
		ret.centerButton.setVisibility(View.GONE);
		ret.rightButton.setVisibility(View.VISIBLE);
		ret.rightButton.setText(R.string.bt_ok);
		
		ret.rightButton.setOnClickListener(new BaseOnClickListener() {
			@Override
			public void onWork(View v) {
				ret.dismiss();
				editText.requestFocus();
			}
		});
		
		return ret;
	}
	
	public static ExitLoginDialog createOKCancelDialogResIdAndString(Context context, int msgID, String val, final BaseDialogOnClickListener okOnClickListener) {
		Resources res = context.getResources();
		String msg = String.format(res.getString(msgID), val);
		
		final ExitLoginDialog ret = new ExitLoginDialog(context);
		
		ret.text.setText(msg);
		ret.leftButton.setVisibility(View.VISIBLE);
		ret.centerButton.setVisibility(View.GONE);
		ret.rightButton.setVisibility(View.VISIBLE);
	
		
		ret.leftButton.setOnClickListener(new BaseOnClickListener() {
			@Override
			public void onWork(View v) {
				ret.dismiss();
			}
		});
		
		ret.rightButton.setOnClickListener(new BaseOnClickListener() {
			@Override
			public void onWork(View v) {				
				okOnClickListener.onClick(ret);
			}
		});
		
		return ret;
	}

	
	public static ExitLoginDialog createOKCancelDialog(Context context, int msgID, final BaseDialogOnClickListener okOnClickListener) {
		final ExitLoginDialog ret = new ExitLoginDialog(context);
		
		ret.text.setText(msgID);
		ret.leftButton.setVisibility(View.VISIBLE);
		ret.centerButton.setVisibility(View.GONE);
		ret.rightButton.setVisibility(View.VISIBLE);
		
		
		ret.leftButton.setOnClickListener(new BaseOnClickListener() {
			@Override
			public void onWork(View v) {
				ret.dismiss();
			}
		});
		
		ret.rightButton.setOnClickListener(new BaseOnClickListener() {
			@Override
			public void onWork(View v) {				
				okOnClickListener.onClick(ret);
			}
		});
		
		return ret;
	}

	// set content
	public void setMsg(String string) {
		text.setText(string);
	}

	public Button getLeftButton() {
		return leftButton;
	}

	public Button getRightButton() {
		return rightButton;
	}

	public Button getCenterButton() {
		return centerButton;
	}
	
}
