package org.spigotmc;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class CachedStreamHandlerFactory implements URLStreamHandlerFactory {
    private static boolean isSet = false;

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("http") || protocol.equals("https")) {
            return new CachedStreamHandler(protocol);
        }
        return null;
    }
    
    public static synchronized void setAsDefault() {
        if (!isSet) {
            URL.setURLStreamHandlerFactory(new CachedStreamHandlerFactory());
            isSet = true;
        }
    }

    public static class CachedStreamHandler extends URLStreamHandler {
        private final String protocol;

        public CachedStreamHandler(String protocol) {
            this.protocol = protocol;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            if (u.getHost().equals("api.mojang.com") || u.getPath().startsWith("/profiles/minecraft")) {
                return new CachedMojangAPIConnection(this, u, null);
            }
            return u.openConnection();
        }

        @Override
        protected URLConnection openConnection(URL u, Proxy p) throws IOException {
            if (u.getHost().equals("api.mojang.com") || u.getPath().startsWith("/profiles/minecraft")) {
                return new CachedMojangAPIConnection(this, u, p);
            }
            return u.openConnection(p);
        }
    }
}
