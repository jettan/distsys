package distributed.systems.core;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.example.LocalSocket;

/**
 * A serversocket implementation
 */
public class SynchronizedSocket extends Socket {
	private LocalSocket socket;
	
	public SynchronizedSocket() throws RemoteException{
		handlers = new ArrayList<IMessageReceivedHandler>();
		registeredSockets = new ConcurrentHashMap<String, Socket>();
	}

	public SynchronizedSocket(Socket localSocket) throws RemoteException{
		handlers = new ArrayList<IMessageReceivedHandler>();
		registeredSockets = new ConcurrentHashMap<String, Socket>();
		socket = (LocalSocket) localSocket;
	}

	@Override
	public void register(String serverid) throws AlreadyAssignedIDException {
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
	public void receiveMessage(Message reply) {
		// TODO Auto-generated method stub
		
	}
}
