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

	public ReferenceExecutionMachine(EndPoint endpoint) throws MalformedURLException,
			RemoteException, InstantiationException, AlreadyBoundException {
		super(endpoint);
	}
	
	/**
	 * @return The last payloaded client count for the Execution Machine
	 * that connected to us
	 */
	public long getLatestClientCount(){
		return latestClientCount;
	}

	@Override
	public void missedBeat(long id) {
		// TODO Uh oh, an Execution Machine dropped
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
