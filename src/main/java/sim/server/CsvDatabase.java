/**
 * 
 */
package sim.server;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.data.Context;
import sim.data.MethodMetrics;
import sim.data.MetricsVisitor;
import sim.data.PlatformMetrics;
import sim.data.SystemMetrics;

/**
 * @author valer
 *
 */
public class CsvDatabase implements MetricsVisitor {

	private static final Logger logger = LoggerFactory.getLogger(CsvDatabase.class);
	
	
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
	
	


	public CsvDatabase() {		
	}
	
	private PrintStream out;
	
	public void open() {
		logger.info("Opening CSV file "+Main.storage_csv_file);
		if(out!=null)return;
		try {
			boolean writeHeader = false;
			if(!new File(Main.storage_csv_file).exists()) {
				writeHeader = true;
			}
			out = new PrintStream(new FileOutputStream(Main.storage_csv_file, true));
			if(out==null) {
				logger.error("Unable to open CSV file for appending: "+Main.storage_csv_file);
				return;
			}
			if (writeHeader) {
				logger.info("New CSV file, writing header");
				out.println("timeStamp"+", "+ 
							DS_SYSTEM_LOAD_AVERAGE+", "+
							DS_TOTAL_SYSTEM_FREE_MEMEORY+", "+
							DS_TOTAL_SYSTEM_USED_MEMORY+", "+
							DS_TOTAL_SYSTEM_USED_SWAP+", "+
							DS_SYSTEM_OPEN_FILE_DESCRIPTOR_COUNT+", "+
							DS_SWAP_IN+", "+
							DS_SWAP_OUT+", "+
							DS_IO_READ+", "+
							DS_IO_WRITE+", "+
							DS_USER_CPU_LOAD+", "+
							DS_SYSTEM_CPU_LOAD+", "+
							DS_IDLE_CPU_LOAD+", "+
							DS_WAIT_CPU_LOAD+", "+
							DS_IRQ_CPU_LOAD+", "+
							DS_USER_CPU_TIME+", "+
							DS_SYSTEM_CPU_TIME+", "+
							DS_IDLE_CPU_TIME+", "+
							DS_WAIT_CPU_TIME+", "+
							DS_IRQ_CPU_TIME);
			} 
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		}
	}
	
	public void close() {
		logger.info("Closing CSV file");
		out.close();
		out=null;
	}

	@Override
	public void visit(MethodMetrics methodMetrics) {}

	@Override
	public void visit(SystemMetrics systemMetrics) {
		if(out==null) {
			open();
		}
		out.print(""+systemMetrics.getCreationTime());
		
		out.print(", "+systemMetrics.getSystemLoadAverage());
		out.print(", "+systemMetrics.getTotalSystemFreeMemory());
		out.print(", "+systemMetrics.getTotalSystemUsedMemory());
		out.print(", "+systemMetrics.getTotalSystemUsedSwap());
		out.print(", "+systemMetrics.getSystemOpenFileDescriptors());
		out.print(", "+systemMetrics.getSwapIn());
		out.print(", "+systemMetrics.getSwapOut());
		out.print(", "+systemMetrics.getIORead());
		out.print(", "+systemMetrics.getIOWrite());
		out.print(", "+systemMetrics.getUserPerc());
		out.print(", "+systemMetrics.getSysPerc());
		out.print(", "+systemMetrics.getIdlePerc());
		out.print(", "+systemMetrics.getWaitPerc());
		out.print(", "+systemMetrics.getIrqPerc());
		out.print(", "+systemMetrics.getUser());
		out.print(", "+systemMetrics.getSys());
		out.print(", "+systemMetrics.getIdle());
		out.print(", "+systemMetrics.getWait());
		out.print(", "+systemMetrics.getIrq());

		out.println();
	}
	
	
	@Override
	public void visit(Context context) {}
		
	@Override
	public void visit(PlatformMetrics pm) {}
	
}
