package org.hdstar.widget.fragment;

import org.hdstar.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class RemoteSettingFragment extends StackFragment {
	public static final int MODE_ADD = 0;
	public static final int MODE_EDIT = 1;
	
	private int order;//不为0时编辑，否则添加
	private Spinner typeSpn;
	private EditText name;
	private EditText ip;
	private EditText username;
	private EditText password;
	
	public static RemoteSettingFragment newInstance(int order){
		RemoteSettingFragment f = new RemoteSettingFragment();
		Bundle args = new Bundle();
		args.putInt("order", order);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		order = getArguments().getInt("order");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.remote_setting, null);
		typeSpn = (Spinner) v.findViewById(R.id.remote_type);
		name = (EditText) v.findViewById(R.id.remote_name);
		ip = (EditText) v.findViewById(R.id.ip);
		username = (EditText) v.findViewById(R.id.username);
		password = (EditText) v.findViewById(R.id.password);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] typeStr = this.getResources().getStringArray(R.array.boxType);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, typeStr);
		typeSpn.setAdapter(adapter);
		if(order != 0){
			
		}
	}

}
