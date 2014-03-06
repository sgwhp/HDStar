package org.hdstar.task.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.hdstar.model.ResponseWrapper;

import ch.boye.httpclientandroidlib.HttpResponse;

import com.google.gson.Gson;

/**
 * 
 * 应用服务器返回json数据的解析工具. <br/>
 * 
 * @author robust
 */
public class DelegateGetParser<T> extends ResponseParser<T> {
	private final Type resultType;

	public DelegateGetParser(final Type resultType) {
		this.resultType = resultType;
	}

	@Override
	public T parse(HttpResponse res, InputStream in) {
		if (res.getStatusLine().getStatusCode() == 200) {
			Gson gson = new Gson();
			ResponseWrapper<T> wrapper = gson.fromJson(
					new InputStreamReader(in), resultType);
			if (wrapper.resCode == 200) {
				setSucceeded();
				return wrapper.body;
			}
		}
		return null;
	}

}
