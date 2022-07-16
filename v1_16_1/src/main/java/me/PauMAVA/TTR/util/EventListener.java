/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.chat.TTRChatManager;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.match.AutoStarter;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.ui.TeamSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class EventListener implements Listener {


    private final TTRCore plugin;
    
    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }


    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    //TODO Modify this in order to have an intermediate lobby to choose match from

        if (plugin.enabled()) {
            //plugin.getPacketInterceptor().addPlayer(event.getPlayer());
            TTRMatch match = plugin.getMatchFromWorld(event.getPlayer().getWorld());
            if(match ==  null) {
                //Player is not in a match when reconnected, should be taken in Main Lobby
                Location location = plugin.getConfigManager().getMainLobbyLocation();
                Location copy = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                copy.add(location.getX() > 0 ? 0.5 : -0.5, 0.0, location.getZ() > 0 ? 0.5 : -0.5);
                event.getPlayer().teleport(copy);
            } else {
                JoinMatchEvent joinMatchEvent = new JoinMatchEvent(event.getPlayer(), match);
                Bukkit.getPluginManager().callEvent(joinMatchEvent);


            }
            event.setJoinMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GREEN + "+ " + ChatColor.GRAY + event.getPlayer().getName() + PluginString.ON_PLAYER_JOIN_OUTPUT);

        }
    }
    @EventHandler
    public void PlayerChatEvent(AsyncPlayerChatEvent event) {
        if (!event.getMessage().startsWith("/")) {
            TTRChatManager.sendMessage(event.getPlayer(), event.getMessage());
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (plugin.enabled()) {
            event.setQuitMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + "- " + ChatColor.GRAY + event.getPlayer().getName() + PluginString.ON_PLAYER_LEAVE_OUTPUT);
            plugin.getAutoStarter().removePlayerFromQueue(event.getPlayer(), TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()));
            plugin.getPacketInterceptor().removePlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) !=null && !plugin.getMatchFromWorld(event.getPlayer().getWorld()).isOnCourse()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerClickEvent(PlayerInteractEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) !=null && !(plugin.getMatchFromWorld(event.getPlayer().getWorld()).getStatus() == MatchStatus.INGAME)) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            event.setCancelled(true);
            if (event.getItem() != null && event.getItem().getType() == Material.BLACK_BANNER) {
                new TeamSelector(event.getPlayer()).openSelector();
            }
        }
    }

    @EventHandler
    public void placeBlockEvent(BlockPlaceEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) !=null && !(plugin.getMatchFromWorld(event.getPlayer().getWorld()).getStatus() == MatchStatus.INGAME)) {
            event.getPlayer().sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + PluginString.ON_PLACE_BLOCK_ERROR);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void breakBlockEvent(BlockBreakEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) !=null && !plugin.getMatchFromWorld(event.getPlayer().getWorld()).isOnCourse()) {
            event.getPlayer().sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + PluginString.ON_BREAK_BLOCK_ERROR);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()) !=null && Objects.requireNonNull(plugin.getMatchFromWorld(event.getEntity().getWorld())).isOnCourse()) {
            plugin.getMatchFromWorld(event.getEntity().getWorld()).playerDeath(event.getEntity(), event.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()) !=null &&  !(Objects.requireNonNull(plugin.getMatchFromWorld(event.getEntity().getWorld())).getStatus() == MatchStatus.INGAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMatchJoined(JoinMatchEvent event) {
        if(event.getMatch().getStatus() == MatchStatus.PREGAME) {
            Inventory playerInventory = event.getPlayer().getInventory();
            playerInventory.clear();
            playerInventory.setItem(0, new ItemStack(Material.BLACK_BANNER));
            plugin.getAutoStarter().addPlayerToQueue(event.getPlayer(), event.getMatch());
        }
    }


}
