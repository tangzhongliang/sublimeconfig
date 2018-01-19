package jp.co.ricoh.advop.idcardscanprint.ui.activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.PopupWindow.OnDismissListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnItemClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.ConnecttingDialog;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.common.SmartSDKApplication;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.AddressbookDestinationSetting.DestinationKind;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.client.BasicRestContext;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.Request;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.RequestHeader;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.common.Response;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Addressbook;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.GetEntryResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.GetTagListResponseBody;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Tag;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.TagArray;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry.MailData;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.BaseActivity;


public class SenderSettingActivity extends BaseActivity {
    
    /** 種別切替ボタン */
    View mSelectServiceButton;
    /** 種別アイコン */
    ImageView mServiceIcon;
    /** タイトル */
    TextView mTitleText;
    /** 種別選択メニュー */
    PopupWindow mPopupWindow;
    View mToggleButton;
    /** 見出し切替アニメーション */
    Animation mToggleAnim;
    private static final String TAG = "SenderSettingActivity";
    public static final int REQUEST_CODE_ADDRESS_ACTIVITY = 1000;
    public static final String KEY_DEST_KIND = "destination_kind";
    public static final String KEY_DISPLAY = "key_display";
    private Button mOkButton;
    
    private Button mCancelButton;
    
    private TextView  mTitle;
    private ViewFlipper mTagGroupFlipper;
    private ViewGroup mTagGroup1;
    private ViewGroup mTagGroup2;
    private ViewGroup mTagGroup3;
    private List<Button> mTagButtons;
    private ImageView mChangeTagGroupButton;
   
    private ViewGroup mProgressArea;
    private GridView mEntryListView;
    private int mSelectedTagId = -1;
    
    private String mSelectedEntryId = null;
    private String mSelectedKeyDisplay;
    
    /**
     * 見出しの表示順と見出しのマップ
     * Map for title display order and title
     */
    private Map<Integer, Tag> mTagMap;
    
    /**
     * エントリーリスト
     * Entry list
     */
    private List<Entry> mEntryList;
    
    private Map<String, Entry> mTempEntryMap;
    private Addressbook mAddressbook;
    private  DestinationKind mKind ;
    private boolean isChangeType;
    int selectedTagId = 1;
    View popupView;
   
    /**
     * エントリ一覧取得タスク
     * Entry list acquisition task
     */
    private GetEntryListTask mGetEntryListTask;
    private EntryListAdapter mEntryListAdapter;
    private Entry selectedEntry;
    /**
     * 見出し一覧取得タスク
     * Tag list acquisition task
     * 
     */
    private GetTagListTask mGetTagListTask;
    private  String lastTitle ;
    private Drawable lastDrawable;
    private FrameLayout etEmailInputFra;
    private EditText etEmailInput;
    private LinearLayout flipper;
    /**
     *   (1) Initialize application
     *   (2) Obtain destination type and set title
     *   (3) Set destination selection state
     *   (4) Set tag button
     *   (5) Set tag group switching button
     *   (6) Set destination display area
     *   (7) Set OK button
     *   (8) Set cancel button
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_sender);
        mKind = DestinationKind.MAIL;
        isChangeType = false;
        flipper = (LinearLayout) findViewById(R.id.flipper);
        mSelectServiceButton = findViewById(R.id.select_service);
        mSelectServiceButton.setOnClickListener(new OnClickSelectService());
        mServiceIcon = (ImageView) findViewById(R.id.icon_service);
        mTitleText = (TextView) findViewById(R.id.app_title_text);
      
        
        mAddressbook = new Addressbook(new BasicRestContext("https"));
        
        mTempEntryMap = new LinkedHashMap<String, Entry>();
       
        mTagButtons = new ArrayList<Button>();
        mTagGroupFlipper = (ViewFlipper) findViewById(R.id.flipper_caption);
        mTagGroup1 = (ViewGroup) findViewById(R.id.tag_group_1);
        mTagGroup2 = (ViewGroup) findViewById(R.id.tag_group_2);;
        mTagGroup3 = (ViewGroup) findViewById(R.id.tag_group_3);
        
        
        
        BaseOnClickListener listener = new BaseOnClickListener() {
            @Override
            public void onWork(View v) {
                
                selectedTagId = ((Integer)v.getTag()).intValue();
                
                changeTagId(selectedTagId);
            }
        };
        Button button = (Button) mTagGroup1.getChildAt(0);
        button.setOnClickListener(listener);
        button.setTag(Integer.valueOf(1));
        mTagButtons.add(button);
        
        button = (Button) mTagGroup2.getChildAt(0);
        button.setOnClickListener(listener);
        button.setTag(Integer.valueOf(1));
        mTagButtons.add(button);
        
        button = (Button) mTagGroup3.getChildAt(0);
        button.setOnClickListener(listener);
        button.setTag(Integer.valueOf(1));
        mTagButtons.add(button);
        
        int tagIndex = 2;
        for (int i = 1; i < mTagGroup1.getChildCount(); i++) {
            button = (Button) mTagGroup1.getChildAt(i);
            button.setOnClickListener(listener);
            button.setTag(Integer.valueOf(tagIndex));
            mTagButtons.add(button);
            tagIndex++;
        }
        for (int i = 1; i < mTagGroup2.getChildCount(); i++) {
            button = (Button) mTagGroup2.getChildAt(i);
            button.setOnClickListener(listener);
            button.setTag(Integer.valueOf(tagIndex));
            mTagButtons.add(button);
            tagIndex++;
        }
        for (int i = 1; i < mTagGroup3.getChildCount(); i++) {
            button = (Button) mTagGroup3.getChildAt(i);
            button.setOnClickListener(listener);
            button.setTag(Integer.valueOf(tagIndex));
            mTagButtons.add(button);
            tagIndex++;
        }
        
        
        
        //(5)
        mChangeTagGroupButton = (ImageView) findViewById(R.id.btn_switch_caption);
        mChangeTagGroupButton.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View v) {
                
                mTagGroupFlipper.showNext();
                changeTagId(1);
            }
        });
        //(6)
        mProgressArea = (ViewGroup) findViewById(R.id.layout_progress);
        
        mEntryList = new ArrayList<Entry>();
        mEntryListAdapter = new EntryListAdapter();
        
        mEntryListView = (GridView) findViewById(R.id.gridview_address_entry);
        mEntryListView.setAdapter(mEntryListAdapter);
        
        mEntryListView.setOnItemClickListener(new BaseOnItemClickListener() {
            
            @Override
            public void onWork(AdapterView<?> parent, View view, int position, long id) {
                
                mSelectedEntryId = mEntryList.get(position).getEntryId();
                mSelectedKeyDisplay = mEntryList.get(position).getKeyDisplay();
                selectedEntry =  mEntryList.get(position);
                
                mOkButton.setEnabled(true);
                mOkButton.getBackground().setAlpha(255);
                mOkButton.setBackgroundResource(R.drawable.bt_ok_02_n);
               
            //    mEntryListAdapter.setSeclection(position);
                mEntryListAdapter.notifyDataSetChanged();
            }
        });
        
        mTitle = (TextView)findViewById(R.id.tx_title_h_ok_cancel);
        mTitle.setText(getString(R.string.tx_init_setting_messenger));
        
        
//        Entry entry = PreferencesUtil.getInstance().getEntrySender();
//        if(entry!=null) {
//            mSelectedEntryId = entry.getEntryId();
//        }
        
        //7
        mOkButton = (Button)findViewById(R.id.btn_ok_h_okcancel);
        mOkButton.setText(getString(R.string.bt_ok));
        if (mSelectedEntryId ==null) {
            mOkButton.setEnabled(false);
            mOkButton.getBackground().setAlpha(30);
     
          
        }
        mOkButton.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View v) {
                
                
                if (mTitleText.getText().toString().equals(getString(R.string.txid_scan_b_dest_select_mail_add))) {
                    
                    
                   boolean  isEmpty =  CUtil.isStringEmpty(etEmailInput.getText().toString());
                   if (CUtil.isStringEmpty(etEmailInput.getText().toString())) {
                       MultiButtonDialog msgDialog = MultiButtonDialog.createMustInputCheckDialog(
                               SenderSettingActivity.this,
                               R.string.sender_email, etEmailInput);
                       msgDialog.show();
                       return;
                   }
                    if (!isEmpty) {
                        boolean isVaildEmail = CUtil.isValidEmail(etEmailInput.getText().toString());   
                        if (isVaildEmail) {
                            Entry entry = new Entry(new LinkedHashMap<String, Object>());
                            //entry.setName(etEmailInput.getText().toString());
                            MailData mailData = entry.getMailData();
                            mailData.setMailAddress(etEmailInput.getText().toString());

                            PreferencesUtil.getInstance().setEntrySender(entry);
                           
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

                        } else {
                            
                            showErrorDialog(getString(R.string.sender_invaild_email));

                        }
                    }

                    return;

                }

                if (mSelectedEntryId != null) {

                    if (mKind != DestinationKind.FOLDER) {
                        selectedEntry.removeFolderData();
                    } else if (mKind != DestinationKind.MAIL) {
                        selectedEntry.removeMailData();
                    }
                    PreferencesUtil.getInstance().setEntrySender(selectedEntry);
               
                   finish();
                   overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
               
            }
        });
        
        //(8)
        mCancelButton = (Button) findViewById(R.id.btn_cancel_h_okcancel);
        mCancelButton.setText(getString(R.string.bt_cancel));
        mCancelButton.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View v) {
                
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        popupView = LayoutInflater.from(this).inflate(R.layout.view_select_service, null, false);
        
        
//        RelativeLayout.LayoutParams linearParams =  (RelativeLayout.LayoutParams)popupView.findViewById(R.id.btn_mail_add).getLayoutParams();  
//        linearParams.height = 70;  
//        popupView.findViewById(R.id.btn_mail_add).setLayoutParams(linearParams); 
//        
//        popupView.findViewById(R.id.btn_mail_add).setBackgroundResource(R.drawable.selector_popwindow_bg_03);
        lastTitle = mTitleText.getText().toString();
        lastDrawable = mServiceIcon.getDrawable(); 
        
        
        etEmailInputFra = (FrameLayout)findViewById(R.id.email_input);
        etEmailInput   =  (EditText) findViewById(R.id.et_email_input);
        setupKeyboardHide(etEmailInputFra);
        
        
       
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mGetTagListTask = new GetTagListTask();
        //mGetTagListTask.execute();
        mGetTagListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    @Override
    public void onStop() {
    
        super.onStop();
    }
    
    @Override
    protected void onResume() {
        super.onResume();     
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGetEntryListTask != null) {
            mGetEntryListTask.cancel(false);
            mGetEntryListTask = null;
        }
        if (mGetTagListTask != null) {
            mGetTagListTask.cancel(false);
            mGetTagListTask = null;
        }
//        mOkButton = null;
//        mCancelButton = null;
//        mTagGroupFlipper = null;
//        mTagGroup1 = null;
//        mTagGroup2 = null;
//        mTagGroup3 = null;
//        mTagButtons = null;
//        mChangeTagGroupButton = null;
//        mProgressArea = null;
//        mEntryListView = null;
//        mSelectedEntryId = null;
//        mSelectedKeyDisplay = null;
//        mTagMap = null;
//        mEntryList = null;
//        mTempEntryMap = null;
//        mEntryListAdapter = null;
//        mAddressbook = null;
//        mKind = null;
       
    }
    
    /** 種別選択ボタン */
    class OnClickSelectService extends BaseOnClickListener {
        @Override
        public void onWork(View v) {
            
            showPopWindowView();
            mPopupWindow.showAtLocation(v, 0, 4, 158);
            
            if(lastTitle.equals(getString(R.string.txid_scan_b_dest_select_mail))) {
                
                popupView.findViewById(R.id.btn_mail).setSelected(true);
                //popupView.findViewById(R.id.btn_mail_add).setSelected(false);
                popupView.findViewById(R.id.btn_mail_add_sender).setSelected(false);
            }else {
                popupView.findViewById(R.id.btn_mail).setSelected(false);
                //popupView.findViewById(R.id.btn_mail_add).setSelected(true);
                popupView.findViewById(R.id.btn_mail_add_sender).setSelected(true);
            }
            //mSelectServiceButton.setSelected(true);
        }
    };
    
    public  void  showPopWindowView() {
       
        
        popupView.findViewById(R.id.btn_folder).setVisibility(View.GONE);
        popupView.findViewById(R.id.btn_folder_add).setVisibility(View.GONE);
        popupView.findViewById(R.id.btn_mail_add).setVisibility(View.GONE);
        
        popupView.findViewById(R.id.btn_mail_add_sender).setVisibility(View.VISIBLE);
        
//        popupView.findViewById(R.id.btn_folder).setOnClickListener(mOnClickListener);
//        popupView.findViewById(R.id.btn_folder_add).setOnClickListener(mOnClickListener);
        popupView.findViewById(R.id.btn_mail).setOnClickListener(mOnClickListener);
        popupView.findViewById(R.id.btn_mail_add_sender).setOnClickListener(mOnClickListener);
        mPopupWindow = new BackkeyPopupWindow(this, popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
      //  mPopupWindow = new  PopupWindow(popupView,  LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setOnDismissListener(new OnDismissServiceMenu());
        
        
        
    }
    
    /** 種別選択メニューのonDismissListener */
    class OnDismissServiceMenu implements OnDismissListener {
        @Override
        public void onDismiss() {
            mSelectServiceButton.setSelected(false);
        }
    }
    
    /** 種別選択ボタンのonClickListener */
    final BaseOnClickListener mOnClickListener = new BaseOnClickListener() {
        @Override
        public void onWork(View view) {
         
            
            switch (view.getId()) {
                case R.id.btn_mail:
               
                    TextView tvEmail= (TextView) popupView.findViewById(R.id.tv_mail); 
                    ImageView ivIcon= (ImageView) popupView.findViewById(R.id.icon_mail);
                    
                    if(!mTitleText.getText().toString().equals(getString(R.string.txid_scan_b_dest_select_mail))) {
                        mSelectedEntryId= null;
                  
                        mOkButton.setEnabled(false);
                        mOkButton.getBackground().setAlpha(30);
                        mServiceIcon.setImageDrawable(ivIcon.getDrawable());
                        mTitleText.setText(tvEmail.getText().toString());
                        
                        lastTitle = mTitleText.getText().toString();
                        lastDrawable = mServiceIcon.getDrawable(); 
                        mKind = DestinationKind.MAIL;
                        isChangeType= true;
                        goneEmailView();
                        changeTagId(selectedTagId);                       
                      
                    }
                    break;
                    
                case R.id.btn_mail_add_sender:
                    
                    showProgressBar(false);
                    
                    TextView tvEmailAdd= (TextView) popupView.findViewById(R.id.tv_mail_add_sender); 
                    ImageView ivIconAdd= (ImageView) popupView.findViewById(R.id.icon_mail_add_sender);
                    
                    mServiceIcon.setImageDrawable(ivIconAdd.getDrawable());
                    mTitleText.setText(tvEmailAdd.getText().toString());
                                    
                    lastTitle = mTitleText.getText().toString();
                    lastDrawable = mServiceIcon.getDrawable(); 
                    mKind = null;
                    showEmailView();
                    mEntryList = new ArrayList<Entry>();
                    mEntryListView.removeAllViewsInLayout();
                    mEntryListAdapter.notifyDataSetChanged();
                    break;

                default:
                    
                    break;
            }
            mPopupWindow.dismiss();
        }
    }; 
    
    public void goneEmailView() {
        if (etEmailInputFra.getVisibility()== 0) {
            
            mOkButton.setEnabled(false);
            mOkButton.getBackground().setAlpha(30);
            etEmailInputFra.setVisibility(View.GONE);
            flipper.setVisibility(View.VISIBLE);
            mEntryListView.setVisibility(View.VISIBLE);
        }
    }
    
    public  void showEmailView() {
        
        if (etEmailInputFra.getVisibility() !=0) {
            mOkButton.setEnabled(true);
            mOkButton.getBackground().setAlpha(255);
            mOkButton.setBackgroundResource(R.drawable.bt_ok_02_n);
            etEmailInputFra.setVisibility(View.VISIBLE);
            flipper.setVisibility(View.GONE);
            mEntryListView.setVisibility(View.GONE);
            Entry entry = PreferencesUtil.getInstance().getEntrySender();
            if(entry != null){
                if(entry.getEntryId() == null || entry.getEntryId()== ""){
                    etEmailInput.setText(entry.getMailData().getMailAddress()) ;
                }
               
            }else {
                etEmailInput.setText("");
            }
        }
       
    }
    
    public void showErrorDialog(String error) {

        final ConnecttingDialog dlg = new ConnecttingDialog(SenderSettingActivity.this);
        dlg.setText(error);
        dlg.setButtonText("OK");
        dlg.SetButtonListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                dlg.dismiss();
            }
        });
        dlg.show();
     

    }
    class GetTagListTask extends AsyncTask<Void, Void, Map<Integer, Tag>> {
        
        @Override
        protected void onPreExecute() {
            showProgressBar(true);
            super.onPreExecute();
        }
        
        @Override
        protected Map<Integer, Tag> doInBackground(Void... ignore) {
            LinkedHashMap<Integer, Tag> tagMap = new LinkedHashMap<Integer, Tag>();
            
            RequestHeader header = new RequestHeader();
            header.put(RequestHeader.KEY_X_APPLICATION_ID, SmartSDKApplication.getProductId());
            Request request = new Request();
            request.setHeader(header);
            try {
                Response<GetTagListResponseBody> response = mAddressbook.getTagList(request);
                
                TagArray tagArray = response.getBody().getTags();
                if(tagArray != null) {
                    for(int i=0; i<tagArray.size(); ++i) {
                        Tag tag = tagArray.get(i);
                        tagMap.put(tag.getTagId(), tag);
                    }
                }
                
            } catch (Exception e) {
   
                LogC.w("GetTagListTask--",e);
            }
            return tagMap;
        }
        
        @Override
        protected void onPostExecute(Map<Integer, Tag> result) {
            Button button;
            Tag tag;
            for (int i = 0; i < mTagButtons.size(); i++) {
                button = mTagButtons.get(i);
                Integer tagIndex = (Integer) button.getTag();
                tag = result.get(tagIndex);
                
                if (tag != null && tagIndex == 1) {
                    tag.setKeyDisplay(getString(R.string.txid_scan_b_dest_addr_tag_freq));
                }
                
                if (tag != null) {
                    button.setText(tag.getKeyDisplay());
                } else {
                    button.setText("");
                    button.setEnabled(false);
                    
                }
            }
            
            mTagMap = result;
            mTagGroupFlipper.setVisibility(View.VISIBLE);
            showProgressBar(false);
            changeTagId(1);
        }
        
    }
    
    private void changeTagId(int newTagId) {
        
        if (isChangeType) {
            isChangeType = false;
            showListView(newTagId); 
        } else {
            if (mSelectedTagId != newTagId) {
                showListView(newTagId);
            } 
        }
    }
    
    public void  showListView(int newTagId) {
        mSelectedTagId = newTagId;

        // (1)
        showProgressBar(true);

        // (2)
        selectTagButton(newTagId);

        // (3)
        Tag tag = mTagMap.get(newTagId);
        List<String> entryIdList;
        if (tag != null && tag.getTagsEntryList() != null) {
            entryIdList = new ArrayList<String>(tag.getEntryNum());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority1());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority2());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority3());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority4());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority5());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority6());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority7());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority8());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority9());
            putToEntryIdList(entryIdList, tag.getTagsEntryList().getTagPriority10());
        } else {
            entryIdList = Collections.emptyList();
        }

        // (4)
        if (mGetEntryListTask != null) {
            mGetEntryListTask.cancel(false);
        }
        mGetEntryListTask = new GetEntryListTask();
        //mGetEntryListTask.execute(entryIdList.toArray(new String[entryIdList.size()]));
        mGetEntryListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, entryIdList.toArray(new String[entryIdList.size()]));
        return;
     
    }
    private void showProgressBar(boolean show) {
        if (show && !mTitleText.getText().toString().equals(getString(R.string.txid_scan_b_dest_select_mail_add))) {
            mEntryListView.setVisibility(View.INVISIBLE);
            mProgressArea.setVisibility(View.VISIBLE);
        } else {
            //mProgressArea.setVisibility(View.INVISIBLE);
            mProgressArea.setVisibility(View.GONE);
            mEntryListView.setVisibility(View.VISIBLE);
        }
    }
    
    private void selectTagButton(int selectTagId) {
        for (int i = 0; i < mTagButtons.size(); i++) {
            Button button = mTagButtons.get(i);
            int tagId = ((Integer)button.getTag()).intValue();
            boolean selected = (tagId == selectTagId);
            if (button.isSelected() != selected) {
                button.setSelected(selected);
            }
        }
    }
    
    
    private void putToEntryIdList(List<String> entryIdList, List<String> tagPriorityIdList) {
        
        
        if (tagPriorityIdList != null) {
            for (String entryId : tagPriorityIdList) {
                if (entryId != null) {
                    entryIdList.add(entryId);
                }
            }
        }
    }
    private class GetEntryListTask extends AsyncTask<String, Entry, List<Entry>> {
        
        @Override
        protected void onPreExecute() {
            mEntryList = new ArrayList<Entry>();
            mEntryListView.removeAllViewsInLayout();
        }
        
        @Override
        protected List<Entry> doInBackground(String... params) {
            
            ArrayList<Entry> entryList = new ArrayList<Entry>();
            
            RequestHeader header = new RequestHeader();
            header.put(RequestHeader.KEY_X_APPLICATION_ID, SmartSDKApplication.getProductId());
            
            Request request = new Request();
            request.setHeader(header);
            
            try {

                for (int i = 0; i < params.length; i++) {
                    if (isCancelled()) {
                        LogC.d(TAG, "getEntry task aborted");
                        break;
                    }

                    String entryId = params[i];
                    Response<GetEntryResponseBody> response = mAddressbook.getEntry(request,  entryId);
                    if (isCancelled()) {
                        LogC.d(TAG, "getEntry task aborted");
                        break;
                    }

                    Entry entry = response.getBody();
                    if (mKind == DestinationKind.FOLDER) {

                        if (entry.getFolderData().getServerName() != null) {
                            entryList.add(entry);
                            publishProgress(entry);
                        }
                    } else if (mKind == DestinationKind.MAIL) {

                        if (entry.getMailData().getMailAddress() != null) {
                            entryList.add(entry);
                            publishProgress(entry);
                        }
                    }

                }

            } catch (Exception e) {
                LogC.e("GetEntryListTask--",e);
               
            }
            
            return entryList;
        }
        @Override
        protected void onProgressUpdate(Entry... values) {
//            for (int i = 0; i < values.length; i++) {
//                mEntryList.add(values[i]);
//            }
            if(mEntryList != null) {
                mEntryList.add(values[0]);
                mEntryListAdapter.notifyDataSetChanged();
            }
            // showProgressBar(false);
        }
        
        @Override
        protected void onPostExecute(List<Entry> result) {
            // mEntryListAdapter.notifyDataSetChanged();
            showProgressBar(false);
        }
        
    }
    
    private static class ViewHolder {
        ViewGroup container;
        TextView registNo;
        TextView keyDisplay;
//        ImageView kind;
//        ImageView lock;
    }
    
    private class EntryListAdapter extends BaseAdapter {
        
        private final LayoutInflater mInflater;
        
     
       public  EntryListAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.layout_sender_gridview_item, null);
                holder.container = (ViewGroup) convertView.findViewById(R.id.address_container);
                holder.registNo = (TextView) convertView.findViewById(R.id.txt_regist);
                holder.keyDisplay = (TextView) convertView.findViewById(R.id.txt_disp);
//                holder.kind = (ImageView) convertView.findViewById(R.id.img_type);
//                holder.lock = (ImageView) convertView.findViewById(R.id.img_secret);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Entry entry = (Entry) getItem(position);
            holder.registNo.setText(String.format("%05d", entry.getRegistrationNumber()));
            holder.keyDisplay.setText(entry.getKeyDisplay());
            
            if (entry.getEntryId().equals(mSelectedEntryId)) {
                holder.container.setBackgroundResource(R.drawable.bt_com_01_w);
                mSelectedKeyDisplay = entry.getKeyDisplay();
            } else {
                holder.container.setBackgroundResource(R.drawable.bt_com_01_n);
            }
            

            
            return convertView;
        }
        
        @Override
        public int getCount() {
            return mEntryList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return mEntryList.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
    }
    
    class BackkeyPopupWindow extends PopupWindow {

        public BackkeyPopupWindow(final Context context, View contentView, int width, int height, boolean focusable) {
            super(contentView, width, height, focusable);

            contentView.setFocusableInTouchMode(true);
            contentView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && isShowing()) {
                        dismiss();
                    }
                    return true;
                }
            });
        }
        @Override
        public final void setBackgroundDrawable(Drawable background) {
            super.setBackgroundDrawable(null);
        }
    }
}