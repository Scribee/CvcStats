package scribee.cvcStats;

public class DefusalRound {
	public int kills = 0;
	public int assists = 0;
	public boolean dead = false;
	public boolean defused = false;
	public boolean planted = false;
	public Team team;
	public Team winner;
	
	public DefusalRound(Team team) {
		this.team = team;
	}
	
	@Override
	public String toString() {
		return team == winner ? team.getID() + "," + assists + "," + kills + "," + dead + ",true" : team.getID() + "," + kills + "," + dead + ",false";
	}
}
