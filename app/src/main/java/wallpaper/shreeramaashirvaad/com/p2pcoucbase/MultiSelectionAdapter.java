package wallpaper.shreeramaashirvaad.com.p2pcoucbase;

import java.util.ArrayList;


import android.content.Context;


import android.util.SparseBooleanArray;


import android.view.LayoutInflater;


import android.view.View;


import android.view.ViewGroup;


import android.widget.BaseAdapter;


import android.widget.CheckBox;


import android.widget.CompoundButton;


import android.widget.CompoundButton.OnCheckedChangeListener;


import android.widget.TextView;


public class MultiSelectionAdapter<T> extends BaseAdapter {


    Context mContext;


    LayoutInflater mInflater;


    ArrayList<DeviceInfo> mList;


    SparseBooleanArray mSparseBooleanArray;


    public MultiSelectionAdapter(Context context, ArrayList<DeviceInfo> list) {


        // TODO Auto-generated constructor stub


        this.mContext = context;


        mInflater = LayoutInflater.from(mContext);


        mSparseBooleanArray = new SparseBooleanArray();


        mList = new ArrayList<DeviceInfo>();


        this.mList = list;


    }


    public ArrayList<DeviceInfo> getCheckedItems() {


        ArrayList<DeviceInfo> mTempArry = new ArrayList<DeviceInfo>();


        for (int i = 0; i < mList.size(); i++) {


            if (mSparseBooleanArray.get(i)) {


                mTempArry.add(mList.get(i));


            }


        }


        return mTempArry;


    }


    @Override


    public int getCount() {


        // TODO Auto-generated method stub


        return mList.size();


    }


    @Override


    public Object getItem(int position) {


        // TODO Auto-generated method stub


        return mList.get(position);


    }


    @Override


    public long getItemId(int position) {


        // TODO Auto-generated method stub


        return position;


    }


    @Override


    public View getView(final int position, View convertView, ViewGroup parent) {


        // TODO Auto-generated method stub


        if (convertView == null) {


            convertView = mInflater.inflate(R.layout.row, null);


        }


        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);

        tvTitle.setText(mList.get(position).getUsername());


        CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.chkEnable);


        mCheckBox.setTag(position);


        mCheckBox.setChecked(mSparseBooleanArray.get(position));


        mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);


        return convertView;


    }


    OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {


        @Override


        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
        }


    };


}
