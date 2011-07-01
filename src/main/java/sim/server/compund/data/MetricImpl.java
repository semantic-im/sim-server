package sim.server.compund.data;

import org.ontoware.rdf2go.model.node.URI;

import sim.data.MetricsVisitor;

/**
 * Implementation for {@link Metric}.
 * 
 * @author ioantoma
 * 
 */

public class MetricImpl implements Metric{

	private static final long serialVersionUID = 2549005240716116739L;
	
	private URI id;
	private URI type;
	private double value;
	private long creationTime;

	public MetricImpl(URI type){
		this.type = type;
		this.creationTime = System.currentTimeMillis();
		this.value = 0;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void accept(MetricsVisitor visitor) {
		// TODO Auto-generated method stub
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public URI getType() {
		return this.type;
	}
	
	@Override
	public void setValue(double value){
		this.value = value;
	}

	@Override
	public URI getId() {
		return id;
	}

	@Override
	public void setId(URI id) {
		this.id = id;
	}
	
}
