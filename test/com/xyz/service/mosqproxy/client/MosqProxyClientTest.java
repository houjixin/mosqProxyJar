package com.xyz.service.mosqproxy.client;

import org.junit.Test;

import com.xyz.service.data.RegResult;

public class MosqProxyClientTest
{
	private String serverHost = "11.12.112.201";
	private int serverPort = 5812;
	@Test
	public void test()
	{
		MosqProxyClient mosqProxyClient = new MosqProxyClient();
		if(!mosqProxyClient.init(serverHost, serverPort)){
			System.exit(0);
		}
		String appId = "atp";
		String userId = "user-001";
		String devId = "android";
		RegResult regRes= mosqProxyClient.registerConnection(appId, userId, devId);
		System.out.println("res:" + regRes);
	}

}
