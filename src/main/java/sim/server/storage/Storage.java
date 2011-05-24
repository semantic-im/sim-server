package sim.server.storage;

import java.io.IOException;

/**
 * @author ioantoma
 * 
 */
public interface Storage {

	public String read(String metric) throws IOException;

	public long write(String metric, long data) throws IOException;
}
