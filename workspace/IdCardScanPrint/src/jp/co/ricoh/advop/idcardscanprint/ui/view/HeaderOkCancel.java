
package jp.co.ricoh.advop.idcardscanprint.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jp.co.ricoh.advop.idcardscanprint.R;

public class HeaderOkCancel extends RelativeLayout {

    /**
     * header titile
     */
    private TextView headerTitle;

    /**
     * header cancel button
     */
    private Button cancelBtn;

    /**
     * header ok button
     */
    private Button okBtn;

    private View view;

    public HeaderOkCancel(Context context) {
        super(context);
        init(context);
    }

    public HeaderOkCancel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * init compenent
     * 
     * @param context
     */
    public void init(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.view_head_ok_cancel, this);
        headerTitle = (TextView) view.findViewById(R.id.tx_title_h_ok_cancel);
        cancelBtn = (Button) view.findViewById(R.id.btn_cancel_h_okcancel);
        okBtn = (Button) view.findViewById(R.id.btn_ok_h_okcancel);
    }

    /**
     * to set header titile
     * 
     * @param title
     */
    public void setHeaderTitle(String title) {

        headerTitle.setText(title);
    }

    /**
     * to set the text of cancel button
     * 
     * @param text
     */
    public void setCancelBtnText(String text) {
        cancelBtn.setText(text);
    }

    public void setOkBtnText(String text) {
        okBtn.setText(text);
    }

    /**
     * do everything about click cancel button in the setCancelAction method
     * 
     * @param cancelBtnAL
     */
    public void setCancelBtnActionListener(View.OnClickListener l) {
        cancelBtn.setOnClickListener(l);

    }

    /**
     * do everything about click ok button in the setCancelAction method
     * 
     * @param cancelBtnAL
     */
    public void setOkBtnActionListener(View.OnClickListener l) {
        okBtn.setOnClickListener(l);
    }
}
