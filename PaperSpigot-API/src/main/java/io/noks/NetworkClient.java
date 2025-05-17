package io.noks;

import java.net.InetSocketAddress;

public interface NetworkClient {
	InetSocketAddress getAddress();
	  
	int getProtocolVersion();
	  
	InetSocketAddress getVirtualHost();
}
