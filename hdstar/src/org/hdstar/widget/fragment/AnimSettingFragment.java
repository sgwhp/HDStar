package org.hdstar.widget.fragment;

import org.hdstar.R;
import org.hdstar.common.Const;
import org.hdstar.common.CustomSetting;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;

public class AnimSettingFragment extends StackFragment {
	private ToggleButton fade;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.animation_setting, null);
		fade = (ToggleButton) v.findViewById(R.id.fade);
		fade.setChecked(CustomSetting.fade);
		fade.setOnCheckedChangeListener(boxCheckedListener);
		RadioGroup radio = (RadioGroup) v.findViewById(R.id.animations);
		radio.setOnCheckedChangeListener(radioCheckedListener);
		// 注意保持界面动画的顺序与枚举类型中的顺序保持一致
		((RadioButton) radio.getChildAt(CustomSetting.anim.ordinal()))
				.setChecked(true);
		return v;
	}

	public void onStop() {
		super.onStop();
		Activity act = getActivity();
		Editor editor = act.getSharedPreferences(Const.SETTING_SHARED_PREFS,
				Activity.MODE_PRIVATE).edit();
		editor.putBoolean("fade", CustomSetting.fade);
		editor.putString("anim", CustomSetting.animToString());
		editor.commit();
	}

	CompoundButton.OnCheckedChangeListener boxCheckedListener = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// if (!isChecked && CustomSetting.anim == TransitionEffect.ZoomOut)
			// {
			// CustomSetting.fade = true;
			// fade.setChecked(true);
			// return;
			// }
			CustomSetting.fade = isChecked;
			JazzyViewPager vp = (JazzyViewPager) getViewPager();
			vp.reset();
			vp.setFadeEnabled(isChecked);
		}
	};

	RadioGroup.OnCheckedChangeListener radioCheckedListener = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.standard:
				CustomSetting.anim = TransitionEffect.Standard;
				break;
			case R.id.tablet:
				CustomSetting.anim = TransitionEffect.Tablet;
				break;
			case R.id.cube_in:
				CustomSetting.anim = TransitionEffect.CubeIn;
				break;
			case R.id.cube_out:
				CustomSetting.anim = TransitionEffect.CubeOut;
				break;
			case R.id.flip_horizontal:
				CustomSetting.anim = TransitionEffect.FlipHorizontal;
				break;
			case R.id.flip_vertical:
				CustomSetting.anim = TransitionEffect.FlipVertical;
				break;
			case R.id.stack:
				CustomSetting.anim = TransitionEffect.Stack;
				break;
			case R.id.zoom_in:
				CustomSetting.anim = TransitionEffect.ZoomIn;
				break;
			case R.id.zoom_out:
				CustomSetting.anim = TransitionEffect.ZoomOut;
				break;
			case R.id.rotate_up:
				CustomSetting.anim = TransitionEffect.RotateUp;
				break;
			case R.id.rotate_down:
				CustomSetting.anim = TransitionEffect.RotateDown;
				break;
			case R.id.accordion:
				CustomSetting.anim = TransitionEffect.Accordion;
				break;
			case R.id.panel:
				CustomSetting.anim = TransitionEffect.Panel;
				break;
			case R.id.jump_down:
				CustomSetting.anim = TransitionEffect.JumpDown;
				break;
			case R.id.jump_up:
				CustomSetting.anim = TransitionEffect.JumpUp;
				break;
			case R.id.window:
				CustomSetting.anim = TransitionEffect.Window;
				break;
			}
			JazzyViewPager vp = (JazzyViewPager) getViewPager();
			vp.setTransitionEffect(CustomSetting.anim);
		}
	};
}
