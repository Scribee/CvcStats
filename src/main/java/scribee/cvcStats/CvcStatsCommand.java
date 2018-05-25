package scribee.cvcStats;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CvcStatsCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "cvcstats";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Usage: /" + getCommandName();
	}
	
	@Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			List<String> scores = new ArrayList<String>();
			scores = ScoreboardUtil.getSidebarScores(Minecraft.getMinecraft().theWorld.getScoreboard());
			for (String score : scores) {
				sender.addChatMessage(new ChatComponentText(score));
			}
		}
		else {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + getCommandUsage(sender)));
		}
	}
}
