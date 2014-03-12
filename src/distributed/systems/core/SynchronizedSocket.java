package distributed.systems.core;

import java.io.Serializable;
import java.rmi.RemoteException;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.example.LocalSocket;

/**
 * A serversocket implementation
 */
public class SynchronizedSocket extends Socket implements Serializable{


	private static final long serialVersionUID = 1L;
	private LocalSocket socket;
	
	private String id;
	
	public SynchronizedSocket() throws RemoteException{
		super();
	}

	public SynchronizedSocket(Socket localSocket) throws RemoteException{
		super();
		socket = (LocalSocket) localSocket;
	}

	@Override
	public void register(String serverid) throws AlreadyAssignedIDException {
		this.id = serverid;
		socket.register(serverid);
	}

	@Override
	public void unRegister() {
		socket.unRegister();
	}

	@Override
	public void sendMessage(Message reply, String origin)
			throws IDNotAssignedException {
		socket.sendMessage(reply, origin);
		
	}

	@Override
	public void addMessageReceivedHandler(IMessageReceivedHandler handler) {
		socket.addMessageReceivedHandler(handler);
	}
}
