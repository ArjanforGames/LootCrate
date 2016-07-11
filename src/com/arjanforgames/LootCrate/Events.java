package com.arjanforgames.LootCrate;

import java.io.IOException;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent event) throws IOException{
		Player player = event.getPlayer();
		if(Main.searchUpdate() != Main.pluginVersion){
			player.sendMessage("LootCrate has a new update available! - Update at spigotmc.org");
			player.sendMessage("Local version: " + Main.pluginVersion + " | New Version: " + Main.searchUpdate());
		}
	}
	
	@EventHandler
	public void onSignInteraction(SignChangeEvent event){
		Player player = event.getPlayer();
		Sign sign = (Sign) event.getBlock().getState();
		if(event.getLine(0).equalsIgnoreCase("[LootCrate]") && event.getLine(1).equalsIgnoreCase("Uncrate") && isInteger(event.getLine(2))){
			int crateId = Integer.parseInt(event.getLine(2));
			if(player.hasPermission("lootcrate.createsign")){
				event.setLine(0, ChatColor.DARK_AQUA + "[LootCrate]");
				
				if(Main.plugin.getConfig().getString("CrateInfo.Crate" + crateId + ".Name") != null){
					event.setLine(1, "Click to uncrate:");
					event.setLine(2, Main.plugin.getConfig().getString("CrateInfo.Crate" + crateId + ".Name").replaceAll("(%([a-f0-9]))", "\u00A7$2"));
				}else{
					event.setLine(1, ChatColor.RED + "Woops!");
					event.setLine(2, ChatColor.RED + "Unknown Crate");
				}
				event.setLine(3, "///////////////");
				sign.update();
			}else{
				player.sendMessage("[LootCrate] You are not allowed to create these signs!");
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSignClicked(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
				Sign sign = (Sign) block.getState();
				String[] lines = sign.getLines();
				if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[LootCrate]")){
					if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("Click to uncrate:")){
						if(player.hasPermission("lootcrate.uncratesign")){
							if(Main.plugin.getConfig().getBoolean("UseVault")){
								if(Main.econ != null){
									if(Main.econ.getBalance(player.getName()) >= Main.plugin.getConfig().getDouble("UncratePrice")){
										Main.econ.withdrawPlayer(player.getName(), Main.plugin.getConfig().getDouble("UncratePrice"));
										int crateId = 0;
										int crates = Main.plugin.getConfig().getInt("Crates");
										String[] crateNames = new String[Main.plugin.getConfig().getInt("Crates")];
										for(int i = 0; i < crates; i++){
											crateNames[i] = Main.plugin.getConfig().getString("CrateInfo.Crate" + (i + 1) + ".Name");
										}
										for(int i = 0; i < crates; i++){
											if(ChatColor.stripColor(lines[2]).equals(crateNames[i].replace("%" + crateNames[i].charAt(1), ""))){
												crateId = i + 1;
												break;
											}			
										}
										LootCrate.roll(player, crateId);
									}
								}else{
									player.sendMessage("[LootCrate] I failed hooking into Vault.");
									player.sendMessage("[LootCrate] Unloading LootCrate...");
									Bukkit.getPluginManager().disablePlugin(Main.plugin);
								}
							}else{
								int crateId = 0;
								int crates = Main.plugin.getConfig().getInt("Crates");
								String[] crateNames = new String[Main.plugin.getConfig().getInt("Crates")];
								for(int i = 0; i < crates; i++){
									crateNames[i] = Main.plugin.getConfig().getString("CrateInfo.Crate" + (i + 1) + ".Name");
								}
								for(int i = 0; i < crates; i++){
									if(ChatColor.stripColor(lines[2]).equals(crateNames[i].replace("%" + crateNames[i].charAt(1), ""))){
										crateId = i + 1;
										break;
									}			
								}
								LootCrate.roll(player, crateId);
							}
						}else{
							player.sendMessage("[LootCrate] You are not allowed to use this!");
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void useCrate(PlayerInteractEvent event){
		Player player = event.getPlayer();
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(player.getItemInHand().getType().equals(Material.CHEST)){
				int crateId = 0;
				int crates = Main.plugin.getConfig().getInt("Crates");
				String[] crateNames = new String[Main.plugin.getConfig().getInt("Crates")];
				for(int i = 0; i < crates; i++){
					crateNames[i] = Main.plugin.getConfig().getString("CrateInfo.Crate" + (i + 1) + ".Name");
				}
				for(int i = 0; i < crates; i++){
					if(event.getPlayer().getItemInHand().getItemMeta().hasDisplayName()){
						if(ChatColor.stripColor(player.getItemInHand().getItemMeta().getDisplayName()).equals(crateNames[i].replace("%" + crateNames[i].charAt(1), ""))){
							crateId = i + 1;
							break;
						}
					}		
				}
				
				if(crateId != 0){
					LootCrate.roll(player, crateId);
				}
			}
		}
	}
	
	@EventHandler
	public void cratePlacingCancel(BlockPlaceEvent event){
		if(event.getItemInHand().getType().equals(Material.CHEST)){
			boolean containsName = false;
			int crates = Main.plugin.getConfig().getInt("Crates");
			String[] crateNames = new String[Main.plugin.getConfig().getInt("Crates")];
			for(int i = 0; i < crates; i++){
				crateNames[i] = Main.plugin.getConfig().getString("CrateInfo.Crate" + (i + 1) + ".Name");
			}
			for(int i = 0; i < crates; i++){
				if(event.getPlayer().getItemInHand().getItemMeta().hasDisplayName()){
					if(ChatColor.stripColor(event.getPlayer().getItemInHand().getItemMeta().getDisplayName()).equals(crateNames[i].replace("%" + crateNames[i].charAt(1), ""))){
						containsName = true;
						break;
					}
				}			
			}
			
			if(containsName){
				consumeItem(event.getPlayer(), 1, event.getPlayer().getItemInHand().getType());	
				event.setCancelled(true);
			}
		}
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}
	
	public boolean consumeItem(Player player, int count, Material mat) {
	    Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(mat);
	    int found = 0;
	    for (ItemStack stack : ammo.values())
	        found += stack.getAmount();
	    if (count > found)
	        return false;
	    for (Integer index : ammo.keySet()) {
	        ItemStack stack = ammo.get(index);
	        int removed = Math.min(count, stack.getAmount());
	        count -= removed;
	        if (stack.getAmount() == removed)
	            player.getInventory().setItem(index, null);
	        else
	            stack.setAmount(stack.getAmount() - removed);
	        if (count <= 0)
	            break;
	    }
	    player.updateInventory();
	    return true;
	}

}
