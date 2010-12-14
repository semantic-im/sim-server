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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.data.Metrics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * @author mcq
 * 
 */
/**
 * @author mcq
 * 
 */
@SuppressWarnings("restriction")
public class HttpCommunicationHandler implements HttpHandler {
	private static final Logger log = LoggerFactory.getLogger(HttpCommunicationHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange
	 * )
	 */
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(xchg.getRequestBody());
		Object o = null;
		try {
			while (true) {
				try {
					o = ois.readObject();
				} catch (EOFException e) {
					log.debug("no more data to read, closing connection ...");
					break;
				}
				if (o instanceof Metrics) {
					log.info(o.toString());
					processMeasurement((Metrics) o);
				}
			}
		} catch (ClassNotFoundException e) {
			log.error("class not found", e);
			throw new RuntimeException("class not found", e);
		}
		xchg.sendResponseHeaders(200, "SUCCESS".length());
		OutputStream os = xchg.getResponseBody();
		os.write("SUCCESS".getBytes());
		os.close();
	}

	/**
	 * Maps the java object to semantic concepts, uploads to semantic and RRD
	 * database.
	 * 
	 * @param measurement
	 */
	private void processMeasurement(Metrics measurement) {
		// TODO: add here code
	}

}
