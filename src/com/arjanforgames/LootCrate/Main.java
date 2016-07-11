package com.arjanforgames.LootCrate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	public static Main plugin;
	public static Economy econ = null;
	public static double pluginVersion = 1.2;
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	
	public void onEnable(){
		plugin = this;
		
		File configFile = new File(getDataFolder(), "config.yml");
		if(!configFile.exists()){
			System.out.println("////////////////////");
			System.out.println("[LootCrate] First boot of LootCrate!");
			System.out.println("[LootCrate] Please go to LootCrate/config.yml to start setting up your crates.");
			System.out.println("[LootCrate] Visit the Spigot page if you need help setting it up.");
			System.out.println("////////////////////");
		}
		
		try {
			if(searchUpdate() != pluginVersion){
				System.out.println("[LootCrate] " + ANSI_YELLOW + "NOTICE: A new version of Lootcrate is out!" + ANSI_RESET);
				System.out.println("[LootCrate] " + ANSI_YELLOW + "NOTICE: Local version: " + pluginVersion + " | New Version: " + searchUpdate() + ANSI_RESET);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(getConfig().getBoolean("ResetConfig") == true){
			configFile.delete();
			saveDefaultConfig();
			reloadConfig();
			System.out.println("[LootCrate] ResetConfig was set to true, resetting config...");
		}else{
			saveDefaultConfig();
		}
		
		if(getConfig().getBoolean("UseVault")){
	        if(setupEconomy()){
	        	System.out.println("[LootCrate] NOTICE: LootCrate succesfully hooked into Vault!");
	        }else{
	        	System.out.println("[LootCrate] NOTICE: LootCrate FAILED to hook into Vault!");
	        }
		}

		getServer().getPluginManager().registerEvents(new LootCrate(), this);
		getServer().getPluginManager().registerEvents(new Events(), this);
		getCommand("uncrate").setExecutor(new CommandExecutor());
		getCommand("givecrate").setExecutor(new CommandExecutor());

		LootCrate.checkConfig();
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	public static double searchUpdate() throws IOException{
		String version = null;
		
		URL url = new URL("http://www.minecompany.nl/lootcrate");
		URLConnection conn = url.openConnection();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			version = inputLine;
		}

		br.close();
		
		return Double.parseDouble(version.replaceAll(" ", ""));
	}

}
