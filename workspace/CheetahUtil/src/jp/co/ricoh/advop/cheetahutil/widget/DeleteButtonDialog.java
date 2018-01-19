package jp.co.ricoh.advop.cheetahutil.widget;

import jp.co.ricoh.advop.cheetahutil.R;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DeleteButtonDialog extends BaseDialog{
	private TextView tvNotice, tvUrl, tvId;
	private Button leftButton;
	private Button rightButton;

	Context context;
	
	public static DeleteButtonDialog createDeleteDialog(Context context, String name, String url, String id, BaseOnClickListener listener){
		DeleteButtonDialog dialog = new DeleteButtonDialog(context);
		dialog.setText(name, url, id);
		dialog.rightButton.setOnClickListener(listener);
		return dialog;
	}
	
	public DeleteButtonDialog(Context context) {
		super(context);
		this.context = context;
		
		dlg.getWindow().setContentView(R.layout.dialog_delete);
		tvNotice = (TextView) dlg.findViewById(R.id.tbd_blue_dialog_guidance30dot);
		tvUrl = (TextView) dlg.findViewById(R.id.tv_delsvr_cfmdlg_url);
		tvId = (TextView) dlg.findViewById(R.id.tv_delsvr_cfmdlg_id);
		leftButton = (Button) dlg.findViewById(R.id.tbd_blue_dialog_button1);
		rightButton = (Button) dlg.findViewById(R.id.tbd_blue_dialog_button3);	
		
		leftButton.setOnClickListener(new BaseOnClickListener() {
			@Override
			public void onWork(View v) {
				DeleteButtonDialog.this.dismiss();
			}
		});
	}
	
	public void setRightButtonClick(OnClickListener listener) {
		if(rightButton != null) {
			rightButton.setOnClickListener(listener);
		}
	}

	// set content
	public void setText(String name, String url, String id) {
		tvNotice.setText(String.format(context.getString(R.string.dialog_delete_info_1), name));
//		tvUrl.setText(String.format(context.getString(R.string.dialog_delete_info_2), url, id, "******"));
		if(url != null && url.isEmpty() == false) {
			tvUrl.setText(String.format(context.getString(R.string.txid_login_url_notice), url));
		}
		
		if(id != null && id.isEmpty() == false) {
			tvId.setText(String.format(context.getString(R.string.txid_login_id_notice), id));
		}
	}
}
