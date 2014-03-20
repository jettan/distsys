package distributed.systems.endpoints.condoresque;

import java.io.Serializable;

import distributed.systems.endpoints.EndPoint;

public class Allocation implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private final EndPoint main;
	private final EndPoint backup;
	
	public Allocation(EndPoint main, EndPoint backup){
		this.main = main;
		this.backup = backup;
	}

	public EndPoint getMain() {
		return main;
	}

	public EndPoint getBackup() {
		return backup;
	}

}
