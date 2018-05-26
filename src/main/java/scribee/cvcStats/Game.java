package scribee.cvcStats;

public class Game {
	
	public String date = java.time.LocalDate.now().toString();
	public Team team;
	public int crimsScore = 0;
	public int copsScore = 0;
	public boolean inProgress = false;
	public boolean wonGame = false;
	
	/**
	 * @return String in the format "t:copsScore,ct:crimScore".
	 */
	public String getGameScore() {
		return "t:" + crimsScore + ",ct:" + copsScore;
	}

	/**
	 * @return "Defusal" if the current game is a DefusalGame, "TDM" if the current game is a TDMGame, or "" if it is neither
	 */
	public String getGameType() {
		if (this instanceof DefusalGame)
			return "Defusal";
		else if (this instanceof TDMGame)
			return "TDM";
		else
			return "";
	}
}
