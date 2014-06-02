package cn.robust.hexagon.library.menu;

import android.content.Context;
import android.graphics.Color;

import cn.robust.hexagon.library.Point;

/**
 * Created by robust on 2014-04-27.
 */
public class MenuItemLeft extends HexagonMenuItem {
    MenuItemLeft(Context context, HexagonMenu menu) {
        super(context, menu, HexagonMenu.ITEM_POS_LEFT);
        setBackgroundColor(Color.rgb(241, 196, 15));
        setTextColor(Color.WHITE);
    }

//    @Override
//    protected void genCenterPoint(float x, float y, float length, float margin) {
//        center.x = x - SQRT_3 * length - margin;
//        center.y = y;
//    }
}
