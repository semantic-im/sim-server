/*
 * Copyright 2010 Softgress - http://www.softgress.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package sim.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

/**
 * Thread running the Http Server listening for method metrics
 * 
 * @author valer
 *
 */
@SuppressWarnings("restriction")
public class ServerHttpThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ServerHttpThread.class);
	
	private static final int SERVER_PORT = 8099;
	private static final String SERVER_PATH = "/server";
	
	public ServerHttpThread() {
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {		
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
			server.createContext(SERVER_PATH, new ServerHttpHandler());
			server.start();
		} catch (IOException e) {
			logger.error("failed to start http communication server", e);
		}
	}

}
