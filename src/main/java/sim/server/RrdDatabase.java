/**
 * 
 */
package sim.server;

import static org.rrd4j.ConsolFun.AVERAGE;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import sim.data.Context;
import sim.data.MethodMetrics;
import sim.data.MetricsVisitor;
import sim.data.PlatformMetrics;
import sim.data.SystemMetrics;

/**
 * Maps the java object to individual metrics and uploads them to a RRD database.
 * 
 * @author valer, mcq
 *
 */
public class RrdDatabase implements MetricsVisitor {

	private static final Logger logger = LoggerFactory.getLogger(RrdDatabase.class);
	
	private final String SYSTEM_METRICS_RRD = "./system_metrics.rrd";
	
	private final long RRD_START_TIME = Util.getTimestamp(new Date()) - 60;
	private final int SYSTEM_RRD_STEP = 5; //the step time measured in seconds which is the interval between database updates
	private final int PLATFORM_RRD_STEP = 5; //the step time measured in seconds which is the interval between database updates
	private final int METHOD_RRD_STEP = 60; //the step time measured in seconds which is the interval between database updates
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
	private final String DS_PROCESSES_COUNT = "processesCount";
	private final String DS_RUNNING_PROCESSES_COUNT = "runningProcCount";
	private final String DS_THREADS_COUNT = "threadsCount";
	private final String DS_TCP_INBOUND = "tcpInbound";
	private final String DS_TCP_OUTBOUND = "tcpOutbound";
	private final String DS_NETWORK_RECEIVED = "networkReceived";
	private final String DS_NETWORK_SENT = "networkSent";
	private final String DS_LOOPBACK_NETWORK_RECEIVED = "loNetworkReceived";
	private final String DS_LOOPBACK_NETWORK_SENT = "loNetworkSent";
	
	private final String DS_WALL_CLOCK_TIME = "wallClockTime";
	private final String DS_THREAD_USER_CPU_TIME = "threadUserCPUTime";
	private final String DS_THREAD_SYSTEM_CPU_TIME = "threadSystemCPUTime";
	private final String DS_THREAD_TOTAL_CPU_TIME = "threadTotalCPUTime";
	private final String DS_THREAD_COUNT = "threadCount";
	private final String DS_THREAD_BLOCK_COUNT = "threadBlockCount";
	private final String DS_THREAD_WAIT_COUNT = "threadWaitCount";
	private final String DS_THREAD_GCC_COUNT = "threadGccCount";
	private final String DS_THREAD_GCC_TIME = "threadGccTime";
	private final String DS_PROCESS_TOTAL_CPU_TIME = "processTotalCPUTime";

	private final String DS_PLATFORM_AVG_CPU_USAGE = "avgCpuUsage";
	private final String DS_PLATFORM_CPU_USAGE = "cpuUsage";
	private final String DS_PLATFORM_CPU_TIME = "cpuTime";
	private final String DS_PLATFORM_GCC_COUNT = "gccCount";
	private final String DS_PLATFORM_GCC_TIME = "gccTime";
	private final String DS_PLATFORM_UPTIME = "upTime";
	private final String DS_PLATFORM_ALLOCATED_MEMORY = "allocatedMemory";
	private final String DS_PLATFORM_USED_MEMORY = "usedMemory";
	private final String DS_PLATFORM_FREE_MEMORY = "feeMemory";
	private final String DS_PLATFORM_UNALLOCATED_MEMORY = "unallocatedMemory";
	private final String DS_PLATFORM_PLATFORM_THREADS_COUNT = "threadsCount";
	private final String DS_PLATFORM_THREADS_STARTED = "threadsStarted";
	private final String DS_PLATFORM_THREADS_TOTAL = "totalThreadsStarted";

	private RrdDb systemMetricsRRD = null;
	private Map<String, RrdDb> methodMetricsRRD = new HashMap<String, RrdDb>();
	private Map<String, RrdDb> platformMetricsRRD = new HashMap<String, RrdDb>();
	
	//used to store the last timestamp, for a context, inserted into method metrics database
	private Map<String, Long> contextLastTimestamp = new HashMap<String, Long>();
	
	public RrdDatabase() {		
	}
	
	private void openSystemMetricDb() {
		if (systemMetricsRRD != null && !systemMetricsRRD.isClosed()) {
			return;
		}
		try {
			if (!new File(SYSTEM_METRICS_RRD).exists()) {
				RrdDef rrdDef = new RrdDef(SYSTEM_METRICS_RRD, RRD_START_TIME, SYSTEM_RRD_STEP);
				
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
				rrdDef.addDatasource(DS_PROCESSES_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_RUNNING_PROCESSES_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREADS_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_TCP_INBOUND, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_TCP_OUTBOUND, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_NETWORK_RECEIVED, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_NETWORK_SENT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_LOOPBACK_NETWORK_RECEIVED, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_LOOPBACK_NETWORK_SENT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 12 * 60); //detail last day, one record each 5 seconds
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 12 * 10, 6 * 24 * 7); //detail last week, one record each 10 minutes
				
				systemMetricsRRD = new RrdDb(rrdDef);
			} else {
				systemMetricsRRD = new RrdDb(SYSTEM_METRICS_RRD);
			}
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
	}

	private void closeSystemMetricDb() {
		try {
			systemMetricsRRD.close();
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
	}

	private String getDatabasePath(MethodMetrics methodMetric) {		
		return methodMetric.getSystemId().getId() + "_" + methodMetric.getMethod().getApplicationId().getId() + (methodMetric.getContextId() == null ? "" : "_" + methodMetric.getContextId());
	}


	private RrdDb openMethodMetricDb(MethodMetrics methodMetric) {
		String dbPath = getDatabasePath(methodMetric);
		RrdDb methodMetricRrd = methodMetricsRRD.get(dbPath);
		if (methodMetricRrd != null && !methodMetricRrd.isClosed()) {
			return methodMetricRrd;
		}
		try {
			String rrdDbPath = dbPath + ".rrd";
			if (!new File(rrdDbPath).exists()) {
				RrdDef rrdDef = new RrdDef(rrdDbPath, RRD_START_TIME, METHOD_RRD_STEP);
				
				rrdDef.addDatasource(DS_WALL_CLOCK_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_USER_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_SYSTEM_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_TOTAL_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_BLOCK_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_WAIT_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_GCC_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_THREAD_GCC_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PROCESS_TOTAL_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 60); //detail last day, one record each 5 seconds
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 6 * 24 * 7); //detail last week, one record each 10 minutes
				
				methodMetricsRRD.put(dbPath, new RrdDb(rrdDef));
			} else {
				methodMetricsRRD.put(dbPath, new RrdDb(rrdDbPath));
			}
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
		return methodMetricsRRD.get(dbPath);
	}

	private void closeMethodMetricDb(MethodMetrics methodMetric) {
		String methodId = getDatabasePath(methodMetric);
		RrdDb methodRrd = methodMetricsRRD.get(methodId);
		if (methodRrd != null) {
			if (!methodRrd.isClosed()) {
				try {
					methodRrd.close();
				} catch (IOException e) {
					logger.error("exception closing rrd database : " + methodId, e);
					throw new RuntimeException("exception closing rrd database : " + methodId, e);
				}
			}
			methodMetricsRRD.remove(methodId);
		}
	}
	/* (non-Javadoc)
	 * @see sim.data.MetricsVisitor#visit(sim.data.MethodMetrics)
	 */
	@Override
	public void visit(MethodMetrics methodMetrics) {
		// stop storing method metrics for rrd; they are not used and rrd is not a good fit for these metrics
		return;
//		RrdDb methodRrd = openMethodMetricDb(methodMetrics);
//		try {
//			Sample sample = methodRrd.createSample();
//			
//			long time = 0;
//			Object obj = methodMetrics.getSystemId().getName() + "_" + methodMetrics.getMethod().getApplicationId().getName() + (methodMetrics.getContextId() == null ? "" : "_" + methodMetrics.getContextId());
//			synchronized(obj) {
//				time = Util.getTimestamp(new Date(methodMetrics.getCreationTime()));
//				
//				String contextId = methodMetrics.getContextId() == null ? "null" : methodMetrics.getContextId();
//				//logger.info("contextId: " + contextId);
//				//logger.info("timestamp: " + time);
//				long lastTimestamp = -1;
//				if (contextLastTimestamp.containsKey(contextId)) {
//					lastTimestamp = contextLastTimestamp.get(contextId);
//				}
//				//logger.info("lastTimestamp: " + lastTimestamp);
//				if (contextLastTimestamp.containsKey(contextId) && time <= lastTimestamp) {
//					time = lastTimestamp + 1;
//				}
//				contextLastTimestamp.put(contextId, time);
//			}
//			sample.setTime(time);
//			sample.setValue(DS_WALL_CLOCK_TIME, methodMetrics.getWallClockTime());
//			sample.setValue(DS_THREAD_USER_CPU_TIME, methodMetrics.getThreadUserCpuTime());
//			sample.setValue(DS_THREAD_SYSTEM_CPU_TIME, methodMetrics.getThreadSystemCpuTime());
//			sample.setValue(DS_THREAD_TOTAL_CPU_TIME, methodMetrics.getThreadTotalCpuTime());
//			sample.setValue(DS_THREAD_COUNT, methodMetrics.getThreadCount());
//			sample.setValue(DS_THREAD_BLOCK_COUNT, methodMetrics.getThreadBlockCount());
//			sample.setValue(DS_THREAD_WAIT_COUNT, methodMetrics.getThreadWaitCount());
//			sample.setValue(DS_THREAD_GCC_COUNT, methodMetrics.getThreadGccCount());
//			sample.setValue(DS_THREAD_GCC_TIME, methodMetrics.getThreadGccTime());
//			sample.setValue(DS_PROCESS_TOTAL_CPU_TIME, methodMetrics.getProcessTotalCpuTime());
//			
//			sample.update();
//		} catch (IOException e) {
//			logger.error("io exception", e);
//			throw new RuntimeException("io exception", e);
//		}
//		closeMethodMetricDb(methodMetrics);
	}

	/* (non-Javadoc)
	 * @see sim.data.MetricsVisitor#visit(sim.data.SystemMetrics)
	 */
	@Override
	public void visit(SystemMetrics systemMetrics) {
		openSystemMetricDb();
		try {
			Sample sample = systemMetricsRRD.createSample();
			
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
			sample.setValue(DS_SYSTEM_CPU_LOAD, systemMetrics.getSysPerc());
			sample.setValue(DS_IDLE_CPU_LOAD, systemMetrics.getIdlePerc());
			sample.setValue(DS_WAIT_CPU_LOAD, systemMetrics.getWaitPerc());
			sample.setValue(DS_IRQ_CPU_LOAD, systemMetrics.getIrqPerc());
			sample.setValue(DS_USER_CPU_TIME, systemMetrics.getUser());
			sample.setValue(DS_SYSTEM_CPU_TIME, systemMetrics.getSys());
			sample.setValue(DS_IDLE_CPU_TIME, systemMetrics.getIdle());
			sample.setValue(DS_WAIT_CPU_TIME, systemMetrics.getWait());
			sample.setValue(DS_IRQ_CPU_TIME, systemMetrics.getIrq());
			sample.setValue(DS_PROCESSES_COUNT, systemMetrics.getProcessesCount());
			sample.setValue(DS_RUNNING_PROCESSES_COUNT, systemMetrics.getRunningProcessesCount());
			sample.setValue(DS_THREADS_COUNT, systemMetrics.getThreadsCount());
			sample.setValue(DS_TCP_INBOUND, systemMetrics.getTcpInbound());
			sample.setValue(DS_TCP_OUTBOUND, systemMetrics.getTcpOutbound());
			sample.setValue(DS_NETWORK_RECEIVED, systemMetrics.getNetworkReceived());
			sample.setValue(DS_NETWORK_SENT, systemMetrics.getNetworkSent());
			sample.setValue(DS_LOOPBACK_NETWORK_RECEIVED, systemMetrics.getLoopbackNetworkReceived());
			sample.setValue(DS_LOOPBACK_NETWORK_SENT, systemMetrics.getLoopbackNetworkSent());
			
			sample.update();
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
		closeSystemMetricDb();
	}
	
	public void fetchData() throws IOException {
	    long start = Util.getTimestamp(2011, 4, 20, 17, 0);
	    long end= Util.getTimestamp(2011, 4, 20, 18, 0);
        System.out.println("== Fetching data for the whole month");
        FetchRequest request = systemMetricsRRD.createFetchRequest(ConsolFun.AVERAGE, start, end);
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

	@Override
	public void visit(Context context) {
		// do nothing; context metrcis are not stored into rrd
	}
	
	private String getDatabasePath(PlatformMetrics pm) {		
		return "platform_" + pm.getSystemId().getId() + "_" + pm.getApplicationId().getId();
	}
	
	private RrdDb openPlatformMetricDb(PlatformMetrics pm) {
		String dbPath = getDatabasePath(pm);
		RrdDb platformMetricRrd = platformMetricsRRD.get(dbPath);
		if (platformMetricRrd != null && !platformMetricRrd.isClosed()) {
			return platformMetricRrd;
		}
		try {
			String rrdDbPath = dbPath + ".rrd";
			if (!new File(rrdDbPath).exists()) {
				RrdDef rrdDef = new RrdDef(rrdDbPath, RRD_START_TIME, PLATFORM_RRD_STEP);
				
				rrdDef.addDatasource(DS_PLATFORM_AVG_CPU_USAGE, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_CPU_USAGE, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_CPU_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_GCC_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_GCC_TIME, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_UPTIME, DsType.COUNTER, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_ALLOCATED_MEMORY, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_USED_MEMORY, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_FREE_MEMORY, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_UNALLOCATED_MEMORY, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_PLATFORM_THREADS_COUNT, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_THREADS_STARTED, DsType.GAUGE, RRD_HEARBEAT, 0, Double.NaN);
				rrdDef.addDatasource(DS_PLATFORM_THREADS_TOTAL, DsType.COUNTER, RRD_HEARBEAT, 0, Double.NaN);
				
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 60); //detail last day, one record each 5 seconds
				rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 6 * 24 * 7); //detail last week, one record each 10 minutes
				
				platformMetricsRRD.put(dbPath, new RrdDb(rrdDef));
			} else {
				platformMetricsRRD.put(dbPath, new RrdDb(rrdDbPath));
			}
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
		return platformMetricsRRD.get(dbPath);
	}

	private void closePlatformMetricDb(PlatformMetrics pm) {
		String platformId = getDatabasePath(pm);
		RrdDb platformRrd = platformMetricsRRD.get(platformId);
		if (platformRrd != null) {
			if (!platformRrd.isClosed()) {
				try {
					platformRrd.close();
				} catch (IOException e) {
					logger.error("exception closing rrd database : " + platformRrd, e);
					throw new RuntimeException("exception closing rrd database : " + platformRrd, e);
				}
			}
			platformMetricsRRD.remove(platformId);
		}
	}
	
	@Override
	public void visit(PlatformMetrics pm) {
		RrdDb platformRrd = openPlatformMetricDb(pm);
		try {
			Sample sample = platformRrd.createSample();
			sample.setTime(Util.getTimestamp(new Date(pm.getCreationTime())));		
			
			sample.setValue(DS_PLATFORM_AVG_CPU_USAGE, pm.getAvgCpuUsage());
			sample.setValue(DS_PLATFORM_CPU_USAGE, pm.getCpuUsage());
			sample.setValue(DS_PLATFORM_CPU_TIME, pm.getCpuTime());
			sample.setValue(DS_PLATFORM_GCC_COUNT, pm.getGccCount());
			sample.setValue(DS_PLATFORM_GCC_TIME, pm.getGccTime());
			sample.setValue(DS_PLATFORM_UPTIME, pm.getUptime());
			sample.setValue(DS_PLATFORM_ALLOCATED_MEMORY, pm.getAllocatedMemory());
			sample.setValue(DS_PLATFORM_USED_MEMORY, pm.getUsedMemory());
			sample.setValue(DS_PLATFORM_FREE_MEMORY, pm.getFreeMemory());
			sample.setValue(DS_PLATFORM_UNALLOCATED_MEMORY, pm.getUnallocatedMemory());
			sample.setValue(DS_PLATFORM_PLATFORM_THREADS_COUNT, pm.getThreadsCount());
			sample.setValue(DS_PLATFORM_THREADS_STARTED, pm.getThreadsStarted());
			sample.setValue(DS_PLATFORM_THREADS_TOTAL, pm.getTotalThreadsStarted());
			
			sample.update();
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
		closePlatformMetricDb(pm);
	}
	
}
