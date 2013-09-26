package org.hdstar.util;

import org.hdstar.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class UserClassImageGetter {
	public static Bitmap get(String url, Context context){
		Resources res = context.getResources();
		if(url.equals("/user")){
			return BitmapFactory.decodeResource(res, R.drawable.user);
		}
		if(url.equals("/power")){
			return BitmapFactory.decodeResource(res, R.drawable.power);
		}
		if(url.equals("/elite")){
			return BitmapFactory.decodeResource(res, R.drawable.elite);
		}
		if(url.equals("/crazy")){
			return BitmapFactory.decodeResource(res, R.drawable.crazy);
		}
		if(url.equals("/insane")){
			return BitmapFactory.decodeResource(res, R.drawable.insane);
		}
		if(url.equals("/veteran")){
			return BitmapFactory.decodeResource(res, R.drawable.veteran);
		}
		if(url.equals("/extreme")){
			return BitmapFactory.decodeResource(res, R.drawable.extreme);
		}
		if(url.equals("/ultimate")){
			return BitmapFactory.decodeResource(res, R.drawable.ultimate);
		}
		if(url.equals("/nexus")){
			return BitmapFactory.decodeResource(res, R.drawable.nexus);
		}
		if(url.equals("/forummoderator")){
			return BitmapFactory.decodeResource(res, R.drawable.forummoderator);
		}
		if(url.equals("/vip")){
			return BitmapFactory.decodeResource(res, R.drawable.vip);
		}
		if(url.equals("/uploader")){
			return BitmapFactory.decodeResource(res, R.drawable.uploader);
		}
		if(url.equals("/seeder")){
			return BitmapFactory.decodeResource(res, R.drawable.seeder);
		}
		if(url.equals("/encoder")){
			return BitmapFactory.decodeResource(res, R.drawable.encoder);
		}
		if(url.equals("/moderator")){
			return BitmapFactory.decodeResource(res, R.drawable.moderator);
		}
		if(url.equals("/administrator")){
			return BitmapFactory.decodeResource(res, R.drawable.administrator);
		}
		if(url.equals("/sysop")){
			return BitmapFactory.decodeResource(res, R.drawable.sysop);
		}
		if(url.equals("/staffleader")){
			return BitmapFactory.decodeResource(res, R.drawable.staffleader);
		}
		if(url.equals("/peasant")){
			return BitmapFactory.decodeResource(res, R.drawable.peasant);
		}
		return BitmapFactory.decodeResource(res, R.drawable.user);
	}
}
