package com.fullwall.SkyPirates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;

/**
 * SkyPirates for Bukkit
 * 
 * @author fullwall
 */
public class SkyPirates extends JavaPlugin {

	public VehicleListen vl = new VehicleListen(this);
	public PlayerListen pl = new PlayerListen(this);
	public static HashMap<Player, Integer> playerModes = new HashMap<Player, Integer>();
	public static HashMap<Integer, BoatHandler> boats = new HashMap<Integer, BoatHandler>();
	public static ArrayList<String> helmets = new ArrayList<String>();

	public static SkyPirates plugin;

	private static final String codename = "Caribbean";
	public static Logger log = Logger.getLogger("Minecraft");

	public void onLoad() {

	}

	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.VEHICLE_COLLISION_BLOCK, vl,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vl, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_ENTER, vl, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_EXIT, vl, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vl, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, pl, Priority.Normal, this);
		populateHelmets();

		PluginDescriptionFile pdfFile = this.getDescription();

		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") loaded");

	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]: version ["
				+ pdfFile.getVersion() + "] (" + codename + ") disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("[Skypirates]: Must be ingame to use this command.");
			return true;
		}
		
		Player player = (Player) sender;
		commandName = "/" + commandName;
		String parameters = "";
		
		for (String i : args) {
			parameters += " " + i;
		}
		
		String fullCommand = commandName + parameters;
		String[] split = fullCommand.split(" ");
		
		if (!player.hasPermission("skypirates.player.changemode")) {
			player.sendMessage(ChatColor.RED
					+ "You don't have permission to use that command.");
			return true;
		}
		
		if (split.length >= 2
				&& (split[0].equals("/skypirates") || split[0].equals("/sky") || split[0]
						.contains("/skypi")) && (split[1].length() >= 1)) {
			if (split[1].equals("clear") || split[1].equals("c")) {
				if (player.hasPermission("skypirates.admin.clear")) {
					BoatHandler b;
					
					if (SkyPirates.boats.isEmpty()) {
						player.sendMessage(ChatColor.GRAY
								+ "There are no SkyPirates boats to remove.");
						return true;
					}
					
					for (Entry<Integer, BoatHandler> entry : boats.entrySet()) {
						b = entry.getValue();
						if (b.boat.isEmpty()) {
							b.boat.getWorld().getEntities()
									.remove(entry.getValue());
							boats.remove(entry.getKey());
						}
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "You don't have permission to use that command.");
					return true;
				}
				return true;
			} else if (split[1].equals("help")) {
				if (!player.hasPermission("skypirates.player.help")) {
					player.sendMessage(ChatColor.DARK_RED
							+ "You don't have permission to use that command.");
					return true;
				}
				player.sendMessage(ChatColor.AQUA + "SkyPirates Modes List");
				player.sendMessage(ChatColor.YELLOW + "---------------------");
				player.sendMessage(ChatColor.GREEN + "plane|p - "
						+ ChatColor.AQUA + "turns your boat into a plane.");
				player.sendMessage(ChatColor.GREEN + "submarine|sub|s - "
						+ ChatColor.AQUA
						+ "turns your boat into a submersible.");
				player.sendMessage(ChatColor.GREEN + "hoverboat|h - "
						+ ChatColor.AQUA + "turns your boat into a hoverboat.");
				player.sendMessage(ChatColor.GREEN + "glider|g - "
						+ ChatColor.AQUA + "turns your boat into a glider.");
				player.sendMessage(ChatColor.GREEN + "drill|d - "
						+ ChatColor.AQUA + "turns your boat into a drill.");
				player.sendMessage(ChatColor.GREEN
						+ "anything else - "
						+ ChatColor.AQUA
						+ "turns your boat back into the regular old jumping variety.");
				player.sendMessage(ChatColor.YELLOW + "---------------------");
				return true;
			}
			ArrayList<String> string = new ArrayList<String>();
			string.add("p");
			string.add("s");
			string.add("g");
			string.add("d");
			string.add("h");
			if (!string.contains("" + split[1].charAt(0))) {
				return false;
			}
			if (player.isInsideVehicle() == false) {
				player.sendMessage(ChatColor.RED
						+ "Modes must be changed within a boat.");
				return true;
			}
			if (!(player.getVehicle() instanceof Boat)
					&& !(PlayerListen.checkBoats((Boat) player.getVehicle()))) {
				player.sendMessage(ChatColor.RED
						+ "Modes must be changed within a boat.");
				return true;
			}
			BoatHandler boat = PlayerListen.getBoatHandler((Boat) player
					.getVehicle());
			if (split[1].equals("p") || split[1].equals("plane")) {
				if (player.hasPermission("skypirates.modes.plane")) {
					player.sendMessage(ChatColor.GREEN
							+ "The boat feels suddenly weightless, like a breath of wind would carry you away!");
					playerModes.put(player, 1);
					boat.setMode(1);
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ "As much as you will it to float, the boat remains stubbornly on the ground.");
					return true;
				}
			} else if (split[1].equals("s") || split[1].contains("sub")) {
				if (player.hasPermission("skypirates.modes.submarine")) {
					playerModes.put(player, 2);
					player.sendMessage(ChatColor.BLUE
							+ "You feel the boat getting heavier and heavier as you sink beneath the waves.");
					boat.setMode(2);
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ "As hard as you try, the boat refuses to sink below the water.");
					return true;
				}
			} else if (split[1].contains("hover") || split[1].equals("h")) {
				if (player.hasPermission("skypirates.modes.hoverboat")) {
					player.sendMessage(ChatColor.GOLD
							+ "The boat lifts into the air, hovering over the world below.");
					SkyPirates.playerModes.put(player, 3);
					boat.setMode(3);
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ "The boat retains its usual weight.");
					return true;
				}
			} else if (split[1].contains("glider") || split[1].equals("g")) {
				if (player.hasPermission("skypirates.modes.glider")) {
					player.sendMessage(ChatColor.WHITE
							+ "The boat prepares to float gently downwards.");
					SkyPirates.playerModes.put(player, 4);
					boat.setMode(4);
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ "The boat retains its usual weight.");
					return true;
				}
			} else if (split[1].contains("drill") || split[1].equals("d")) {
				if (player.hasPermission("skypirates.modes.drill")) {
					player.sendMessage(ChatColor.DARK_GRAY
							+ "The boat feels like it has immense force behind it, enough to drill through solid earth.");
					SkyPirates.playerModes.put(player, 5);
					boat.setMode(5);
					return true;
				} else {
					player.sendMessage(ChatColor.RED
							+ "The boat retains its usual strength.");
					return true;
				}
			} else {

				player.sendMessage(ChatColor.GRAY
						+ "The boat is just that, an ordinary vehicle.");
				SkyPirates.playerModes.put(player, 0);
				boat.setMode(0);
				boat.resetValues();
				return true;
			}
		}
		return false;

	}

	public void populateHelmets() {
		helmets.add("298");
		helmets.add("306");
		helmets.add("310");
		helmets.add("314");
	}

}