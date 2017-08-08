package com.xyz.service.mosqproxy.client;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.service.common.DefaultValues;
import com.xyz.service.common.ReturnCode;
import com.xyz.service.common.Tools;
import com.xyz.service.conn.pool.RpcClientInfo;
import com.xyz.service.conn.pool.RpcStubMosqProxyFactory;
import com.xyz.service.conn.pool.RpcStubPool;
import com.xyz.service.conn.pool.RpcStubPoolConfig;
import com.xyz.service.data.ConnectionInfo;
import com.xyz.service.data.RegResult;
import com.xyz.service.thrift.datatype.ResBool;
import com.xyz.service.thrift.datatype.ResSetStr;
import com.xyz.service.thrift.datatype.ResStr;
import com.xyz.service.thrift.stub.MosqProxyStub;


public class MosqProxyClient
{
	private String getClassName(){return "MosqProxyClient";}
	private static Logger logger = LoggerFactory.getLogger(MosqProxyClient.class);
	private RpcStubMosqProxyFactory rpcfactory = null;
	private RpcStubPool<RpcClientInfo<MosqProxyStub.Client>> rpcstubpool = null;
	private RpcStubPoolConfig config = new RpcStubPoolConfig();
	private int timeout = 2000;
	private int serverMode = 3;
	private String sHost = "";
	private int sPort = -1;
	public MosqProxyClient()
	{
		config.setMaxActive(100);
		config.setMaxIdle(100);
		config.setMaxWait(3000);
		config.setTestWhileIdle(false);
		config.setMinEvictableIdleTimeMillis(3600000);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(false);
	}
	public boolean init(String serverHost, int serverPort, int serverMode,RpcStubPoolConfig config,int timeout)
	{
		this.timeout = timeout;
		this.config = config;
		this.serverMode = serverMode;
		this.sHost = serverHost;
		this.sPort = serverPort;
		return innerInit();
	}
	
	private boolean innerInit()
	{
		long logIndex = Tools.getId();
		String logFlag = getClassName() + "innerInit";
		logger.info("[lid:{}][{}] serverHost:{} serverPort:{} serverMode:{}", logIndex, logFlag, this.sHost, this.sPort, this.serverMode);
		rpcfactory = new RpcStubMosqProxyFactory(this.sHost, this.sPort, this.serverMode, this.timeout);
		rpcstubpool = new RpcStubPool<>();
		if (!rpcstubpool.init(logIndex, this.config, rpcfactory))
		{
			logger.warn("[lid:{}][{}] rpcpool init failed!", logIndex, logFlag);
			return false;
		}
		return true;
	}

	public boolean init(String serverHost, int serverPort)
	{
		if(!Tools.checkPort(serverPort) || StringUtils.isBlank(serverHost))
			return false;
		this.sHost = serverHost;
		this.sPort = serverPort;
		return innerInit();
	}

	private RpcClientInfo<MosqProxyStub.Client> getServiceClient(long logIndex)
	{
		String logFlag = "MosqProxyClient.getServiceClient";
		RpcClientInfo<MosqProxyStub.Client> rpcconnect = null;
		try
		{
			rpcconnect = rpcstubpool.getResource(logIndex);
			return rpcconnect;
		}
		catch (Exception e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, logFlag, e);
			rpcstubpool.returnBrokenResource(logIndex, rpcconnect);
			return null;
		}
	}
	
	public RegResult forceRegisterConnection(String appId, String userId, String devId){
		return registerConnection(Tools.getId(), DefaultValues.DEFAULT_CALLER, appId, userId, devId, true);
	}
	
	
	public RegResult registerConnection(String appId, String userId, String devId){
		return registerConnection(Tools.getId(), DefaultValues.DEFAULT_CALLER, appId, userId, devId, false);
	}
	
	public RegResult registerConnection(long logIndex, String caller, String appId, String userId, String devId, boolean forceRegister)
	{
		String logFlag = getClassName() + ".registerConnection";
		logger.debug("[lid:{}][{}] appId:{}, userId:{}, devId:{}", logIndex, logFlag, caller, appId, userId, devId);
		if(StringUtils.isBlank(caller) || 
				StringUtils.isBlank(appId) || 
				StringUtils.isBlank(userId) || 
				StringUtils.isBlank(devId)){
			logger.error("[lid:{}][{}]parameter error! caller:{} appId:{}, userId:{}, devId:{}", logIndex, logFlag, caller, appId, userId, devId);
			return null;
			
		}
		RegResult regResult = null;
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return null;
			}
			ResStr res;
			if(forceRegister)
				res = ci.getClient().registerConnection(logIndex, caller, appId, userId, devId, null);
			else
				res = ci.getClient().forceRegisterConnection(logIndex, caller, appId, userId, devId, null);
			if (res.res == ReturnCode.SUCCESS && !StringUtils.isBlank(res.value)){
				ConnectionInfo connInfo = ConnectionInfo.fromJSONString(res.value);
				if(connInfo == null){
					logger.debug("[lid:{}][{}] res:{}", logIndex, caller, res.value);
				}
				regResult = new RegResult(res.res, connInfo);
			}
			else
				logger.warn("[lid:{}][{}] register connection fail!, result:{}", logIndex, caller, res);
			rpcstubpool.returnResource(logIndex, ci);
			return regResult;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return null;
		}
	}
	
	public String bindConnId(long logIndex, String caller, String appId, String userId, String connId, String devId, boolean isForceBind)
	{
		String logFlag = getClassName() + ".bindConnId";
		logger.debug("[lid:{}][{}] appId:{}, userId:{}, connId:{}, devId:{}, isForceBind:{}", logIndex, logFlag, caller, appId, userId, connId, devId, isForceBind);
		if(StringUtils.isBlank(caller) || 
				StringUtils.isBlank(appId) || 
				StringUtils.isBlank(userId) || 
				StringUtils.isBlank(connId) || 
				StringUtils.isBlank(devId)){
			logger.error("[lid:{}][{}]parameter error! caller:{} appId:{}, userId:{}, connId:{}, devId:{}", logIndex, logFlag, caller, appId, userId, connId, devId);
			return null;
			
		}
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return null;
			} 
			ResStr res;
			if(isForceBind)
				res = ci.getClient().bindConnId(logIndex, caller, appId, userId, devId, connId, null);
			else
				res = ci.getClient().forceBindConnId(logIndex, caller, appId, userId, devId, connId, null);
			rpcstubpool.returnResource(logIndex, ci);
			return res.value;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return null;
		}
	}
	
	public String forceBindConnId(long logIndex, String caller, String appId, String userId, String connId, String devtype){
		return bindConnId(logIndex, caller, appId, userId, connId, devtype, true);
	}
	
	public boolean sendMsgToUser(long logIndex, String caller, String appId, String userId, String topic, String msg)
	{
		String logFlag = getClassName() + ".sendMsgToUser";
		logger.debug("[lid:{}][{}] caller:{}; appId:{}, userId:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, userId, topic, msg);
		if(StringUtils.isBlank(caller) || 
				StringUtils.isBlank(appId) || 
				StringUtils.isBlank(userId) || 
				StringUtils.isBlank(topic) || 
				msg == null){
			logger.error("[lid:{}][{}]parameter error! caller:{} appId:{}, userId:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, userId, topic, msg);
			return false;
			
		}
		boolean res = false;
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return false;
			} 
			ResBool rpcResult = ci.getClient().sendMsgToUser(logIndex, caller, appId, userId, topic, msg, null);
			if (rpcResult.res == ReturnCode.SUCCESS){
				res = rpcResult.value;
			}
			rpcstubpool.returnResource(logIndex, ci);
			return res;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return false;
		}
	}
	
	public boolean sendMsgToDevType(long logIndex, String caller, String appId, String userId, String devType, String topic, String msg)
	{
		String logFlag = getClassName() + ".sendMsgToDevType";
		logger.debug("[lid:{}][{}] caller:{}; appId:{}, userId:{}, devType:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, userId, devType, topic, msg);
		if(StringUtils.isBlank(caller) || 
				StringUtils.isBlank(appId) || 
				StringUtils.isBlank(userId) || 
				StringUtils.isBlank(topic) || 
				msg == null){
			logger.error("[lid:{}][{}]parameter error! caller:{} appId:{}, userId:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, userId, topic, msg);
			return false;
			
		}
		boolean res = false;
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return false;
			} 
			ResBool rpcResult = ci.getClient().sendMsgToDevType(logIndex, caller, appId, userId, devType, topic, msg, null);
			if (rpcResult.res == ReturnCode.SUCCESS){
				res = rpcResult.value;
			}
			rpcstubpool.returnResource(logIndex, ci);
			return res;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return false;
		}
	}
	
	public boolean sendBroadcastMsg(long logIndex, String caller, String appId, String topic, String msg)
	{
		String logFlag = getClassName() + ".sendBroadcastMsg";
		logger.debug("[lid:{}][{}] appId:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, topic, msg);
		if(StringUtils.isBlank(caller) || 
				StringUtils.isBlank(appId) || 
				StringUtils.isBlank(topic) || 
				msg == null){
			logger.error("[lid:{}][{}]parameter error! caller:{} appId:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, topic, msg);
			return false;
			
		}
		boolean res = false;
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return false;
			} 
			ResBool rpcResult = ci.getClient().sendBroadcastMsg(logIndex, caller, appId, topic, msg, null);
			if (rpcResult.res == ReturnCode.SUCCESS){
				res = rpcResult.value;
			}
			rpcstubpool.returnResource(logIndex, ci);
			return res;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return false;
		}
	}
	
	
	public Set<String> sendMsgToUsers(long logIndex, String caller, String appId, Set<String> dstUserIds, String topic, String msg)
	{
		String logFlag = getClassName() + ".sendMsgToUsers";
		logger.debug("[lid:{}][{}] appId:{}, dstUserIds:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, dstUserIds, topic, msg);
		if(StringUtils.isBlank(caller) || 
				StringUtils.isBlank(appId) || 
				dstUserIds == null ||
				dstUserIds.isEmpty() ||
				StringUtils.isBlank(topic) || 
				msg == null){
			logger.error("[lid:{}][{}]parameter error! caller:{} appId:{}, dstUserIds:{}, topic:{}, msg:{}", logIndex, logFlag, caller, appId, dstUserIds, topic, msg);
			return dstUserIds;
			
		}
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return null;
			} 
			ResSetStr rpcResult = ci.getClient().sendMsgToUsers(logIndex, caller, appId, dstUserIds, topic, msg, null);
			rpcstubpool.returnResource(logIndex, ci);
			return rpcResult.value;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return null;
		}
	}
	
	public ConnectionInfo getConnection(){
		return getConnection(Tools.getId(), DefaultValues.DEFAULT_CALLER);
	}
	
	public ConnectionInfo getConnection(long logIndex, String caller){
//		String logFlag = getClassName() + ".getConnection";
		ConnectionInfo connInfo = ConnectionInfo.fromJSONString(getStrConnection(logIndex, caller));
		return (connInfo != null && connInfo.isValid()) ? connInfo : null;
	}
	
	public String getStrConnection(long logIndex, String caller){
		String logFlag = getClassName() + ".getStrConnection";
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return null;
			} 
			ResStr rpcResult = ci.getClient().getConnection(logIndex, caller, null);
			rpcstubpool.returnResource(logIndex, ci);
			return rpcResult.value;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return null;
		}
	}
	
	public void shutDown()
	{
		long logIndex = Tools.getId();
		String logFlag = "MosqProxyClient.shutDown";
		if (rpcstubpool == null)
			return;
		try
		{
			rpcstubpool.destroy(logIndex);
			rpcstubpool = null;
		}
		catch (Exception ex)
		{
			logger.error("[lid:{}][{}] exception happened while shutting down rpcstubpool...", logIndex, logFlag);
		}
	}
	
	
	public String getClientStatus(long logIndex, String caller, String appId, String userId, String devId)
	{
		String logFlag = getClassName() + ".getClientStatus";
		logger.debug("[lid:{}][{}] appId:{}, userId:{}, devId:{}", logIndex, logFlag, caller, appId, userId, devId);
		if(StringUtils.isBlank(caller) || 
				StringUtils.isBlank(appId) || 
				StringUtils.isBlank(userId) || 
				StringUtils.isBlank(devId)){
			logger.error("[lid:{}][{}]parameter error! caller:{} appId:{}, userId:{}, devId:{}", logIndex, logFlag, caller, appId, userId, devId);
			return null;
			
		}
		RpcClientInfo<MosqProxyStub.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex);
			if (ci == null)
			{
				logger.warn("[lid:{}][{}] cann't get mosqProxy Client", logIndex, logFlag);
				return null;
			} 
			ResStr res = ci.getClient().getClientStatus(logIndex, caller, appId, userId, devId, null);
			rpcstubpool.returnResource(logIndex, ci);
			return res.value;
		}
		catch (TException e)
		{
			logger.warn("[lid:{}][{}] exception happened! detail:\n{}", logIndex, caller, e);
			rpcstubpool.returnBrokenResource(logIndex, ci);
			return null;
		}
	}
	
	public void shutdown(long logIndex){
		
	}
}
