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
 * This class is responsible to start up the SIM-Server application.
 * 
 * SIM-Server acts as a measurements collector for all agents. Measurements are
 * then transformed into semantic concepts and then uploaded into a semantic
 * database. Some of the measurements are also uploaded into a RRD database.
 * 
 * @author mcq
 * 
 */
@SuppressWarnings("restriction")
public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private static final int SERVER_PORT = 8099;
	private static final String SERVER_PATH = "/server";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		startHttpCommunicationServer();
	}

	public static void startHttpCommunicationServer() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
			server.createContext(SERVER_PATH, new HttpCommunicationHandler());
			server.start();
		} catch (IOException e) {
			log.error("failed to start http communication server", e);
		}
	}

}
