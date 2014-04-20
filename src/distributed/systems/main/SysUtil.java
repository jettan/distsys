package distributed.systems.main;

import java.io.File;

public class SysUtil {

	public static boolean shouldShutDown(){
		File f = new File("SHUTDOWN");
		return f.exists() && !f.isDirectory();
	}
}
