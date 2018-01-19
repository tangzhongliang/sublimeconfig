package jp.co.ricoh.advop.idcardscanprint.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnItemClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.IdCardScanPrintActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.InitialSettingActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SenderGridViewActivity;

public class SenderSelectFragment extends Fragment {//172.25.78.169/68.150 /10.0.2.2:54080 

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
    private static final int TOTAL = 6;
    /**
     * touch the button to setting page
     */
    private Button btnSetting;
    /**
     * for send items
     */
    private GridView gvButtons;
    /**
     * Read the address to be sent from the device
     */
    private ArrayList<Entry> entries;
    /**
     * Select the address you want to send
     */
    private Entry[] selects;
    
    //private LinearLayout settingLayout;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sender_select, container, false);  
        btnSetting = (Button) view.findViewById(R.id.btn_setting);
        gvButtons = (GridView) view.findViewById(R.id.List_receiver);
        //settingLayout = (LinearLayout)view.findViewById(R.id.listlayout_setting);
        return view;  
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        selects = CHolder.instance().getLinkedSelects();
        if (selects == null) {
            selects = new Entry[TOTAL];
        }
        
        btnSetting.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                startActivityForResult(new Intent(getActivity(), InitialSettingActivity.class), TOTAL);
            }
        });
        gvButtons.setAdapter(mAdapter);
        gvButtons.setOnItemClickListener(itemClickListener);
        
        setData(SET_DATA_MODEL_DEVICES);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TOTAL) {
                setData(SET_DATA_MODEL_REFRESH);
                LogC.d("--SenderSelectFragment--onActivityResult--->requestCode");
            } else {
                Entry tmpEntry = (Entry) data.getExtras().getSerializable("entry");
                LogC.d("--SenderSelectFragment--onActivityResult--->",tmpEntry.toString());
                if (requestCode == TOTAL-1) {
                    selects[TOTAL-1] = tmpEntry;
                }
                entries.set(requestCode, tmpEntry);
                setData(SET_DATA_MODEL_RESET);
            }
        }
    }
    /**
     * put data on list with model
     * @param model can be with {@link #SET_DATA_MODEL_DEVICES} or {@link #SET_DATA_MODEL_RESET} or {@link #SET_DATA_MODEL_REFRESH}
     */
    private void setData(int model) {
        ArrayList<Entry> tmpEntries = null;
        entries = CHolder.instance().getEntries();
        if (entries == null) {
            entries = new ArrayList<Entry>();
            for (int i = 0; i < TOTAL; i++) {
                entries.add(null);
               /* if (i != 5 && i != 1) {
                    Entry entry = new Entry(new HashMap<String, Object>());
                    if (i != 0) {
                        entry.setKeyDisplay("未设定--"+i);
                    }
                    entry.setName("未设定--"+i); 
                    entries.set(i, entry);
                }*/
            }
        }
        if (model == SET_DATA_MODEL_DEVICES) {
            tmpEntries = PreferencesUtil.getInstance().getDestinations();
        } else if (model == SET_DATA_MODEL_RESET) {
        } else if (model == SET_DATA_MODEL_REFRESH) {
            tmpEntries = PreferencesUtil.getInstance().getDestinations();
        }
        if (tmpEntries != null) {
            for (int i = 0; i < tmpEntries.size(); i++) {
                entries.set(i, tmpEntries.get(i));
                selects[i] = selects[i] != null ? entries.get(i):null;
            }
        }
        
        CHolder.instance().setLinkedSelects(selects);
        CHolder.instance().setEntries(entries);
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        CHolder.instance().setLastInputEntry(null);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        IdCardScanPrintActivity act = (IdCardScanPrintActivity)getActivity();
        if(CHolder.instance().getApplication().getSystemStateMonitor().isMachineAdmin()) {
            act.setNextVisble(View.INVISIBLE);            
        } else if(CHolder.instance().getSelects() == null || CHolder.instance().getSelects().length == 0) {      
            act.setNextVisble(View.VISIBLE);
            act.setNextEnable(false, (float)0.3);            
        } else {
            act.setNextVisble(View.VISIBLE);
            act.setNextEnable(true, (float)1.0);
        }
        
//        if(CHolder.instance().getApplication().getSystemStateMonitor().needShowSetting()) {
//            settingLayout.setVisibility(View.VISIBLE);
//        } else {
//            settingLayout.setVisibility(View.INVISIBLE);
//        }
    }
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
            return entries.get(position);
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //modify layout can not update
            //if (convertView == null) {
                convertView = LayoutInflater.from(SenderSelectFragment.this.getActivity()).inflate(R.layout.layout_fragment_sender_select_item, parent, false);
            //}
            ImageView imgCheckItem = (ImageView) convertView.findViewById(R.id.icon_act_check_sender);
            ImageView imgItemIcon = (ImageView) convertView.findViewById(R.id.icon_service_sender);
            TextView txtName = (TextView) convertView.findViewById(R.id.sender_title_name);
            TextView txtAddress = (TextView) convertView.findViewById(R.id.sender_title_address);
            Button imgAlter = (Button) convertView.findViewById(R.id.sender_alter);
            ImageView imgPop = (ImageView) convertView.findViewById(R.id.icon_act_pop);
            
            imgCheckItem.setSelected(selects[position] != null);
            imgAlter.setOnClickListener(new BaseOnClickListener() {
                
                @Override
                public void onWork(View v) {
                    
                    Entry entry = entries.get(position);
                    if (entry != null && entry.getKeyDisplay() == null) {
                        CHolder.instance().setLastInputEntry(entry);
                    }
                    startActivityForResult(new Intent(SenderSelectFragment.this.getActivity(), SenderGridViewActivity.class), position);
                }
            });
            
            imgPop.setVisibility(View.INVISIBLE);
            imgAlter.setVisibility(View.GONE);
            
            Drawable drawableBac = null;
            Drawable drawableIcon = null;
            String name = null;
            String address = null;
            
            if (entries.get(position) == null) {
                imgCheckItem.setVisibility(View.GONE);
                imgItemIcon.setVisibility(View.GONE);
                txtAddress.setVisibility(View.GONE);
                drawableBac = getResources().getDrawable(R.drawable.sim_bt_button_h);
                name = getString(R.string.fragment_sender_item) + (position+1) +": "+getString(R.string.fragment_sender_item_value_null);
                txtName.setTextColor(Color.argb(76, 0, 0, 0));
                if (position == TOTAL-1) {
                    txtName.setTextColor(Color.argb(255, 0, 0, 0));
                    drawableBac = getResources().getDrawable(R.drawable.selector_bt_com_01);
                    name = getString(R.string.fragment_sender_list_outside);
                    imgPop.setVisibility(View.VISIBLE);
                }
            } else {
                imgCheckItem.setVisibility(View.VISIBLE);
                imgItemIcon.setVisibility(View.VISIBLE);
                txtAddress.setVisibility(View.VISIBLE);
                
                if (entries.get(position).getKeyDisplay() == null) {
                    txtName.setVisibility(View.GONE);
                    txtAddress.setTextColor(Color.argb(255, 0, 0, 0));
                } else {
                    name = entries.get(position).getKeyDisplay();
                    txtName.setTextColor(Color.argb(255, 0, 0, 0));
                    txtAddress.setTextColor(Color.argb(128, 0, 0, 0));
                }
                if (entries.get(position).getMailData().getMailAddress() != null) {
                    drawableIcon = getResources().getDrawable(R.drawable.icon_service_mail);
                    address = entries.get(position).getMailData().getMailAddress();
                } else if (entries.get(position).getFolderData().getPath() != null) {
                    drawableIcon = getResources().getDrawable(R.drawable.icon_service_folder);
                    if (entries.get(position).getKeyDisplay() == null) {
                        address = entries.get(position).getName();
                    } else if(!CUtil.isStringEmpty(entries.get(position).getFolderData().getServerName())) {
                        String path = entries.get(position).getFolderData().getPath();
                        if (CUtil.isStringEmpty(path)) {
                            path = "\\";
                        }
                        if (!CUtil.isStringEmpty(path) && !path.startsWith("\\") && !path.startsWith("/") && !path.startsWith("¥")) {
                            path = "\\" + path;
                        }
                        address = entries.get(position).getFolderData().getServerName() + path;
                    } else {
                        address = entries.get(position).getFolderData().getPath();
                    }
                }
                if (position == TOTAL-1) {
                    imgAlter.setVisibility(View.VISIBLE);
                    drawableBac = getResources().getDrawable(R.drawable.bt_com_01_n);
                }
                if (selects[position] != null){
                    drawableBac = getResources().getDrawable(R.drawable.bt_com_01_w);
                } else {
                    drawableBac = getResources().getDrawable(R.drawable.selector_bt_com_01);
                }
            }
            
            if (drawableBac != null) {
                convertView.setBackground(drawableBac);
            }
            if (drawableIcon != null) {
                imgItemIcon.setImageDrawable(drawableIcon);
            }
            if (name != null) {
                txtName.setText(name);
            }
            if (address != null) {
                txtAddress.setText(address);
            }
            return convertView;
        }
    };
    /**
     * item clicked
     */
    private AdapterView.OnItemClickListener itemClickListener = new BaseOnItemClickListener() {
        
        @Override
        public void onWork(AdapterView<?> parent, View view, int position, long id){
            IdCardScanPrintActivity act = (IdCardScanPrintActivity)getActivity();
            if (entries.get(position) == null) {
                if (position == TOTAL-1) {
                    
                    startActivityForResult(new Intent(SenderSelectFragment.this.getActivity(), SenderGridViewActivity.class), position);     
                }
            } else {
                
                selects[position] = selects[position] == null ? entries.get(position) : null;
                mAdapter.notifyDataSetChanged();
                CHolder.instance().setLinkedSelects(selects);
            }
            
            if(CHolder.instance().getSelects() == null || CHolder.instance().getSelects().length == 0) {                
                act.setNextEnable(false, (float)0.3);
            } else {
                act.setNextEnable(true, (float)1.0);
            }
        }
    };
    
}
