import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Deployer {

	/**
	 * Depends on:
	 *  - create_machine.sh [>MACHINE ID]
	 *  - get_machine_ip.sh [<MACHINE_ID] [>MACHINE_IP]
	 *  - delete_machine.sh [<MACHINE_ID]
	 *  
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException{
		System.out.println("created machine with ID " + createMachine());
	}
	
	/**
	 * @return The created machine id
	 */
	private static String createMachine() throws InterruptedException, IOException{
		String cmd = "./create_machine.sh" ;
		Runtime run = Runtime.getRuntime() ;
		Process pr = run.exec(cmd) ;
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;

		String output = "";
		
		String line = buf.readLine();
		while (line != null){
			output += line;
		}
		
		Scanner sc = new Scanner(output);
		return sc.next();
	}
	
}
