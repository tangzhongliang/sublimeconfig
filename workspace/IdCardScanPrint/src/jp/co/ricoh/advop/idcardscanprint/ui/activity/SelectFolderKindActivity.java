package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnItemClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.BaseActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SenderGridViewActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;

import java.util.ArrayList;

public class SelectFolderKindActivity extends BaseActivity {
    
    public static final int TOTAL_SIZE = 2;
    
    public static final int FOLDER_KIND_SMB = 0;
    public static final int FOLDER_KIND_FTP = 1;
    
    /**
     * the cancel button
     */
    private RelativeLayout backBtn;
    /**
     * receiver setting list
     */
    private ListView buttons;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        setEnableBack();
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_input_folder_kind);
        
        backBtn = (RelativeLayout) findViewById(R.id.folder_kind_goback_btn);
        buttons = (ListView) findViewById(R.id.list_folder_kind);
        
        setEvent();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            Entry entry = (Entry) data.getExtras().getSerializable("entry");
            Intent intent = getIntent();Bundle bundle = new Bundle();
            bundle.putSerializable("entry", entry);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }
    /**
     * set widgets event
     */
    private void setEvent() {
        backBtn.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                SelectFolderKindActivity.this.finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        buttons.setAdapter(mAdapter);
        buttons.setOnItemClickListener(itemClickListener);
    }
    /**
     * list item click listener
     */
    private OnItemClickListener itemClickListener = new BaseOnItemClickListener() {

        @Override
        public void onWork(AdapterView<?> parent, View view, int position, long id) {
            
            switch (position) {
                case FOLDER_KIND_SMB:
                    startActivityForResult(new Intent(SelectFolderKindActivity.this,
                            InputSmbActivity.class), position);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    break;
                case FOLDER_KIND_FTP:
                    startActivityForResult(new Intent(SelectFolderKindActivity.this,
                            InputFtpActivity.class), position);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    break;
                default:
            }
        }
    };
    /**
     * list adapter
     */
    private BaseAdapter mAdapter = new BaseAdapter() {
        
        @Override
        public int getCount() {
            return TOTAL_SIZE;
        }
      
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public Object getItem(int position) {
            return position;
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SelectFolderKindActivity.this).inflate(
                        R.layout.layout_select_folder_kind_item, parent, false);
            }
            TextView txtTitle = (TextView) convertView.findViewById(R.id.folder_kind_title);

            switch (position) {
                case FOLDER_KIND_SMB:
                    txtTitle.setText(getString(R.string.folder_kind_smb));
                    break;
                case FOLDER_KIND_FTP:
                    txtTitle.setText(getString(R.string.folder_kind_ftp));
                    break;
                default:
            }

            return convertView;
        }
    };

}
