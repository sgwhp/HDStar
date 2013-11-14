package org.hdstar.widget.fragment;

import org.hdstar.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RemoteFragment extends StackFragment {

	public static RemoteFragment newInstance() {
		RemoteFragment f = new RemoteFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.test, null);
	}

}
