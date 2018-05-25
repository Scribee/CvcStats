package scribee.cvcStats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

/**
 * Written by Aaron1998ish on 11/12/2017.
 *
 * Useful methods for retrieving data on scoreboards on Hypixel
 */
public class ScoreboardUtil {

    /**
     * Filters scores from a scoreboards objective and returns the values in a list.
     *
     * Output was designed around Hypixel's scoreboard setup.
     *
     * @param scoreboard The target scoreboard.
     * @return If no scores were found, an empty list is returned.
     */
    public static List<String> getSidebarScores(Scoreboard scoreboard) {
        List<String> found = new ArrayList<>();

        ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebar != null) {
            List<Score> scores = new ArrayList<>(scoreboard.getScores());


            /*Scores retrieved here do not care for ordering, this is done by the Scoreboard itself.
              We'll need to do this our selves in this case.

              This will appear backwards in chat, but remember that the scoreboard reverses this order
              to ensure highest scores go first.
             */
            scores.sort(Comparator.comparingInt(Score::getScorePoints));
            
            for (Score score : scores) {
            	String name = score.getPlayerName(); // not actually used for text by most servers
            	System.out.println(getPrefixFromContainingTeam(scoreboard, name) + "\n" + name + "\n" + getSuffixFromContainingTeam(scoreboard, name));
            	found.add(getPrefixFromContainingTeam(scoreboard, name) + getSuffixFromContainingTeam(scoreboard, name));
            }
        }
        return found;
    }

    /**
     * Filters through Scoreboard teams searching for a team
     * that contains the last part of our scoreboard message.
     *
     * @param scoreboard The target scoreboard.
     * @param member The message we're searching for inside a teams member collection.
     * @return If no team was found, an empty suffix is returned.
     */
    private static String getSuffixFromContainingTeam(Scoreboard scoreboard, String member) {
        String suffix = null;
        for (ScorePlayerTeam team : scoreboard.getTeams()) {
            if (team.getMembershipCollection().contains(member)) {
                suffix = team.getColorSuffix();
                break;
            }
        }
        return (suffix == null ? "" : suffix);
    }
    
    /**
     * Filters through Scoreboard teams searching for a team
     * that contains the main part of our scoreboard message.
     *
     * @param scoreboard The target scoreboard
     * @param member The message we're searching for inside a teams member collection.
     * @return If no team was found, an empty prefix is returned.
     */
    private static String getPrefixFromContainingTeam(Scoreboard scoreboard, String member) {
        String prefix = null;
        for (ScorePlayerTeam team : scoreboard.getTeams()) {
            if (team.getMembershipCollection().contains(member)) {
                prefix = team.getColorPrefix();
                break;
            }
        }
        return (prefix == null ? "" : prefix);
    }
}