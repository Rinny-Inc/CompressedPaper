package io.noks.utils;

import java.net.InetSocketAddress;

import javax.annotation.Nullable;

import io.noks.NetworkClient;
import net.minecraft.server.NetworkManager;

public class PaperNetworkClient implements NetworkClient {
	private final NetworkManager networkManager;

	PaperNetworkClient(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public InetSocketAddress getAddress() {
		return (InetSocketAddress) this.networkManager.getSocketAddress();
	}

	public int getProtocolVersion() {
		return this.networkManager.protocolVersions;
	}

	@Nullable
	public InetSocketAddress getVirtualHost() {
		return this.networkManager.virtualHost;
	}

	public static InetSocketAddress prepareVirtualHost(String host, int port) {
		int len = host.length();
		final int pos = host.indexOf('\0');
		if (pos >= 0) {
			len = pos;
		}
		if (len > 0 && host.charAt(len - 1) == '.') {
			len--;
		}
		return InetSocketAddress.createUnresolved(host.substring(0, len), port);
	}
}
