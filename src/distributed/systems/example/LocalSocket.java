package distributed.systems.example;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import distributed.systems.core.IRMIReceiver;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;

/**
 * A localhost socket
 */
public class LocalSocket extends Socket {
	
	private String id;
	
	public LocalSocket() throws RemoteException {
		super();
	}

	/**
	 * Claim a serverid name for ourselves
	 * 
	 * TODO broadcast synchronize with other servers??
	 * 
	 * @param serverid The id to claim
	 * @throws AlreadyAssignedIDException if the socket could not be bound or the id already exists
	 */
	public void register(String serverid) throws AlreadyAssignedIDException{
		this.id = serverid;
		try {
			java.rmi.Naming.bind(serverid, this);
			System.out.println("Bound to " + serverid);
		} catch (MalformedURLException | RemoteException
				| AlreadyBoundException e) {
			e.printStackTrace();
			throw new AlreadyAssignedIDException();
		}

	}

	/**
	 * Release our lease on the server id
	 */
	@Override
	public void unRegister() {
		if (id != null)
			try {
				java.rmi.Naming.unbind(id);
			} catch (RemoteException | MalformedURLException
					| NotBoundException e) {
				e.printStackTrace();
			}
	}

	/**
	 * Send a message over our socket
	 */
	@Override
	public void sendMessage(Message reply, String origin)
			throws IDNotAssignedException {
		IRMIReceiver remoteReceiver;
		String rmiURL = "rmi://localhost:1099/" + getServerID(origin);
		System.out.println("Looking up: " + rmiURL);
		try {
			remoteReceiver = (IRMIReceiver) java.rmi.Naming.lookup(rmiURL);
			remoteReceiver.receiveMessage(reply);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(Message reply) {
		System.out.println("We received a message!");
		// TODO Auto-generated method stub
		
	}

}
