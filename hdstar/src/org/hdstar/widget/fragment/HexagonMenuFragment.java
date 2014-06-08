package org.hdstar.widget.fragment;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hdstar.R;
import org.hdstar.component.activity.ForumsActivity;
import org.hdstar.component.activity.HelpActivity;
import org.hdstar.component.activity.MessageActivity;
import org.hdstar.component.activity.RemoteActivity;
import org.hdstar.component.activity.SettingActivity;
import org.hdstar.component.activity.SlidingFragmentActivity;
import org.hdstar.component.activity.TorrentActivity;

import cn.robust.hexagon.library.OnMenuItemClickedListener;
import cn.robust.hexagon.library.menu.HexagonMenu;
import cn.robust.hexagon.library.menu.HexagonMenuItem;

/**
 * 侧边栏六边形菜单
 * @author robust
 */
public class HexagonMenuFragment extends Fragment implements OnMenuItemClickedListener {

    public HexagonMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_hexagon_menu, container, false);
        HexagonMenu menu = (HexagonMenu) v.findViewById(R.id.hexa);
        HexagonMenuItem menuItem = menu.add(HexagonMenu.ITEM_POS_CENTER);
        menuItem.setIcon(R.drawable.menu_forum);
        menuItem.setText(R.string.forums);

        menuItem = menu.add(HexagonMenu.ITEM_POS_TOP_RIGHT);
        menuItem.setIcon(R.drawable.menu_torrent);
        menuItem.setText(R.string.torrent);

        menuItem = menu.add(HexagonMenu.ITEM_POS_RIGHT);
        menuItem.setIcon(R.drawable.menu_remote);
        menuItem.setText(R.string.remote);

        menuItem = menu.add(HexagonMenu.ITEM_POS_BOTTOM_RIGHT);
        menuItem.setIcon(R.drawable.menu_exit);
        menuItem.setText(R.string.exit);

        menuItem = menu.add(HexagonMenu.ITEM_POS_BOTTOM_LEFT);
        menuItem.setIcon(R.drawable.menu_setting);
        menuItem.setText(R.string.setting);

        menuItem = menu.add(HexagonMenu.ITEM_POS_LEFT);
        menuItem.setIcon(R.drawable.menu_message);
        menuItem.setText(R.string.message);

        menuItem = menu.add(HexagonMenu.ITEM_POS_TOP_LEFT);
        menuItem.setIcon(R.drawable.menu_help);
        menuItem.setText(R.string.help);
        menu.setOnMenuItemClickedListener(this);
        return v;
    }

    @Override
    public void onClick(HexagonMenuItem menuItem) {
        Intent intent = new Intent();
        @SuppressWarnings("rawtypes")
        Class klass;
        switch (menuItem.getPosition()){
            case HexagonMenu.ITEM_POS_CENTER:
                klass = ForumsActivity.class;
                break;
            case HexagonMenu.ITEM_POS_TOP_RIGHT:
                klass = TorrentActivity.class;
                break;
            case HexagonMenu.ITEM_POS_RIGHT:
                klass = RemoteActivity.class;
                break;
            case HexagonMenu.ITEM_POS_BOTTOM_RIGHT:
                checkExit();
                return;
            case HexagonMenu.ITEM_POS_BOTTOM_LEFT:
                klass = SettingActivity.class;
                break;
            case HexagonMenu.ITEM_POS_LEFT:
                klass = MessageActivity.class;
                break;
            case HexagonMenu.ITEM_POS_TOP_LEFT:
                klass = HelpActivity.class;
                break;
            default:
                return;
        }
        SlidingFragmentActivity sliding = (SlidingFragmentActivity) this
                .getActivity();
        sliding.toggle();
        if (!klass.isInstance(sliding)) {
            intent.setClass(this.getActivity(), klass);
            this.startActivity(intent);
            this.getActivity().finish();
        }
    }

    private void checkExit() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm)
                .setIcon(R.drawable.icon)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.exit,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                ((NotificationManager) getActivity()
                                        .getSystemService(
                                                Context.NOTIFICATION_SERVICE))
                                        .cancelAll();
                                getActivity().finish();
                            }

                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                            }

                        }).create().show();
    }
}
