package distributed.systems.example;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;

/**
 * A localhost socket
 */
public class LocalSocket extends Socket implements Serializable {

	private static final long serialVersionUID = 1L;

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
		this.setId(serverid);
	}

	/**
	 * Release our lease on the server id
	 */
	@Override
	public void unRegister() {
		if (!handlers.containsKey(this.id))
			try {
				java.rmi.Naming.unbind(getId());
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
		try {
			System.out.println("[" + id + "] Sending " + reply);
			//System.out.println("handlerList size: " + handlers.size());

			// Check whether we already have the handler for this client.
			if (!handlers.containsKey(origin)) {
				//System.out.println("Handler not found in list, looking up...");
				// Look up the serverid immediately instead of the url since the naming lookup works like this.
				IMessageReceivedHandler remoteReceiver = (IMessageReceivedHandler) java.rmi.Naming.lookup(getServerID(origin));
				handlers.put(origin, remoteReceiver);
				remoteReceiver.onMessageReceived(reply);
			} else {
				//System.out.println("Handler found in list, skipping lookup.");
				handlers.get(origin).onMessageReceived(reply);
			}
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addMessageReceivedHandler(IMessageReceivedHandler handler)  {
		try {
			System.out.println("Trying to bind serverid " + this.id + " to RMI registry.");
			java.rmi.Naming.bind(this.getId(), handler);
			System.out.println("Succesfully bound " + this.id + " to RMI registry.");
			handlers.put(this.id, handler);
		} catch (MalformedURLException | RemoteException
				| AlreadyBoundException e) {
			e.printStackTrace();
		}

	}

	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

}
