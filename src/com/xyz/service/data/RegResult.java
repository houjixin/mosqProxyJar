package com.xyz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.xyz.service.common.DefaultValues;

/**
 * Created by Jason on 2017/4/7.
 */
public class RegResult{
	//特别注意返回码为：BIND_EXIST的情况
	private int code;
    private ConnectionInfo conInfo;
    public RegResult(int retCode, ConnectionInfo conInfo){
        this.code = retCode;
        this.conInfo = conInfo;
    }
    public ConnectionInfo getConnectionInfo(){
    	return conInfo;
    }
    public String toJsonString(){
        JSONObject joRes = new JSONObject();
        joRes.put(DefaultValues.RETURN_CODE, code);
        joRes.put(DefaultValues.RETURN_VALUE, conInfo);
        return joRes.toJSONString();
    }
    
    public String getValue() {
    	if(conInfo != null)
    		return conInfo.toOutSimpleString();
        return null;
    }

}
