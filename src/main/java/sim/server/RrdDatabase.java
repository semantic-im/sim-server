/**
 * 
 */
package sim.server;

import static org.rrd4j.ConsolFun.AVERAGE;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.data.MethodMetrics;
import sim.data.MetricsVisitor;
import sim.data.SystemMetrics;

/**
 * @author valer
 *
 */
public class RrdDatabase implements MetricsVisitor {

	private static final Logger logger = LoggerFactory.getLogger(RrdDatabase.class);
	
	private final String SYSTEM_METRICS_RRD = "./system_metrics.rrd";
	
	private final long RRD_START_TIME = Util.getTimestamp(new Date()) - 1;
	private final int RRD_STEP = 60; //the step time measured in seconds which is the interval between database updates
	private final int RRD_HEARBEAT = 600; //Defines the minimum heartbeat, the maximum number of seconds that can go by before a DS value is considered unknown.
	
	private final String DS_SYSTEM_LOAD_AVERAGE = "sysLoadAverage";
	private final String DS_TOTAL_SYSTEM_FREE_MEMEORY = "totalSysFreeMemory";
	private final String DS_TOTAL_SYSTEM_USED_MEMORY = "totalSysUsedMemory";
	private final String DS_TOTAL_SYSTEM_USED_SWAP = "totalSysUsedSwap";
	private final String DS_SYSTEM_OPEN_FILE_DESCRIPTOR_COUNT = "sysOpenFileDescrCnt";
	private final String DS_SWAP_IN = "swapIn";
	private final String DS_SWAP_OUT = "swapOut";
	private final String DS_IO_READ = "iORead";
	private final String DS_IO_WRITE = "iOWrite";
	private final String DS_USER_CPU_LOAD = "userCPULoad";
	private final String DS_SYSTEM_CPU_LOAD = "systemCPULoad";
	private final String DS_IDLE_CPU_LOAD = "idleCPULoad";
	private final String DS_WAIT_CPU_LOAD = "waitCPULoad";
	private final String DS_IRQ_CPU_LOAD = "irqCPULoad";
	private final String DS_USER_CPU_TIME = "userCPUTime";
	private final String DS_SYSTEM_CPU_TIME = "systemCPUTime";
	private final String DS_IDLE_CPU_TIME = "idleCPUTime";
	private final String DS_WAIT_CPU_TIME = "waitCPUTime";
	private final String DS_IRQ_CPU_TIME = "irqCPUTime";
	
	private RrdDb rrdDb = null;
	
	public RrdDatabase() {		
	}
	
	public void open() {
		try {
			if (!new File(SYSTEM_METRICS_RRD).exists()) {
				RrdDef rrdDef = new RrdDef(SYSTEM_METRICS_RRD, RRD_START_TIME, RRD_STEP);
				
				rrdDef.addDatasource(DS_SYSTEM_LOAD_AVERAGE, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_TOTAL_SYSTEM_FREE_MEMEORY, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_TOTAL_SYSTEM_USED_MEMORY, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_TOTAL_SYSTEM_USED_SWAP, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_SYSTEM_OPEN_FILE_DESCRIPTOR_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_SWAP_IN, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_SWAP_OUT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_IO_READ, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_IO_WRITE, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_USER_CPU_LOAD, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_SYSTEM_CPU_LOAD, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_IDLE_CPU_LOAD, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_WAIT_CPU_LOAD, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_IRQ_CPU_LOAD, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_USER_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_SYSTEM_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_IDLE_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_WAIT_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_IRQ_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 60); //detail last hour, one record each minute
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 6 * 24); //detail last day, one record each 10 minutes
				
				rrdDb = new RrdDb(rrdDef);
			} else {
				rrdDb = new RrdDb(SYSTEM_METRICS_RRD);
			}
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
	}

	public void close() {
		try {
			rrdDb.close();
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
	}

	/* (non-Javadoc)
	 * @see sim.data.MetricsVisitor#visit(sim.data.MethodMetrics)
	 */
	@Override
	public void visit(MethodMetrics methodMetrics) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see sim.data.MetricsVisitor#visit(sim.data.SystemMetrics)
	 */
	@Override
	public void visit(SystemMetrics systemMetrics) {
		try {
			Sample sample = rrdDb.createSample();
			
			sample.setTime(Util.getTimestamp(new Date(systemMetrics.getCreationTime())));
			sample.setValue(DS_SYSTEM_LOAD_AVERAGE, systemMetrics.getSystemLoadAverage());
			sample.setValue(DS_TOTAL_SYSTEM_FREE_MEMEORY, systemMetrics.getTotalSystemFreeMemory());
			sample.setValue(DS_TOTAL_SYSTEM_USED_MEMORY, systemMetrics.getTotalSystemUsedMemory());
			sample.setValue(DS_TOTAL_SYSTEM_USED_SWAP, systemMetrics.getTotalSystemUsedSwap());
			sample.setValue(DS_SYSTEM_OPEN_FILE_DESCRIPTOR_COUNT, systemMetrics.getSystemOpenFileDescriptors());
			sample.setValue(DS_SWAP_IN, systemMetrics.getSwapIn());
			sample.setValue(DS_SWAP_OUT, systemMetrics.getSwapOut());
			sample.setValue(DS_IO_READ, systemMetrics.getIORead());
			sample.setValue(DS_IO_WRITE, systemMetrics.getIOWrite());
			sample.setValue(DS_USER_CPU_LOAD, systemMetrics.getUserPerc());
			sample.setValue(DS_USER_CPU_LOAD, systemMetrics.getSysPerc());
			sample.setValue(DS_IDLE_CPU_LOAD, systemMetrics.getIdlePerc());
			sample.setValue(DS_WAIT_CPU_LOAD, systemMetrics.getWaitPerc());
			sample.setValue(DS_IRQ_CPU_LOAD, systemMetrics.getIrqPerc());
			sample.setValue(DS_USER_CPU_TIME, systemMetrics.getUser());
			sample.setValue(DS_SYSTEM_CPU_TIME, systemMetrics.getSys());
			sample.setValue(DS_IDLE_CPU_TIME, systemMetrics.getIdle());
			sample.setValue(DS_WAIT_CPU_TIME, systemMetrics.getWait());
			sample.setValue(DS_IRQ_CPU_TIME, systemMetrics.getIrq());
			
			sample.update();
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
	}
	
	public void fetchData() throws IOException {
	    long start = Util.getTimestamp(2011, 4, 20, 17, 0);
	    long end= Util.getTimestamp(2011, 4, 20, 18, 0);
        System.out.println("== Fetching data for the whole month");
        FetchRequest request = rrdDb.createFetchRequest(ConsolFun.AVERAGE, start, end);
        System.out.println(request.dump());
        logger.info(request.dump());
        FetchData fetchData = request.fetchData();
        System.out.println("== Data fetched. " + fetchData.getRowCount() + " points obtained");
        System.out.println(fetchData.toString());
        System.out.println("== Dumping fetched data to XML format");
        System.out.println(fetchData.exportXml());
        System.out.println("== Fetch completed");
	}
	
	public void createGraph() {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(500);
		gDef.setHeight(300);
		gDef.setFilename("./image.png");
		gDef.setStartTime(Util.getTimestamp(2011, 4, 20, 17, 0));
		gDef.setEndTime(Util.getTimestamp(2011, 4, 20, 18, 0));
		gDef.setTitle("My Title");
		gDef.setVerticalLabel("bytes");

		gDef.datasource(DS_SYSTEM_LOAD_AVERAGE, SYSTEM_METRICS_RRD, DS_SYSTEM_LOAD_AVERAGE, ConsolFun.AVERAGE);
		gDef.hrule(0.5, Color.GREEN, "hrule");
		gDef.setImageFormat("png");

		gDef.line(DS_SYSTEM_LOAD_AVERAGE, Color.GREEN, "sun temp");
		
		gDef.gprint(DS_SYSTEM_LOAD_AVERAGE, AVERAGE, "average = %10.3f %s");
		
		// then actually draw the graph
		try {
			RrdGraph graph = new RrdGraph(gDef); // will create the graph in the path specified
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		RrdDatabase rrdDatabase = new RrdDatabase();
		//rrdDatabase.open();
		rrdDatabase.createGraph();
		//rrdDatabase.fetchData();
	}
}
