package scribee.cvcStats;

public class DefusalGame extends Game {
	
	private boolean joinedInProgressGame = false;
	
	private int copKills = 0;
	private int crimKills = 0;
	
	@SuppressWarnings("unused")
	private int copAssists = 0;
	@SuppressWarnings("unused")
	private int crimAssists = 0;
	
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
		if (round.winner == Team.COPS) {
			copsScore++;
		}
		else if (round.winner == Team.CRIMS) {
			crimsScore++;
		}

		if (round.team == Team.COPS) {
			if (round.dead) {
				copDeaths++;
			}
			
			copAssists += round.assists;
			copKills += round.kills;
		}
		else if (round.team == Team.CRIMS) {
			if (round.dead) {
				crimDeaths++;
			}
			
			crimAssists += round.assists;
			crimKills += round.kills;
		}

		if (round.planted) {
			bombsPlanted++;
		}
		else if (round.defused) {
			bombsDefused++;
		}

		team = round.team; // means game team will always be the team that the last round was
	}

	public void swapTeams() {
		int temp = copsScore;
		copsScore = crimsScore;
		crimsScore = temp;
	}

	@Override
	public String toString() {
		return "G," + date + "," + wonGame + "," + team.getID() + "," + copAssists + "," + copKills + "," + crimAssists + "," + crimKills + "," + getGameScore() + "," + bombsPlanted + "," + bombsDefused + "," + joinedInProgressGame;
	}
}
