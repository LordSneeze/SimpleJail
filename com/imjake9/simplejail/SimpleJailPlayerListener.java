package com.imjake9.simplejail;

import com.imjake9.simplejail.SimpleJail.JailMessage;
import com.imjake9.simplejail.SimpleJail.JailStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SimpleJailPlayerListener implements Listener {
    
    private final SimpleJail plugin;
    
    public SimpleJailPlayerListener(SimpleJail plugin) {
        
        this.plugin = plugin;
        
    }
    
    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        
        Player player = event.getPlayer();
        
        if(!plugin.playerIsJailed(player)) return;
        
        event.setRespawnLocation(plugin.getJailLocation());
        
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        
        final Player player = event.getPlayer();
        JailStatus status = plugin.getPlayerStatus(player);
        
        if(!plugin.playerIsJailed(player)) return;

        if (plugin.playerIsTempJailed(player) && status != JailStatus.PENDING) {

            double tempTime = plugin.getTempJailTime(player);
            long currentTime = System.currentTimeMillis();

            if (tempTime <= currentTime) {
                try {
                    plugin.unjailPlayer(player.getName());
                } catch (JailException ex) {
                    // Should never happen
                    ex.printStackTrace();
                    return;
                }
                JailMessage.UNTEMPJAILED.print(player.getName());
                return;
            }
            
        }
        
        // If player is still jailed, check status:
        if (status == JailStatus.JAILED) {
            
            JailMessage.PLAYER_IS_JAILED.send(player);
            if(plugin.playerIsTempJailed(player)) {
                int minutes = (int) ((plugin.getTempJailTime(player) - System.currentTimeMillis()) / 60000);
                JailMessage.JAIL_TIME.send(player, plugin.prettifyMinutes(minutes));
            }
            
        } else if (status == JailStatus.PENDING) {
            
            player.teleport(plugin.getJailLocation());
            plugin.setPlayerStatus(player, JailStatus.JAILED);
            
            // Send message
            if (plugin.playerIsTempJailed(player)) {
                int minutes = (int) ((plugin.getTempJailTime(player) - System.currentTimeMillis()) / 60000);
                JailMessage.TEMPJAILED.send(player, plugin.prettifyMinutes(minutes));
            } else {
                JailMessage.JAILED.send(player);
            }
            
        } else if (status == JailStatus.FREED) {
            try {
                plugin.unjailPlayer(player.getName());
            } catch (JailException ex) {
                // Should never happen
                ex.printStackTrace();
            }
        }
        
    }
    
}
