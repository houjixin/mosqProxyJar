package com.xyz.service.mosqproxy.client;

public class Version
{
	/*
	 * 版本号分为三段，形式为： XXX.XXX.XXX �?��边的第一段表示主版本号，中间的第二段表示次版本，右边的第三段表示bug fix�?
	 * 版本号升级请遵循以下规则�?1.在有接口及功能大改动时，主版本号变更�?2.在接口有变更时，次版本变�?3.bug
	 * fix，且不涉及接口变化时，bug fix段更新； 4.主版本号升级时，次版本号和内部修订版本号均从0�?��；次版本号升级时，内部修改版本号�?�?��
	 */
	public static final String VERSION = "0.0.4";
	/**
	 * 版本0.0.4主要完成功能�?1)修改getId接口实现，添加回调参�?
	 * (2)修改日志格式�?）添加VERSION文件�?）添加打包好的jar文件
	 */

}
