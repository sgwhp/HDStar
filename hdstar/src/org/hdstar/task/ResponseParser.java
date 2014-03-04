package org.hdstar.task;

import java.io.InputStream;
import java.lang.reflect.Type;

import org.hdstar.R;

import ch.boye.httpclientandroidlib.HttpResponse;

/**
 * �����������. <br/>
 * ���е�BaseAsyncTask��Ҫ�ɴ������������ݲ��ж����������Ƿ�ɹ����
 * 
 * @author robust
 */
public abstract class ResponseParser<T> {
	public static final int SUCCESS_MSG_ID = 0;
	protected int msgId = R.string.refresh_failed;
	protected Type type;// gson ����ʱ��Ҫ�õ������ͣ�������ʹ��gson�Դ���typetoken��ȡ

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
	 * �����Ƿ����. <br/>
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
