package distributed.systems.endpoints.condoresque;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.HeartbeatReceiver;

/**
 * For each connected Execution Machine we have a ReferenceExecutionMachine
 * for the Central Manager to manage the connection to a single Execution Machine.
 *
 * This runs on a Central Manager: should not be constructed by programmers!!
 */
public class ReferenceExecutionMachine extends HeartbeatReceiver implements Comparable<ReferenceExecutionMachine> {

	private static final long serialVersionUID = 1L;
	
	private transient long latestClientCount = 0;
	private transient final int id;

	public ReferenceExecutionMachine(int id) throws MalformedURLException,
			RemoteException, InstantiationException, AlreadyBoundException {
		super(new EndPoint("REFEXMACHINE_" + id));
		this.id = id;
	}
	
	/**
	 * @return The last payloaded client count for the Execution Machine
	 * that connected to us
	 */
	public long getLatestClientCount(){
		return latestClientCount;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return "REFEXMACHINE_" + id;
	}
	
	public String getRemoteName(){
		return "EXECUTION_MACHINE_" + id;
	}

	@Override
	public void receivePayload(Serializable payload) {
		if (payload instanceof Integer){
			latestClientCount = (Integer) payload;
		} else {
			System.err.println("Unknown payload received in heartbeat, ignoring");
		}
	}

	@Override
	public int compareTo(ReferenceExecutionMachine o) {
		return Long.compare(latestClientCount, o.getLatestClientCount());
	}

}
