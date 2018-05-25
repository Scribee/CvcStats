package scribee.cvcStats;

public enum Team {
	COPS("ct"), 
	CRIMS("t");
	
	private final String id;
	
	private Team(String id) {
		this.id = id;
	}
	
	public Team getOpposite() {
		if (id.equals("ct"))
			return COPS;
		
		return CRIMS;
	}
	
	public String getID() {
		return id;
	}
}
