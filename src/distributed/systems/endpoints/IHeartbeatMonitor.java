package distributed.systems.endpoints;

public interface IHeartbeatMonitor {
	
	public void missedBeat(EndPoint remote);
	
}
