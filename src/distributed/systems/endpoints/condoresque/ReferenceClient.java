package distributed.systems.endpoints.condoresque;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

import distributed.systems.endpoints.EndPoint;
import distributed.systems.endpoints.HeartbeatReceiver;
import distributed.systems.endpoints.IHeartbeatMonitor;

/**
 * For each connected Client we have a ReferenceClient
 * for the Execution Machine to manage the connection to a single Client.
 *
 * This runs on an Execution Machine: should not be constructed by programmers!!
 */
public class ReferenceClient extends HeartbeatReceiver{

	private static final long serialVersionUID = 1L;

	public ReferenceClient(EndPoint endpoint, IHeartbeatMonitor monitor) throws MalformedURLException,
			RemoteException, InstantiationException, AlreadyBoundException {
		super(endpoint, monitor);
	}

}
