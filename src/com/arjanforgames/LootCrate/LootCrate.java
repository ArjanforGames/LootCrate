package com.arjanforgames.LootCrate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LootCrate implements Listener {
	
	static int rollerOne;
	static int rollerTwo;
	static int rollerThree;
	static int rollerFinal;
	private static HashMap<Player, Boolean> canTakeLoot = new HashMap<Player, Boolean>();
	
	public static void checkConfig(){
		int amountTiers = Main.plugin.getConfig().getInt("Tiers");
		int amountCrates = Main.plugin.getConfig().getInt("Crates");
		
		System.out.println("[LootCrate] Total of " + amountTiers + " tiers have been loaded!");
		System.out.println("[LootCrate] Total of " + amountCrates + " crates have been loaded!");
	}
	
	public static void roll(final Player player, final int crateNumber){
		canTakeLoot.remove(player);
		final Inventory randomItemInv = Bukkit.createInventory(null, 9, Main.plugin.getConfig().getString("CrateInfo.Crate" + crateNumber + ".Name").replaceAll("(%([a-f0-9]))", "\u00A7$2"));
		
		createRandomCrate(player, randomItemInv, crateNumber);
				
		rollerOne = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
			int firstRoll = 0;

			public void run(){
				ItemStack zeroStack;
				zeroStack = randomItemInv.getItem(0);
				for(int i = 0; i < (randomItemInv.getSize() - 1); i++){
					randomItemInv.setItem(i, randomItemInv.getItem(i + 1));
				}
				randomItemInv.setItem(8, zeroStack);
				Sound spinningSound = Sound.valueOf(Main.plugin.getConfig().getString("SpinningSound"));
				player.playSound(player.getLocation(), spinningSound, 1, 1);
				firstRoll++;
				
				if(firstRoll == 15){
					Bukkit.getScheduler().cancelTask(rollerOne);
					rollerTwo = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
						int secondRoll = 0;
						
						public void run(){
							ItemStack zeroStack;
							zeroStack = randomItemInv.getItem(0);
							for(int i = 0; i < (randomItemInv.getSize() - 1); i++){
								randomItemInv.setItem(i, randomItemInv.getItem(i + 1));
							}
							randomItemInv.setItem(8, zeroStack);
							Sound spinningSound = Sound.valueOf(Main.plugin.getConfig().getString("SpinningSound"));
							player.playSound(player.getLocation(), spinningSound, 1, 1);
							secondRoll++;
							
							if(secondRoll == 10){
								Bukkit.getScheduler().cancelTask(rollerTwo);
								rollerThree = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
									int thirdRoll = 0;
									
									public void run(){
										ItemStack zeroStack;
										zeroStack = randomItemInv.getItem(0);
										for(int i = 0; i < (randomItemInv.getSize() - 1); i++){
											randomItemInv.setItem(i, randomItemInv.getItem(i + 1));
										}
										randomItemInv.setItem(8, zeroStack);
										Sound spinningSound = Sound.valueOf(Main.plugin.getConfig().getString("SpinningSound"));
										player.playSound(player.getLocation(), spinningSound, 1, 1);
										thirdRoll++;
										
										if(thirdRoll == 5){
											Bukkit.getScheduler().cancelTask(rollerThree);
											
											rollerFinal = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
												int stage = 0;
												@SuppressWarnings("deprecation")
												ItemStack closingItem = new ItemStack(Material.getMaterial(Main.plugin.getConfig().getInt("FinalStage")));
												Sound sound = Sound.valueOf(Main.plugin.getConfig().getString("FinalStageSound"));
												Sound soundSucces = Sound.valueOf(Main.plugin.getConfig().getString("FinalStageSucces"));

												public void run(){
													if(stage == 0){
														randomItemInv.setItem(0, closingItem);
														randomItemInv.setItem(8, closingItem);
														player.playSound(player.getLocation(), sound, 1, 1);
													}
													if(stage == 1){
														randomItemInv.setItem(1, closingItem);
														randomItemInv.setItem(7, closingItem);
														player.playSound(player.getLocation(), sound, 1, 1);
													}
													if(stage == 2){
														randomItemInv.setItem(2, closingItem);
														randomItemInv.setItem(6, closingItem);
														player.playSound(player.getLocation(), sound, 1, 1);
													}
													if(stage == 3){
														randomItemInv.setItem(3, closingItem);
														randomItemInv.setItem(5, closingItem);
														player.playSound(player.getLocation(), sound, 1, 1);
													}
													if(stage == 4){
														Bukkit.getScheduler().cancelTask(rollerFinal);
														player.playSound(player.getLocation(), soundSucces, 1, 1);
														createLootItem(player, randomItemInv, crateNumber, getPaneTier(randomItemInv));
														canTakeLoot.put(player, true);
													}
													stage++;
												}
											}, 20L, Main.plugin.getConfig().getLong("SpeedFinalStage"));
										}
									}
								}, 0L, Main.plugin.getConfig().getLong("SpeedThirdStage"));
							}
						}
					}, 0L, Main.plugin.getConfig().getLong("SpeedSecondStage"));
				}
			}
		}, 0L, Main.plugin.getConfig().getLong("SpeedFirstStage"));
	}
	
	@EventHandler
	public void takeLoot(InventoryClickEvent event){
		Player player = (Player) event.getWhoClicked();
		boolean containsName = false;
		int crates = Main.plugin.getConfig().getInt("Crates");
		String[] crateNames = new String[Main.plugin.getConfig().getInt("Crates")];
		for(int i = 0; i < crates; i++){
			crateNames[i] = Main.plugin.getConfig().getString("CrateInfo.Crate" + (i + 1) + ".Name");
		}
		for(int i = 0; i < crates; i++){
			if(ChatColor.stripColor(event.getInventory().getName()).equals(crateNames[i].replace("%" + crateNames[i].charAt(1), ""))){
				containsName = true;
				break;
			}			
		}
		
		if(containsName){
			if(!canTakeLoot.containsKey(player)){
				event.setCancelled(true);
			}else{
				if(event.getSlot() == 4){
					player.closeInventory();
					canTakeLoot.remove(player);
				}else{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void closingInv(InventoryCloseEvent event){
		Player player = (Player) event.getPlayer();
		if(!canTakeLoot.containsKey(player)){
			Bukkit.getScheduler().cancelTask(rollerOne);
			Bukkit.getScheduler().cancelTask(rollerTwo);
			Bukkit.getScheduler().cancelTask(rollerThree);
			Bukkit.getScheduler().cancelTask(rollerFinal);
		}else if(canTakeLoot.containsKey(player)){
			player.getInventory().addItem(event.getInventory().getItem(4));
			player.updateInventory();
			canTakeLoot.remove(player);
		}
	}
	
	public static void createChestCrate(Player player, int crateId){
		ItemStack chest = new ItemStack(Material.CHEST);
		ItemMeta meta = chest.getItemMeta();
		meta.setDisplayName(Main.plugin.getConfig().getString("CrateInfo.Crate" + crateId + ".Name").replaceAll("(%([a-f0-9]))", "\u00A7$2"));
		meta.setLore(Arrays.asList(ChatColor.WHITE + "This crate can be opened to reveal your reward.", ChatColor.WHITE + "One time use only!", ChatColor.YELLOW + "Right-click me!"));
		chest.setItemMeta(meta);
		player.getInventory().addItem(chest);
		player.sendMessage("[LootCrate] You received a crate! (" + Main.plugin.getConfig().getString("CrateInfo.Crate" + crateId + ".Name").replaceAll("(%([a-f0-9]))", "\u00A7$2") + ChatColor.WHITE + ")");
	}
	
	private static void createRandomCrate(Player player, Inventory inv, int crateNumber){
		List<Integer> cratenrTiers = Main.plugin.getConfig().getIntegerList("CrateInfo.Crate" + crateNumber + ".Tiers");
		int[] crateTiers = new int[cratenrTiers.size()];
		for(int i = 0; i < cratenrTiers.size(); i++){
			crateTiers[i] = cratenrTiers.get(i);
		}
		
		for(int i = 0; i < inv.getSize(); i++){
			int arrayID = generateCrate(crateNumber);
			
			ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) getTierColor(Main.plugin.getConfig().getString("TierColor." + crateTiers[arrayID])));
			setPaneColor(pane);
			inv.setItem(i, pane);	
		}
		
		player.openInventory(inv);
	}
	
	private static int generateCrate(int crateNumber){
        int dropChanceSum = 0;
        List<Integer> cratenrTierChances = Main.plugin.getConfig().getIntegerList("CrateInfo.Crate" + crateNumber + ".Tiers");
        int[] crateTierChances = new int[cratenrTierChances.size()];
        for(int i = 0; i < cratenrTierChances.size(); i++){
			crateTierChances[i] = cratenrTierChances.get(i);
		}
        
        for(int i = 0; i < crateTierChances.length; i++)
            dropChanceSum += crateTierChances[i];
       
        int randomNumber = new Random().nextInt(dropChanceSum);

        int dropChanceTillNow = 0;
        for(int i = 0; i < crateTierChances.length; i++)
        {
            dropChanceTillNow += crateTierChances[i];
            if(dropChanceTillNow >= randomNumber){
            	return i;
            }
        }
        return -1;
    }
	
	private static int generateLootItem(int crateNumber, int tier){
		int dropChanceSum = 0;
		List<Integer> cratenrItemChances = Main.plugin.getConfig().getIntegerList("CrateContents.Crate" + crateNumber + ".Tier" + tier + ".Chance");
		int[] crateItemChances = new int[cratenrItemChances.size()];
		for(int i = 0; i < cratenrItemChances.size(); i++){
			crateItemChances[i] = cratenrItemChances.get(i);
		}
		
		for(int i = 0; i < crateItemChances.length; i++)
            dropChanceSum += crateItemChances[i];
		
		int randomNumber = new Random().nextInt(dropChanceSum);
		
		int dropChanceTillNow = 0;
        for(int i = 0; i < crateItemChances.length; i++)
        {
            dropChanceTillNow += crateItemChances[i];
            if(dropChanceTillNow >= randomNumber){
            	return i;
            }
        }
        return -1;
	}
	
	private static int getTierColor(String color){
		if(color.equals("WHITE")) return 0;
		if(color.equals("ORANGE")) return 1;
		if(color.equals("MAGENTA")) return 2;
		if(color.equals("LIGHT_BLUE")) return 3;
		if(color.equals("YELLOW")) return 4;
		if(color.equals("LIME")) return 5;
		if(color.equals("PINK")) return 6;
		if(color.equals("GRAY")) return 7;
		if(color.equals("LIGHT_GRAY")) return 8;
		if(color.equals("CYAN")) return 9;
		if(color.equals("PURPLE")) return 10;
		if(color.equals("BLUE")) return 11;
		if(color.equals("BROWN")) return 12;
		if(color.equals("GREEN")) return 13;
		if(color.equals("RED")) return 14;
		if(color.equals("BLACK")) return 15;
		else return 0;
	}
	
	private static String getColorTier(int colorValue){
		if(colorValue == 0) return "WHITE";
		if(colorValue == 1) return "ORANGE";
		if(colorValue == 2) return "MAGENTA";
		if(colorValue == 3) return "LIGHT_BLUE";
		if(colorValue == 4) return "YELLOW";
		if(colorValue == 5) return "LIME";
		if(colorValue == 6) return "PINK";
		if(colorValue == 7) return "GRAY";
		if(colorValue == 8) return "LIGHT_GRAY";
		if(colorValue == 9) return "CYAN";
		if(colorValue == 10) return "PURPLE";
		if(colorValue == 11) return "BLUE";
		if(colorValue == 12) return "BROWN";
		if(colorValue == 13) return "GREEN";
		if(colorValue == 14) return "RED";
		if(colorValue == 15) return "BLACK";
		else return "WHITE";
	}
	
	private static int getPaneTier(Inventory inv){
		ItemStack pane = inv.getItem(4);
		int data = pane.getDurability();
		int paneTier = 0;
		int amountTiers = Main.plugin.getConfig().getInt("Tiers") + 1;
		String paneColor = getColorTier(data);
		String[] allColors = new String[amountTiers];
		
		for(int i = 1; i < amountTiers; i++){
			allColors[i] = Main.plugin.getConfig().getString("TierColor." + i);
		}
		
		for(int i = 1; i < allColors.length; i++){
			if(allColors[i].equals(paneColor)){
				paneTier = i;
				break;
			}
		}
		return paneTier;
	}
	
	
	private static ItemStack setPaneColor(ItemStack pane){
		ItemMeta paneMeta = pane.getItemMeta();
		if(pane.getDurability() == 0) { paneMeta.setDisplayName(ChatColor.WHITE + "???"); }
		if(pane.getDurability() == 1) { paneMeta.setDisplayName(ChatColor.GOLD + "???"); }
		if(pane.getDurability() == 2) { paneMeta.setDisplayName(ChatColor.DARK_PURPLE + "???"); }
		if(pane.getDurability() == 3) { paneMeta.setDisplayName(ChatColor.AQUA + "???"); }
		if(pane.getDurability() == 4) { paneMeta.setDisplayName(ChatColor.YELLOW + "???"); }
		if(pane.getDurability() == 5) { paneMeta.setDisplayName(ChatColor.GREEN + "???"); }
		if(pane.getDurability() == 6) { paneMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "???"); }
		if(pane.getDurability() == 7) { paneMeta.setDisplayName(ChatColor.GRAY + "???"); }
		if(pane.getDurability() == 8) { paneMeta.setDisplayName(ChatColor.WHITE + "???"); }
		if(pane.getDurability() == 9) { paneMeta.setDisplayName(ChatColor.DARK_AQUA + "???"); }
		if(pane.getDurability() == 10) { paneMeta.setDisplayName(ChatColor.DARK_PURPLE + "???"); }
		if(pane.getDurability() == 11) { paneMeta.setDisplayName(ChatColor.BLUE + "???"); }
		if(pane.getDurability() == 12) { paneMeta.setDisplayName(ChatColor.DARK_GRAY + "???"); }
		if(pane.getDurability() == 13) { paneMeta.setDisplayName(ChatColor.DARK_GREEN + "???"); }
		if(pane.getDurability() == 14) { paneMeta.setDisplayName(ChatColor.RED + "???"); }
		if(pane.getDurability() == 15) { paneMeta.setDisplayName(ChatColor.BLACK + "???"); }
		pane.setItemMeta(paneMeta);
		return pane;
	}
	
	private static void createLootItem(Player player, Inventory inv, int crateNumber, int tier){
		int arrayID = generateLootItem(crateNumber, tier);
		
		List<Integer> cratenrItems = Main.plugin.getConfig().getIntegerList("CrateContents.Crate" + crateNumber + ".Tier" + tier + ".Items");
		int[] crateItems = new int[cratenrItems.size()];
		for(int i = 0; i < cratenrItems.size(); i++){
			crateItems[i] = cratenrItems.get(i);
		}
		
		inv.setItem(4, idToItemStack(crateNumber, tier, crateItems[arrayID], arrayID));
		player.openInventory(inv);
	}
	
	@SuppressWarnings("deprecation")
	private static ItemStack idToItemStack(int crateNumber, int tier, int id, int arrayID){
		List<Integer> cratenrQuantities = Main.plugin.getConfig().getIntegerList("CrateContents.Crate" + crateNumber + ".Tier" + tier + ".Quantity");
		int[] crateQuantities = new int[cratenrQuantities.size()];
		for(int i = 0; i < cratenrQuantities.size(); i++){
			crateQuantities[i] = cratenrQuantities.get(i);
		}
		
		ItemStack item = new ItemStack(Material.getMaterial(id), crateQuantities[arrayID]);
		return item;
	}
}
