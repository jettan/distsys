import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
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
		
		int EXECUTION_MACHINE_COUNT = args.length > 0 ? Integer.parseInt(args[0]) : 5; 
		int PLAYER_COUNT = args.length > 0 ? Integer.parseInt(args[1]) : 100;
		int DRAGON_COUNT = args.length > 0 ? Integer.parseInt(args[2]) : 20;
		
		LinkedList<String> machines = new LinkedList<String>();
		
		/*
		 * Create Central Manager
		 */
		String cm = createMachineWith("CentralManager.jar");
		if (cm != null){
			machines.add(cm);
		} else {
			System.err.println("Some machines could not be created, possible network outage?");
			return;
		}
		String cmip = getMachineIP(cm);
		
		/*
		 * Create Execution Machines
		 */
		for (int i = 0; i < EXECUTION_MACHINE_COUNT; i++){
			String em = createMachineWith("ExecutionMachine.jar", "EXECUTION_MACHINE" + i, cmip);
			if (em != null){
				machines.add(em);
			} else {
				System.err.println("Some machines could not be created, possible network outage?");
				deleteKnownMachines(machines);
				return;
			}
		}
		
		/*
		 * Create Dragons
		 */
		for (int i = 0; i < DRAGON_COUNT; i++){
			String drgn = createMachineWith("Client.jar", "DRAGON", cmip);
			if (drgn != null){
				machines.add(drgn);
			} else {
				System.err.println("Some machines could not be created, possible network outage?");
				deleteKnownMachines(machines);
				return;
			}
		}
		
		/*
		 * Create Players
		 */
		for (int i = 0; i < PLAYER_COUNT; i++){
			String plyr = createMachineWith("Client.jar", "PLAYER", cmip);
			if (plyr != null){
				machines.add(plyr);
			} else {
				System.err.println("Some machines could not be created, possible network outage?");
				deleteKnownMachines(machines);
				return;
			}
		}
		
		System.out.println("System fully deployed with:");
		System.out.println("\t1 CentralManager");
		System.out.println("\t" + EXECUTION_MACHINE_COUNT + " ExecutionMachine");
		System.out.println("\t" + DRAGON_COUNT + " Client DRAGON");
		System.out.println("\t" + PLAYER_COUNT + " Client PLAYER");
	}
	
	private static void deleteKnownMachines(LinkedList<String> machines) throws InterruptedException, IOException{
		for (String machine : machines){
			deleteMachine(machine);
		}
	}
	
	private static String createMachineWith(String deployable, String ... cmdlineargs) throws InterruptedException, IOException{
		String machine = createMachine();
		
		String status = "";
		while (!"RUNNING".equals(status) && !"FAILED".equals(status)){
			Thread.sleep(2000);
			status = getMachineStatus(machine);
		}
		
		if ("FAILED".equals(status)){
			System.err.println("Failed to allocate machine");
			deleteMachine(machine);
			return null;
		}
		
		copyFileToMachine(deployable, machine);
		startDeployableOnMachine(deployable, machine, cmdlineargs);
		
		return machine;
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
		String out = sc.next();
		sc.close();
		return out;
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
		String out = sc.next();
		sc.close();
		return out;
	}
	
	private static String getMachineIP(String machine) throws InterruptedException, IOException{
		String cmd = "./get_machine_ip.sh " + machine;
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
		String out = sc.next();
		sc.close();
		return out;
	}
	
	private static void copyFileToMachine(String file, String machine) throws InterruptedException, IOException{
		String ip = getMachineIP(machine);
		
		String cmd = "scp " + file + " root@" + ip + ":~/";
		Runtime run = Runtime.getRuntime() ;
		Process pr = run.exec(cmd) ;
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;

		String line = buf.readLine();
		while (line != null){
			line = buf.readLine();
		}
	}
	
	private static void startDeployableOnMachine(String file, String machine, String ... cmdlineargs) throws InterruptedException, IOException{
		String ip = getMachineIP(machine);
		
		String args = "";
		for (String arg : cmdlineargs){
			args += " " + arg;
		}
		
		String cmd = "ssh root@" + ip + " java -jar " + file + args;
		Runtime run = Runtime.getRuntime() ;
		Process pr = run.exec(cmd) ;
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;

		String line = buf.readLine();
		while (line != null){
			line = buf.readLine();
		}
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
