package distributed.systems.endpoints;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A Class that keeps track of an RMI endpoint
 */
public class EndPoint {

	private final String host;
	private final int port;
	private final String registry;
	
	public EndPoint(String host, int port, String registry){
		this.host = host;
		this.port = port;
		this.registry = registry;
	}
	
	public EndPoint(String host, String registry){
		this.host = host;
		this.port = 1099;
		this.registry = registry;
	}
	
	public EndPoint(String registry){
		this.host = "localhost";
		this.port = 1099;
		this.registry = registry;
	}
	
	public String getHostName(){
		return host;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getRegistryName(){
		return registry;
	}
	
	public String getURI(){
		return "rmi://" + host + ":" + port + "/" + registry;
	}
	
	public <T extends Remote> void open(T r) throws MalformedURLException, RemoteException, AlreadyBoundException, InstantiationException{
		if (!"localhost".equals(host))
			throw new InstantiationException("Tried to open a registry on a remote host");
		Naming.bind(registry, r);
	}
	
	public void close() throws RemoteException, MalformedURLException{
		try {
			Naming.unbind(registry);
		} catch (NotBoundException e) {
		}
	}
	
	public Remote connect() throws MalformedURLException, RemoteException, NotBoundException{
		return Naming.lookup(getURI());
	}
	
	public boolean equals(EndPoint other){
		return getURI().equals(other.getURI());
	}

}
