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
	 *  - get_machine_status.sh [<MACHINE_ID]
	 *  
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException{
		String machine = createMachine();
		System.out.println("created machine with ID " + machine);
		
		String status = "";
		while (!"RUNNING".equals(status) && !"FAILED".equals(status)){
			Thread.sleep(2000);
			status = getMachineStatus(machine);
		}
		
		System.out.println("Finished boot phase of machine " + machine + " with status " + status);
		System.out.println("Removing");
		
		deleteMachine(machine);
	}
	
	/**
	 * @return The created machine id
	 */
	private static String createMachine() throws InterruptedException, IOException{
		String cmd = "./create_machine.sh";
		Runtime run = Runtime.getRuntime();
		Process pr = run.exec(cmd);
		pr.waitFor();
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;

		String output = "";
		
		String line = buf.readLine();
		while (line != null){
			output += line;
			line = buf.readLine();
		}
		
		Scanner sc = new Scanner(output);
		return sc.next();
	}
	
	private static String getMachineStatus(String machine) throws InterruptedException, IOException{
		String cmd = "./get_machine_status.sh " + machine;
		Runtime run = Runtime.getRuntime() ;
		Process pr = run.exec(cmd) ;
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;

		String output = "";
		
		String line = buf.readLine();
		while (line != null){
			output += line;
			line = buf.readLine();
		}
		
		Scanner sc = new Scanner(output);
		return sc.next();
	}
	
	private static void deleteMachine(String machine) throws InterruptedException, IOException{
		String cmd = "./delete_machine.sh " + machine;
		Runtime run = Runtime.getRuntime() ;
		Process pr = run.exec(cmd) ;
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;

		String line = buf.readLine();
		while (line != null){
			line = buf.readLine();
		}
	}
	
}
