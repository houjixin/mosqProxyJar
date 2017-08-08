package com.xyz.service.common;

public class DefaultValues
{
	public static final String DEFAULT_CALLER = "mosq-proxy-client";
	public static final int METRICS_LOGGAP = 10000;//metrics的输出间隔
	public static final String KEY_BASE = "abcdefghijklmnopqrstuvwxyz0123456789?!@#$%^&*()_+-=";//用于生成随机字符串用于加密
	public static final int THRIFT_THREAD_NUM_IO = 10;//thriftIO线程数
	public static final int THRIFT_THREAD_NUM_WORKER = 50;//thrift工作线程数
	public static final int SERVER_MODE_THREAD_POOL = 1;
	public static final int SERVER_MODE_NONBLOCK = 2;
	public static final int SERVER_MODE_THREADEDSELECTOR = 3;

	public static final long ID_ERROR = -1;//错误的ID
	public static final int INVALID_PORT = -1;//无效的端口
	//用户状态
	public static final String CONN_STATUS_ONLINE = "1";//连接在线
	public static final String CONN_STATUS_OFFLINE = "2";//连接不在线
	public static final String CONN_STATUS_ACQUIRED = "0";//已分配，刚刚获得mosquitto server，还没建立tcpip连接
	public static final String CONN_STATUS_ERROR = "-1";//查询用户在线状态错误
	
	public static final String CON_INFO_STATUS = "st";
	public static final String CON_INFO_KEY = "k";
	public static final String CON_INFO_ENCRY_TYPE = "en_t";
	public static final String CON_INFO_INNER_HOST = "hi";
	public static final String CON_INFO_INNER_PORT = "pi";
	public static final String CON_INFO_OUT_HOST = "ho";
	public static final String CON_INFO_OUT_PORT = "po";
	public static final String CON_INFO_SESSION = "ss";
	public static final String CON_INFO_MOSQ_FLAG = "mf";
	
	public static final String RETURN_CODE = "code";
	public static final String RETURN_VALUE = "value";

}
