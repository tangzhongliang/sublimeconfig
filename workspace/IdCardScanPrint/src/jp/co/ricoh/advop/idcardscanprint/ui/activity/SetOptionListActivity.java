package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;



import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnItemClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SetOptionListActivity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SetOptionListActivity extends BaseActivity {
    private int mSelected;
    private SortListAdapter mAdapter;
    private ListView mList;
    private String title;
    private TextView txtTitle;
    private ImageView imgBackBtn;
    Map<String, Integer> map;
    private List<Integer> mListData = new ArrayList<Integer>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_list_base);
        txtTitle = (TextView)findViewById(R.id.txt_title);
        imgBackBtn = (ImageView)findViewById(R.id.title_img_back);
        
        Intent intent = this.getIntent();  

        //string = intent.getStringExtra("Inital");        
        title = intent.getStringExtra("Title");
        //intent.getIntegerArrayListExtra("ListData");
        
        if(title != null) {
            txtTitle.setText(title);
        }
        
        
        
        mList = (ListView) findViewById(R.id.listview_1row);       
        setListData();
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mOnItemClickListener);
        
        imgBackBtn.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                finish();
                SetOptionListActivity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        
        setEnableBack();
    }
    
    
    private void setListData() {
        if(mListData != null) {
            mListData.clear();
        }
        
        Intent intent = this.getIntent(); 
        mListData = intent.getIntegerArrayListExtra("ListData");
        if(mListData != null) {
            mSelected = intent.getIntExtra("Inital", mListData.get(0));
        }
//        Bundle bundle = getIntent().getExtras(); 
//        
//        map = (Map<String, Integer>) bundle.getSerializable("serializable");
//          
//        //String value = PreferencesUtil.getInstance(SetOptionListActivity.this).getSmtpServer().get(CHolder.instance().getGlobalDataManager().getSmtpType());
//        
//        Iterator it = map.keySet().iterator();    
//        while(it.hasNext()){    
//             String key;    
//              
//             key = it.next().toString();    
//             mListData.add(map.get(key));    
//             if(key.equalsIgnoreCase(string)) {
//                 mSelected = map.get(key);
//             }
//        }   
//              
        
        mAdapter = new SortListAdapter(this, mListData);
    }
    
    private final OnItemClickListener mOnItemClickListener = new BaseOnItemClickListener() {
        @Override
        public void onWork(AdapterView<?> parent, View view, int position, long id) {
            // 入力確認音
            

            // 設定の反映
            mSelected = mAdapter.getItem(position);
            mAdapter.notifyDataSetChanged();

            
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 自画面を閉じる
                    Intent intent = new Intent();  
                   // Map<String, Integer> map = Const.SSL_LIST;
//                    String seletString = "";
//                    Iterator it = map.keySet().iterator();    
//                    while(it.hasNext()){    
//                         String key;    
//                          
//                         key = it.next().toString();    
//                         
//                         if(mSelected == map.get(key)) {
//                             seletString = key;
//                             break;
//                         }
//                    }   
                    Bundle bundle = new Bundle(); 
                    bundle.putInt("selected", mSelected); 
                    intent.putExtras(bundle); 
                    LogC.d("mselected is" + mSelected);
                    //intent.putExtra("selected", mSelected);  
                   
                    setResult(RESULT_OK, intent);  
                    finish();
                    SetOptionListActivity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
            }, 10);
        }
    };
    
    
    
    private class SortListAdapter extends ArrayAdapter<Integer> {
        private List<Integer> mAdapterList;
        public SortListAdapter(Context context, List<Integer> objects) {
            super(context, 0, objects);
            mAdapterList = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
//            if (isRemoving()) {
//                return view;
//            }
            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_set_detail, parent, false);
            }

            View background = view.findViewById(R.id.list_1row_back);
            View icon = view.findViewById(R.id.list_1row_title_radio);
            TextView title = (TextView) view.findViewById(R.id.list_1row_title);
            View popup = view.findViewById(R.id.list_1row_figure);

            Integer item = getItem(position);

            if (isSelectedLine(item)) {
                background.setSelected(true);
                icon.setSelected(true);

            } else {
                background.setSelected(false);
                icon.setSelected(false);
            }

            Integer titleId = mAdapterList.get(position);
            if ( null != titleId ) {
                title.setText(titleId);
            }
            popup.setVisibility(View.GONE);

            return view;
        }

        private boolean isSelectedLine(Integer item) {
            return item == mSelected;
        }
    }
}
