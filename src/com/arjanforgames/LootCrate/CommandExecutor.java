package com.arjanforgames.LootCrate;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExecutor implements org.bukkit.command.CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		final Player player = (Player) sender;
		
		/*  ____  _____ ____  _   _  ____  ____ ___ _   _  ____ 
		  |  _ \| ____| __ )| | | |/ ___|/ ___|_ _| \ | |/ ___|
		  | | | |  _| |  _ \| | | | |  _| |  _ | ||  \| | |  _ 
		  | |_| | |___| |_) | |_| | |_| | |_| || || |\  | |_| |
		  |____/|_____|____/ \___/ \____|\____|___|_| \_|\____| */
		
		if(cmd.getLabel().equalsIgnoreCase("uncrate")){
			if(player instanceof Player){
				if(player.hasPermission("lootcrate.uncratecmd")){
					if(args.length == 1){
						int cratenr = Integer.parseInt(args[0]);					
						LootCrate.roll(player, cratenr);
					}else{
						player.sendMessage("[LootCrate] Usage: /uncrate <crateid>");
					}
				}else{
					player.sendMessage("[LootCrate] You can't use this command!");
				}
			}
		}
		if(cmd.getLabel().equalsIgnoreCase("givecrate")){
			if(player instanceof Player){
				if(player.hasPermission("lootcrate.givecrate")){
					if(args.length == 2){
						Player target = Bukkit.getServer().getPlayer(args[0]);
						if(target != null){
							if(Events.isInteger(args[1])){
								int crateId = Integer.parseInt(args[1]);
								if(Main.plugin.getConfig().getString("CrateInfo.Crate" + crateId + ".Name") != null){
									LootCrate.createChestCrate(target, crateId);
								}else{
									player.sendMessage("[LootCrate] Wrong crate ID!");
								}
							}else{
								player.sendMessage("[LootCrate] Use a number!");
							}
						}else{
							player.sendMessage("[LootCrate] That player is not connected!");
						}
					}else{
						player.sendMessage("[LootCrate] Usage: /givecrate <playername> <crateid>");
					}
				}else{
					player.sendMessage("[LootCrate] You can't use this command!");
				}
			}
		}
		return false;
	}

}
