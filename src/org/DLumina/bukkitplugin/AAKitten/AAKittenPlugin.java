package org.DLumina.bukkitplugin.AAKitten;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AAKittenPlugin extends JavaPlugin implements Listener {
	
	private double search_speed = 20; //tick per search&shoot，主时钟
	private double consume_fish = 0.1; //chance consuming fish per shoot
	private double attack_speed = 2;//主时钟每过多少循环攻击一次
	
	private final Set<String> absence_worlds = new HashSet<String>();//哪些世界不攻击
	private final Set<String> enemies_list = new HashSet<String>();//需要攻击的生物
	
	private BukkitTask checkTask;
	
	@Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }
	
	private void loadConfig() {
        List<String> list1 = getConfig().getStringList("Absence_worlds");
        if (list1 != null) {
            for (String w : list1) {
            	absence_worlds.add(w.toLowerCase());
            }
        }
        List<String> list2 = getConfig().getStringList("Enemies_list");
        if (list2 != null) {
            for (String w : list2) {
            	enemies_list.add(w.toLowerCase());
            }
        }
        search_speed = getConfig().getDouble("Search_speed");
        consume_fish = getConfig().getDouble("Consume_fish");
        attack_speed = getConfig().getDouble("Attack_speed");
        checkTask = Bukkit.getScheduler().runTaskTimer(this, new SearchDestroy(absence_worlds, enemies_list, consume_fish, attack_speed), 0L, (long)search_speed);
    }
}