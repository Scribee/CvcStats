package scribee.cvcStats;

public class DefusalRound {
	public int kills;
	public boolean dead;
	public boolean defused;
	public boolean planted;
	public Team team;
	public Team winner;
	
	public DefusalRound(Team team) {
		this.team = team;
	}
	
	@Override
	public String toString() {
		return team == winner ? team.getID() + "," + kills + "," + dead + ",true" : team.getID() + "," + kills + "," + dead + ",false";
	}
}
