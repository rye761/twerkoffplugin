package ryebread761.plugins.twerkplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class TwerkPlugin extends JavaPlugin implements Listener {
	Map<String, Integer> activePlayers;
	Map<String, String> activeRequests;
	
	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		activePlayers = new HashMap<String, Integer>();
		activeRequests = new HashMap<String, String>();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("twerkoff")) {
			if (sender instanceof Player) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("challenge")) {
						if (args.length == 2) {
							List<String> users = new ArrayList<String>();
							for (Player player : getServer().getOnlinePlayers()) {
								users.add(player.getName());
							}
							if (users.contains(args[1]) && !args[1].equalsIgnoreCase(sender.getName())) {
								//send challenge
								sendChallenge(args[1], sender.getName());
							} else {
								List<String> playerNames = new ArrayList<String>();
								Player[] players = getServer().getOnlinePlayers();
								for (Player p : players) {
									if (p.getName().startsWith(args[1]))
										playerNames.add(p.getName());
								}
								
								if (playerNames.size() == 1 && !playerNames.get(0).equalsIgnoreCase(sender.getName())) {
									sendChallenge(playerNames.get(0), sender.getName());
								} else if (playerNames.size() > 1) {
									String allPlayersWithMatch = "";
									for (String s : playerNames) {
										allPlayersWithMatch += s;
									}
									sender.sendMessage(ChatColor.RED + "Found multiple players: " + allPlayersWithMatch);
								} else {
									sender.sendMessage("Sorry, can't find a player named " + args[1]);
								}
							}
						}
					} else if (args[0].equalsIgnoreCase("accept")) {
						if (activeRequests.containsKey(sender.getName())) {
							final Player challenger = getServer().getPlayer(activeRequests.get(sender.getName()));
							if (challenger == null) {
								sender.sendMessage("Challenger quit!");
								activeRequests.remove(sender.getName());
								return true;
							}
							challenger.sendMessage(ChatColor.YELLOW + "Get ready!");
							final Player localSender = (Player) sender;
							final Plugin thisPlug = this;
							sender.sendMessage(ChatColor.YELLOW + "Get ready!");
							activeRequests.remove(sender.getName());
							new BukkitRunnable() {

								@Override
								public void run() {
									activePlayers.put(localSender.getName(), 0);
									activePlayers.put(challenger.getName(), 0);
									localSender.sendMessage(ChatColor.GREEN + "Go!");
									challenger.sendMessage(ChatColor.GREEN + "Go!");
									
									new BukkitRunnable() {

										@Override
										public void run() {
											//determine who won and announce
											int challengerScore = activePlayers.get(challenger.getName());
											int senderScore = activePlayers.get(localSender.getName());
											activePlayers.remove(challenger.getName());
											activePlayers.remove(localSender.getName());
											
											if (challengerScore > senderScore) {
												getServer().broadcastMessage(ChatColor.GREEN +
														challenger.getName() + " (" + challengerScore + ") " +
														" has won in a twerking challenge against " +
														localSender.getName() + " (" + senderScore + ")" + ".");
											} else if (senderScore > challengerScore) {
												getServer().broadcastMessage(ChatColor.GREEN +
														localSender.getName() + " (" + senderScore + ") " +
														" has won in a twerking challenge against " +
														challenger.getName() + " (" + challengerScore + ")" + ".");
											} else {
												getServer().broadcastMessage(ChatColor.GREEN +
														challenger.getName() + " (" + challengerScore + ") " +
														" has tied in a twerking challenge against " +
														localSender.getName() + " (" + senderScore + ")" + ".");
											}
										}
										
									}.runTaskLater(thisPlug, 20 * 60);
								}
								
							}.runTaskLater(this, 20 * 3);
						} else {
							sender.sendMessage(ChatColor.RED + "Sorry, you do not have any pending requests.");
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Usage: /twerkoff [action] [args]");
				}
			} else {
				sender.sendMessage("Sorry, only players can run this command. Why not hop in game?");
			}
			return true;
		}
		return false;
	}
	
	private void sendChallenge (final String toUser, String fromUser) {
		activeRequests.put(toUser, fromUser);
		final Player toUserPlayer = getServer().getPlayer(toUser);
		if (toUserPlayer != null) {
			toUserPlayer.sendMessage(ChatColor.GREEN + fromUser + 
					" has challenged you to a twerking challenge! Type /twerkoff accept");
			final Player fromUserPlayer = getServer().getPlayer(fromUser);
			fromUserPlayer.sendMessage(ChatColor.GREEN + " Challenge sent!");
			
			//handle timeout
			new BukkitRunnable() {

				@Override
				public void run() {
					if (activeRequests.containsKey(toUser)) {
						activeRequests.remove(toUser);
						toUserPlayer.sendMessage(ChatColor.RED + "Challenge request timeout.");
						fromUserPlayer.sendMessage(ChatColor.RED + "Challenge request timeout.");
					}
				}
				
			}.runTaskLater(this, 20 * 20);
		} else {
			activeRequests.remove(toUser);
		}
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e) {
		if (!e.isSneaking()) {
			if (activePlayers.containsKey(e.getPlayer().getName())) {
				activePlayers.put(e.getPlayer().getName(), activePlayers.get(e.getPlayer().getName()) + 1);
			}
		}
	}
}
