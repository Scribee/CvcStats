package scribee.cvcStats;

public class DefusalGame extends Game {
	
	public boolean joinedInProgressGame = false;
	private int copKills = 0;
	private int crimKills = 0;
	
	@SuppressWarnings("unused")
	private int crimDeaths = 0;
	@SuppressWarnings("unused")
	private int copDeaths = 0;
	private int bombsDefused = 0;
	private int bombsPlanted = 0;
	
	public DefusalGame(Team startingTeam, boolean joinedInProgressGame) {
		team = startingTeam;
		this.joinedInProgressGame = joinedInProgressGame;
	}

	public void addDefusalRound(DefusalRound round) {
		if (round.winner == Team.COPS)
			copsScore++;
		else if (round.winner == Team.CRIMS)
			crimsScore++;

		if (round.team == Team.COPS) {
			if (round.dead)
				copDeaths++;
			
			copKills += round.kills;
		}
		else if (round.team == Team.CRIMS) {
			if (round.dead)
				crimDeaths++;
			
			crimKills += round.kills;
		}

		if (round.planted)
			bombsPlanted++;
		else if (round.defused)
			bombsDefused++;

		team = round.team; // game team will always be the team that the last round was
	}

	public void swapTeams() {
		int temp = copsScore;
		copsScore = crimsScore;
		crimsScore = temp;
	}

	@Override
	public String toString() {
		return "G," + date + "," + wonGame + "," + team.getID() + "," + copKills + "," + crimKills + "," + getGameScore() + "," + bombsPlanted + "," + bombsDefused + "," + joinedInProgressGame;
	}
}
