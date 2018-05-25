package scribee.cvcStats;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@Mod(modid = Reference.MODID, name = Reference.MOD_NAME, version = Reference.VERSION, clientSideOnly = true, canBeDeactivated = true)
public class CvcStats {

	public static Configuration config;
	
	private static final String CATEGORY_MISC = "Miscellaneous";
	private static final String S = String.valueOf('\u00a7');
	
	private static String name = "";
	private static String currentMap;
	private static Game currentGame;
	private static DefusalRound currentRound;
	@SuppressWarnings("unused")
	private static boolean gettingMap = false;
	
	// matches kill and death messages
	private static Pattern killfeedPattern = Pattern.compile(S + "r" + S + "[34](\\w{1,16}) " + S + "r" + S + "f(\\W\\W?\\W?) " + S + "r" + S + "[34](\\w{1,16})");
	// matches end of tdm game message
	private static Pattern tdmScoresPattern = Pattern.compile(S + "(3" + String.valueOf('\u9290') + "|4" + String.valueOf('\u9291') + ")" + S + "f Points: " + S + "a(\\d{1,4})" + S + "7\\/1,000");
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CvcStatsCommand());
	}

	@SubscribeEvent
	public void onChatEvent(ClientChatReceivedEvent event) {
		String message = event.message.getFormattedText();
		
		if (name.equals("")) {
			name = Minecraft.getMinecraft().thePlayer.getDisplayNameString();
			System.out.println(name);
			
			if (!config.getString("Name", CATEGORY_MISC, "", "Name of the player").equals(name))
				config.get(CATEGORY_MISC, "Name", "").set(name);
		}

		Matcher matcher = killfeedPattern.matcher(message);
		if (matcher.find()) {
			if (matcher.group(1).equals(name)) {
				if (currentGame.getGameType().equals("Defusal"))
					currentRound.kills++;
				else if (currentGame.getGameType().equals("TDM"))
					((TDMGame) currentGame).kills++;
				updateGunStats(matcher.group(2));
			}
			else if (matcher.group(3).equals(name))
				if (currentGame.getGameType().equals("Defusal"))
					currentRound.dead = true;
				else if (currentGame.getGameType().equals("TDM"))
					((TDMGame) currentGame).deaths++;
		}
		
		else if (message.length() > 37 && message.substring(0, 37).equals(S + "r" + S + "aYou are currently playing on " + S + "r" + S + "e")) {
			currentMap = message.substring(37, message.length() - 2);
			System.out.println("Map: " + currentMap);
			gettingMap = false;
		}
		
		// when defusal round starts
		// §r             §r§eYou are a Cop! Prevent the bomb from being§r
		// §r                  §r§eYou are a Criminal! Protect the bomb§r
		// §r              §r§eYou are a Criminal! Get to a bomb site and§r
		else if (message.equals(S + "r             " + S + "r" + S + "eYou are a Cop! Prevent the bomb from being" + S + "r")) {
			System.out.println("Cop instructions");
			// if first round of game
			if (currentGame == null || !currentGame.inProgress) {
				System.out.println("new game");
				
				Minecraft.getMinecraft().thePlayer.sendChatMessage("/map");
				gettingMap = true;
				
				currentRound = new DefusalRound(Team.COPS);
				
				currentGame = new DefusalGame(currentRound.team, false);
				currentGame.inProgress = true;
			}
			// if teams were just switched (current team is different from last team)
			else if (currentGame.team == Team.CRIMS) {
				System.out.println("swap teams");
				
				currentRound = new DefusalRound(Team.COPS);
				((DefusalGame) currentGame).swapTeams();
			}
		}
		else if (message.equals(S + "r                  " + S + "r" + S + "eYou are a Criminal! Protect the bomb" + S + "r") || message.equals(S + "r              " + S + "r" + S + "eYou are a Criminal! Get to a bomb site and" + S + "r")) {
			System.out.println("crim info");
			if (currentGame == null || !currentGame.inProgress) {
				System.out.println("new game");
				
				Minecraft.getMinecraft().thePlayer.sendChatMessage("/map");
				gettingMap = true;
				
				currentRound = new DefusalRound(Team.CRIMS);

				currentGame = new DefusalGame(currentRound.team, false);
				currentGame.inProgress = true;
			}
			// if teams were just switched (current team is different from last team)
			else if (currentGame.team == Team.COPS) {
				System.out.println("swap teams");
				
				currentRound = new DefusalRound(Team.CRIMS);
				((DefusalGame) currentGame).swapTeams();
			}
		}
		// TDM game starts
		// §r               §r§eon challenges to earn points! First team§r
		else if (message.equals(S + "r               " + S + "r" + S + "eon challenges to earn points! First team" + S + "r")) {
			System.out.println("tdm info");
			
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/map");
			gettingMap = true;
			currentGame = new TDMGame();
			
			new ScheduledCode(() -> getTDMTeam(), 5);
			
			currentGame.inProgress = true;
		}

		else if (currentGame != null && currentGame.inProgress) {
			if (currentGame.getGameType().equals("Defusal")) {
				if (message.length() > 5 && message.substring(0, 5).equals(S + "r" + S + "6+")) {
					// if planted
					// message.length() > 18 && 
					if (currentRound.team == Team.CRIMS && message.contains(" (Planted Bomb")) {
						System.out.println("planted");
						currentRound.planted = true;
					}
					// if defused
					else if (currentRound.team == Team.COPS && message.contains(" (Defused Bomb")) {
						System.out.println("defused");
						currentRound.defused = true;
					}
				}

				// when defusal round ends
				// §r                      §r§4§lCriminals won the round!§r
				// §r                         §r§3§lCops won the round!§r
				else if (message.equals(S + "r                      " + S + "r" + S + "4" + S + "lCriminals won the round!" + S + "r")) {
					System.out.println("Crims win");
					currentRound.winner = Team.CRIMS;
					saveStats(currentRound.toString());

					((DefusalGame) currentGame).addDefusalRound(currentRound);
					currentRound = new DefusalRound(currentRound.team);
				}
				else if (message.equals(S + "r                         " + S + "r" + S + "3" + S + "lCops won the round!" + S + "r")) {
					System.out.println("Cops win");
					currentRound.winner = Team.COPS;
					saveStats(currentRound.toString());

					((DefusalGame) currentGame).addDefusalRound(currentRound);
					currentRound = new DefusalRound(currentRound.team);
				}
				
				// when defusal game ends
				// this message is also shown in TDM games, but I don't want to use it as well
				// §r                       §r§4§lCriminals won the game!§r
				else if (message.equals(S + "r                       " + S + "r" + S + "4" + S + "lCriminals won the game!" + S + "r")) {
					if (currentGame.team == Team.CRIMS) {
						currentGame.wonGame = true;
					}

					currentRound.winner = Team.CRIMS;
					saveStats(currentRound.toString());

					saveStats(currentGame.toString());
					currentGame.inProgress = false;
				}
				else if (message.equals(S + "r                          " + S + "r" + S + "3" + S + "lCops won the game!" + S + "r")) {
					if (currentGame.team == Team.COPS) {
						currentGame.wonGame = true;
					}

					currentRound.winner = Team.COPS;
					saveStats(currentRound.toString());

					saveStats(currentGame.toString());
					currentGame.inProgress = false;
				}
			}
			
			// when TDM game ends
			// §r                      §r§7Crims reached enough points!§r
			// §r                      §r§7Cops reached enough points!§r
			else if (message.equals(S + "r                      " + S + "r" + S + "7Crims reached enough points!" + S + "r")) {
				if (currentGame.team == Team.CRIMS) {
					currentGame.wonGame = true;
				}
				
				List<String> sbValues = new ArrayList<String>();
				sbValues = ScoreboardUtil.getSidebarScores(Minecraft.getMinecraft().theWorld.getScoreboard());
				// Your Points: §a0
				// §4?§f Points: §a0§7/1,000
				// §3?§f Points: §a0§7/1,000				
				for (String score : sbValues) {
					if (score.length() >= 15 && score.substring(0,15).equals("Your Points: " + S + "a")) {
						((TDMGame) currentGame).points = Integer.parseInt(score.substring(15));
						continue;
					}
					Matcher tdmMatcher = tdmScoresPattern.matcher(score);
					if (tdmMatcher.find()) {
						if (tdmMatcher.group(1).equals("4" + String.valueOf('\u9291'))) {
							currentGame.crimsScore = Integer.parseInt(tdmMatcher.group(2));
						}
						else if (tdmMatcher.group(1).equals("3" + String.valueOf('\u9290'))) {
							currentGame.copsScore = Integer.parseInt(tdmMatcher.group(2));
						}
					}
				}
				
				saveStats(currentGame.toString());
				currentGame.inProgress = false;
			}
			else if (message.equals(S + "r                      " + S + "r" + S + "7Cops reached enough points!" + S + "r")) {
				if (currentGame.team == Team.COPS) {
					currentGame.wonGame = true;
				}
				
				List<String> sbValues = new ArrayList<String>();
				sbValues = ScoreboardUtil.getSidebarScores(Minecraft.getMinecraft().theWorld.getScoreboard());
				// Your Points: §a0
				// §4?§f Points: §a0§7/1,000
				// §3?§f Points: §a0§7/1,000
				for (String score : sbValues) {
					System.out.println(score);
					if (score.substring(0,15).equals("Your Points: " + S + "a")) {
						((TDMGame) currentGame).points = Integer.parseInt(score.substring(15));
						continue;
					}
					Matcher tdmMatcher = tdmScoresPattern.matcher(score);
					if (tdmMatcher.find()) {
						if (tdmMatcher.group(1).equals("4" + String.valueOf('\u9291'))) {
							currentGame.crimsScore = Integer.parseInt(tdmMatcher.group(2));
						}
						else if (tdmMatcher.group(1).equals("3" + String.valueOf('\u9290'))) {
							currentGame.copsScore = Integer.parseInt(tdmMatcher.group(2));
						}
					}
				}
				
				saveStats(currentGame.toString());
				currentGame.inProgress = false;
			}
		}
		// suicide nade
		else if ((message.startsWith(S + "r" + S + "f" + String.valueOf('\u9283') + " ") || message.startsWith(S + "r" + S + "f" + String.valueOf('\u9279') + " ")) && message.substring(10).equals(name + S + "r")) {
			config.get(CATEGORY_MISC, "suicidenades", 0).set(config.get(CATEGORY_MISC, "suicidenades", 0).getInt() + 1);
			currentRound.dead = true;
		}
		// died from C4
		else if (message.startsWith(S + "r" + S + "f" + String.valueOf('\u9276') + " ") && message.substring(10).equals(name + S + "r")) {
			config.get(CATEGORY_MISC, "c4deaths", 0).set(config.get(CATEGORY_MISC, "c4deaths", 0).getInt() + 1);
			currentRound.dead = true;
		}
		// died from fall damage
		else if (message.startsWith(S + "r" + S + "f" + String.valueOf('\u9271') + String.valueOf('\u9272') + " ") && message.substring(11).equals(name + S + "r")) {
			config.get(CATEGORY_MISC, "falldeaths", 0).set(config.get(CATEGORY_MISC, "falldeaths", 0).getInt() + 1);
			currentRound.dead = true;
		}
	}
	
	@SubscribeEvent
	public void onPlayerLeaveEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		if (currentGame != null && currentGame.inProgress) {
			if (currentGame.getGameType().equals("Defusal")) {
				currentRound.dead = true;
				currentRound.winner = currentRound.team.getOpposite();
				saveStats(currentRound.toString());

				((DefusalGame) currentGame).addDefusalRound(currentRound);
				saveStats(currentGame.toString());
			}
			else if (currentGame.getGameType().equals("TDM")) {
				currentGame.inProgress = false;
			}
		}
	}
	
	private static void saveStats(String message) {
		String[] original = config.getStringList(currentMap, currentGame.getGameType(), new String[] { "" }, "List of stats for " + currentMap);
		if (original.length == 0) {
			config.get(currentGame.getGameType(), // Category will be the game type, like "Defusal"
					currentMap, // Name of the array will be the name of the map
					new String[] { "" },
					"List of stats for " + currentMap).set(new String[] { message });
		}
		else {
			String[] updated = new String[original.length + 1];
			for (int i = 0; i < original.length; i++) {
				updated[i] = original[i];
			}
			updated[updated.length - 1] = message;

			config.get(currentGame.getGameType(), currentMap, new String[] { "" }, "List of stats for " + currentMap).set(updated);
		}
		config.save();
	}

	private static void updateGunStats(String gunChars) {
		String weapon = "";

		/** 
		 * Thanks to Kokeria for making this easy :D
		 * https://hypixel.net/threads/copy-and-paste-all-cops-and-crims-symbols.254552/
		 */
		if (gunChars.substring(0, 1).equals(String.valueOf('\u928f'))) {
			weapon = "pistol";
		}
		else if (gunChars.substring(0, 1).equals(String.valueOf('\u928e'))) {
			weapon = "magnum";
		}
		else if (gunChars.substring(0, 1).equals(String.valueOf('\u928d'))) {
			weapon = "smg";
		}
		else if (gunChars.substring(0, 1).equals(String.valueOf('\u9283'))) {
			weapon = "grenade";
		}
		else if (gunChars.substring(0, 1).equals(String.valueOf('\u9279'))) {
			weapon = "firebomb";
		}
		else if (gunChars.substring(0, 1).equals(String.valueOf('\u928c'))) {
			weapon = "knife";
		}
		else if (gunChars.substring(0, 2).equals(String.valueOf('\u928a') + String.valueOf('\u928b'))) {
			weapon = "shotgun";
		}
		else if (gunChars.substring(0, 2).equals(String.valueOf('\u9288') + String.valueOf('\u9289'))) {
			weapon = "carbine";
		}
		else if (gunChars.substring(0, 2).equals(String.valueOf('\u9286') + String.valueOf('\u9287'))) {
			weapon = "rifle";
		}
		else if (gunChars.substring(0, 2).equals(String.valueOf('\u9284') + String.valueOf('\u9285'))) {
			weapon = "sniper";
		}
		
		// Category name is game type, field name is the weapon name
		config.get(currentGame.getGameType(), weapon, 0).set(config.get(currentGame.getGameType(), weapon, 0).getInt() + 1);
		
		if (gunChars.substring(gunChars.length() - 1).equals(String.valueOf('\u9270'))) {
			config.get(currentGame.getGameType(), weapon + "hs", 0).set(config.get(currentGame.getGameType(), weapon + "hs", 0).getInt() + 1);
		}
		config.save();
	}
	
	private static void getTDMTeam() {
		if (Minecraft.getMinecraft().theWorld.getScoreboard() != null) {
			List<String> sbValues = new ArrayList<String>();
			//sbValues = getScoreboardData();
			sbValues = ScoreboardUtil.getSidebarScores(Minecraft.getMinecraft().theWorld.getScoreboard());

			for (String score : sbValues) {
				System.out.println(score);

				if (score.contains(String.valueOf('\u9291') + " Crims"))
					currentGame.team = Team.CRIMS;
				else if (score.contains(String.valueOf('\u9290') + " Cops"))
					currentGame.team = Team.COPS;
			}
		}
		else {
			System.out.println("no scoreboard");
			if (Minecraft.getMinecraft().thePlayer != null) {
				new ScheduledCode(() -> getTDMTeam(), 5);
			}
		}
	}
}
