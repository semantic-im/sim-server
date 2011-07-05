package sim.server.data;

import org.ontoware.rdf2go.model.node.URI;

/**
 * A generic metric 
 *  
 * @author ioantoma
 * 
 */

public interface Metric {
	
	/**
	 * @return returns the ontological type of the metric
	 */
	public URI getType();
	
	/**
	 * @return returns the value of the metric
	 */
	public double getValue();
	
	/**
	 * @return sets the value of the metric
	 */
	public void setValue(double value);
	
	/**
	 * @return returns the id of the metric
	 */
	public URI getId();
	
	/**
	 * @return sets the id of the metric
	 */
	public void setId(URI uri);
	
	/**
	 * @return returns the time in millis when the metrics was created
	 */
	public long getCreationTime();

	/**
	 * @return returns the id of the method to which the metric is connected
	 */
	public String getMethodId();
	
	/**
	 * @return sets the id of method to which the metric is connected
	 */
	public void setMethodId(String methodId);
	

}
