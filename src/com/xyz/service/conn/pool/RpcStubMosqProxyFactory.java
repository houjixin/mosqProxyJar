package com.xyz.service.conn.pool;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.service.common.DefaultValues;
import com.xyz.service.common.ReturnCode;
import com.xyz.service.thrift.datatype.ResStr;
import com.xyz.service.thrift.stub.MosqProxyStub;



//业务处理对象，创建对象，验证对象
public class RpcStubMosqProxyFactory extends BasePoolableObjectFactory<RpcClientInfo<MosqProxyStub.Client>>
{
	private final String host;
	private final int port;
	private final int serverMode;
	private final int timeout;
//	public AtomicInteger logId = new AtomicInteger(-1);
	private static Logger logger = LoggerFactory.getLogger(RpcStubMosqProxyFactory.class);
	private AtomicLong innerId = new AtomicLong(0);
	public long getId(){
		return innerId.getAndIncrement();
	}
	private String getClassName()
	{// 仅用于内部获取类的最短名�?
		return "RpcStubMosqProxyFactory";
	}

	public RpcStubMosqProxyFactory(final String host, final int port, final int mode, final int timeout)
	{
		super();
		this.host = host;
		this.port = port;
		this.serverMode = mode;
		this.timeout = timeout;
	}

	@Override
	public RpcClientInfo<MosqProxyStub.Client> makeObject() throws Exception
	{
		String logFlag = getClassName() + ".makeObject";
		TTransport transport = null;
		TProtocol protocol = null;
		TSocket tsocket = null;
		if (serverMode == DefaultValues.SERVER_MODE_THREAD_POOL)
		{
			tsocket = new TSocket(this.host, this.port, this.timeout);
			transport = tsocket;
			protocol = new TBinaryProtocol(transport);
		}
		else if (serverMode == DefaultValues.SERVER_MODE_NONBLOCK || serverMode == DefaultValues.SERVER_MODE_THREADEDSELECTOR)
		{
			tsocket = new TSocket(this.host, this.port, this.timeout);
			transport = new TFramedTransport(tsocket);
			// 协议要和服务端一致
			protocol = new TBinaryProtocol(transport);
		}
		RpcClientInfo<MosqProxyStub.Client> rpctype = new RpcClientInfo<MosqProxyStub.Client>(new MosqProxyStub.Client(protocol), transport, tsocket);
		try
		{
			rpctype.getTTransport().open();
			RpcStubPool.iNum.incrementAndGet();
			return rpctype;
		}
		catch (TTransportException e)
		{
			logger.warn("[lid:{}][{}]getTTransport failed! TTransportException detail:{}", getId(), logFlag, e);
			return null;
		}
	}

	@Override
	public void destroyObject(RpcClientInfo<MosqProxyStub.Client> obj) throws Exception
	{
		String logFlag = getClassName() + ".destroyObject";
		if (obj == null)
		{
			logger.warn("[lid:{}][{}]Object is empty!", getId(), logFlag);
			return;
		}
		RpcClientInfo<MosqProxyStub.Client> rpctype = obj;
		rpctype.getTTransport().close();
		RpcStubPool.iNum.decrementAndGet();
	}

	@Override
	public boolean validateObject(RpcClientInfo<MosqProxyStub.Client> obj)
	{
		String logFlag = getClassName() + ".validateObject";
		if (obj == null)
		{
			logger.warn("[lid:{}][{}]Object is empty!", getId(), logFlag);
			return false;
		}
		try
		{
			RpcClientInfo<MosqProxyStub.Client> rpctype = obj;
			if (!checkSocket(rpctype.getTSocket().getSocket()))
			{
				logger.warn("[lid:{}][{}] checkSocket fail!", getId(), logFlag);
				return false;
			}
			ResStr resStr = rpctype.getClient().echo(getId(), logFlag, "OK", null);
			if (resStr.res != ReturnCode.SUCCESS)
			{
				logger.warn("[lid:{}][{}] err result!", getId(), logFlag);
				return false;
			}
			return resStr.value.equals("OK");
		}
		catch (Exception e)
		{
			logger.warn("[lid:{}][{}]validateObject fail! Exception detail:{}", getId(), logFlag, e);
			return false;
		}
	}
	public boolean checkSocket(Socket socket)
	{
		return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
	}
}
