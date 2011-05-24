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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Main {
	
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ServerHttpThread serverHttpThread = new ServerHttpThread();
		Thread thread = new Thread(serverHttpThread);
		thread.run();
	}

}
