package utils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.ServerSocket;

/**
 * This class is from Luan's work (http://infolab.usc.edu/Luan/Outlier/)
 * @author Luan
 */

/**
 * Some utilities.
 */
public final class Utils {
    public static long peakUsedMemory = 0;
    private static final int MegaBytes = 1024*1024;

    /**
     * Don't let anyone instantiate this class.
     */
	private Utils() {}

    public static long getCPUTime(){
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()? bean.getCurrentThreadCpuTime(): 0L;
    }
    public static int generatePort() {
        int port = (int) (Math.random() * (65535 - 1024 + 1) + 1024);
        while (!isPortAvailable(port)) {
            port = (int) (Math.random() * (65535 - 1024 + 1) + 1024);
        }
        return port; // Return -1 if no available port is found in the range.
    }

    public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // If no exception is thrown, it means the port is available.
            return true;
        } catch (IOException e) {
            // Port is not available because it is already in use.
            return false;
        }
    }
	
}
