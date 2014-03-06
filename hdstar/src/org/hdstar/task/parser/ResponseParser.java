package org.hdstar.task.parser;

import java.io.InputStream;
import java.lang.reflect.Type;

import org.hdstar.R;

import ch.boye.httpclientandroidlib.HttpResponse;

/**
 * 请求任务解析. <br/>
 * 所有的BaseAsyncTask都要由此类来解析数据并判断请求任务是否成功完成
 * 
 * @author robust
 */
public abstract class ResponseParser<T> {
	public static final int SUCCESS_MSG_ID = 0;
	protected int msgId = R.string.refresh_failed;
	protected Type type;// gson 解析时需要用到的类型，泛型请使用gson自带的typetoken获取

	public ResponseParser() {
	}

	public ResponseParser(Type resultType) {
		type = resultType;
	}

	public ResponseParser(int defMsgId) {
		this.msgId = defMsgId;
	}

	public int getMessageId() {
		return msgId;
	}

	public void setMessageId(int id) {
		msgId = id;
	}

	/**
	 * 任务是否完成. <br/>
	 * 
	 * @return
	 */
	public boolean isSucceeded() {
		return msgId == SUCCESS_MSG_ID;
	}

	public void setSucceeded() {
		msgId = SUCCESS_MSG_ID;
	}

	public Type getType() {
		return type;
	}

	public abstract T parse(HttpResponse res, InputStream in);

}
