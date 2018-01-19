package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnItemClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;

public class InitialSettingReceiverActivity extends BaseActivity{
    
    /**
     * reset default data
     */
    private static final int SET_DATA_MODEL_RESET = 0;
    /**
     * set devices data
     */
    private static final int SET_DATA_MODEL_DEVICES = 1;    
    /**
     * refresh data
     */
    private static final int SET_DATA_MODEL_REFRESH = 2;
    /**
     * sender select item total
     */
    private static final int TOTAL = 5;
    /**
     * the header
     */
    private HeaderOkCancel header;
    /**
     * receiver setting list
     */
    private GridView buttons;
    /**
     * 
     */
    private RelativeLayout mResetBtn;
    /**
     * Read the address to be sent from the device
     */
    private ArrayList<Entry> entries;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setting_receiver);
        
        header = (HeaderOkCancel) findViewById(R.id.reveiver_header);
        buttons = (GridView) findViewById(R.id.list_receiver_setting);
        mResetBtn = (RelativeLayout) findViewById(R.id.init_setting_receiver_reset_btn);
        
        setEvent();
        setData(SET_DATA_MODEL_DEVICES);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        CHolder.instance().setLastInputEntry(null);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            LogC.d("--InitialSettingReceiverActivity--onActivityResult--->",((Entry) data.getExtras().getSerializable("entry"))+"");
            entries.set(requestCode, (Entry) data.getExtras().getSerializable("entry"));
            setData(SET_DATA_MODEL_REFRESH);
        }
    }
    /**
     * put data on list with model
     * @param model can be with {@link #SET_DATA_MODEL_DEVICES} or {@link #SET_DATA_MODEL_RESET} or {@link #SET_DATA_MODEL_REFRESH}
     */
    private void setData(int model) {
        ArrayList<Entry> tmpEntries = null;
        if (entries == null) {
            entries = new ArrayList<Entry>();
            for (int i = 0; i < TOTAL; i++) {
                entries.add(i, null);
            }
        }
        if (model == SET_DATA_MODEL_DEVICES) {
            tmpEntries = PreferencesUtil.getInstance().getDestinations();
        } else if (model == SET_DATA_MODEL_RESET) {
            for (int i = 0; i < TOTAL; i++) {
                entries.set(i, null);
            }
//            PreferencesUtil.getInstance().setDestinations(entries);
        } else if (model == SET_DATA_MODEL_REFRESH) {
        }
        if (tmpEntries != null) {
            for (int i = 0; i < tmpEntries.size(); i++) {
                entries.set(i, tmpEntries.get(i));
            }
        }
        mAdapter.notifyDataSetChanged();
    }
    /**
     * set widgets event
     */
    private void setEvent() {
        
        header.setHeaderTitle(getString(R.string.tx_init_setting_addressee));
        header.setOkBtnText(getString(R.string.bt_ok));
        header.setCancelBtnText(getString(R.string.bt_cancel));
        
        header.setOkBtnActionListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                PreferencesUtil.getInstance().setDestinations(entries);
                InitialSettingReceiverActivity.this.finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        header.setCancelBtnActionListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                InitialSettingReceiverActivity.this.finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        mResetBtn.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                setData(SET_DATA_MODEL_RESET);
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
            
            Entry entry = entries.get(position);
            if (entry != null && entry.getKeyDisplay() == null) {
                CHolder.instance().setLastInputEntry(entry);
            }
            startActivityForResult(new Intent(InitialSettingReceiverActivity.this, SenderGridViewActivity.class), position);
            InitialSettingReceiverActivity.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    };
    /**
     * list adapter
     */
    private BaseAdapter mAdapter = new BaseAdapter() {
        
        @Override
        public int getCount() {
            return TOTAL;
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
                convertView = LayoutInflater.from(InitialSettingReceiverActivity.this).inflate(R.layout.layout_initial_setting_receiver_item, parent, false);
            }
            TextView txtTitle = (TextView) convertView.findViewById(R.id.sender_title);
            ImageView imgCheckItemIcon = (ImageView) convertView.findViewById(R.id.sender_icon);
            TextView txtName = (TextView) convertView.findViewById(R.id.sender_title_name);
            
            String value = null;
            Drawable drawable = null;
            
            
            if (entries.get(position) == null) {
                imgCheckItemIcon.setVisibility(View.GONE);
                value = getString(R.string.fragment_sender_item_value_null);
            } else {
                drawable = getResources().getDrawable(R.drawable.icon_service_mail);
                
                imgCheckItemIcon.setVisibility(View.VISIBLE);
                value = entries.get(position).getKeyDisplay();
                
                if (entries.get(position).getMailData().getMailAddress() != null) {
                    drawable = getResources().getDrawable(R.drawable.icon_service_mail);
                    if (value == null) {
                        value = entries.get(position).getMailData().getMailAddress();
                    }
                } else if (entries.get(position).getFolderData().getPath() != null) {  
                    drawable = getResources().getDrawable(R.drawable.icon_service_folder);
                    if (value == null) {
                        if(entries.get(position).getFolderData().getProtocolType().equals("FTP")) {
                            value = entries.get(position).getFolderData().getServerName();
                        }else {
                            value = entries.get(position).getFolderData().getPath();
                        }
                        
                     
                       
                    }
                }
            }
            
            txtTitle.setText(getString(R.string.fragment_sender_item)+(position+1));
            if (value != null) {
                txtName.setText(value);
            } 
            if (drawable != null) {
                imgCheckItemIcon.setImageDrawable(drawable);
            }
            
            return convertView;
        }
    };

}
