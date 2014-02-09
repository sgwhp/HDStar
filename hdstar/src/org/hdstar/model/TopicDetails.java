package org.hdstar.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * 帖子详情. <br/>
 * 日期: 2014年2月9日 上午9:29:53 <br/>
 * 
 * @author robust
 * @history 2014年2月9日 robust 新建
 */
public class TopicDetails implements Serializable {

	private static final long serialVersionUID = 5239480189685048085L;
	public String title;
	public ArrayList<Post> posts;

}