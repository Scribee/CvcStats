package scribee.cvcStats;

public class TDMGame extends Game {
	
	public int points = 0; // points that the player has earned; team points are stored in crimsScore and copsScore
	public int kills = 0;
	public int assists = 0;
	public int deaths = 0;
	
	public TDMGame() {
		
	}
	
	@Override
	public String toString() {
		return date + "," + wonGame + "," + team.getID() + "," + assists + "," + kills + "," + deaths + "," + points + "," + getGameScore();
	}
}
