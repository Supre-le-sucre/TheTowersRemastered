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
import me.PauMAVA.TTR.config.TTRConfigManager;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.match.AutoStarter;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.ui.MatchSelector;
import me.PauMAVA.TTR.ui.TeamSelector;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

public class EventListener implements Listener {


    private final TTRCore plugin;
    
    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }


    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        if (plugin.enabled()) {
            //plugin.getPacketInterceptor().addPlayer(event.getPlayer());
            //TTRMatch match = plugin.getMatchFromWorld(event.getPlayer().getWorld());
            //if(match ==  null) {
                //Player is not in a match when reconnected, should be taken in Main Lobby
            Location location = plugin.getConfigManager().getMainLobbyLocation();
            Location copy = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
            copy.add(location.getX() > 0 ? 0.5 : -0.5, 0.0, location.getZ() > 0 ? 0.5 : -0.5);
            event.getPlayer().sendMessage(ChatColor.RED + "Avertissement");
            event.getPlayer().sendMessage(ChatColor.RED + "Le Tower viens tout juste d'être créé ce qui signifie que certains bugs apparaîtront en jeu");
            event.getPlayer().sendMessage(ChatColor.GOLD + "Merci de le faire savoir à l'équipe de développement !");
            event.getPlayer().teleport(copy);
            event.getPlayer().getInventory().clear();
            event.getPlayer().getInventory().setItem(0, new ItemBuilder(Material.IRON_SWORD).setName(ChatColor.GOLD + "Sélectionnez un match").toItemStack());
            /*} else {
                JoinMatchEvent joinMatchEvent = new JoinMatchEvent(event.getPlayer(), match);
                Bukkit.getPluginManager().callEvent(joinMatchEvent);


            } */

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
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) != null) {
            TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()).getTeamHandler().getPlayerTeam(event.getPlayer()).removePlayer(event.getPlayer());
            TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()).removePlayer(event.getPlayer());
            plugin.getAutoStarter().removePlayerFromQueue(event.getPlayer(), TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()));
            plugin.getPacketInterceptor().removePlayer(event.getPlayer());
            if(TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()).getPlayers().isEmpty()) {
                TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()).endMatch(null);
            }
        }
    }

    /*@EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) != null && !plugin.getMatchFromWorld(event.getPlayer().getWorld()).isOnCourse()) {
            event.setCancelled(true);
        }
    } */

    @EventHandler
    public void playerClickEvent(PlayerInteractEvent event) {
        if(plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) == null && event.getItem() != null && event.getItem().getType() == Material.IRON_SWORD)
            new MatchSelector(event.getPlayer()).openSelector();
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) != null && !(plugin.getMatchFromWorld(event.getPlayer().getWorld()).getStatus() == MatchStatus.INGAME)) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            event.setCancelled(true);
            if (event.getItem() != null && event.getItem().getType().toString().split("_")[1].equalsIgnoreCase("BANNER")) {
                new TeamSelector(event.getPlayer()).openSelector();
            }
            else if (event.getItem() != null && event.getItem().getType() == Material.RED_BED) {
                plugin.getAutoStarter().removePlayerFromQueue(event.getPlayer(), TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()));
                plugin.getMatchFromWorld(event.getPlayer().getWorld()).removePlayer(event.getPlayer());
                TTRChatManager.broadcastMatchMessage(TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()), TTRPrefix.TTR_GAME + "" + ChatColor.RED + "- " + ChatColor.GRAY + event.getPlayer().getName() + PluginString.ON_PLAYER_LEAVE_OUTPUT);
                if(plugin.getMatchFromWorld(event.getPlayer().getWorld()).getTeamHandler().getPlayerTeam(event.getPlayer()) != null) plugin.getMatchFromWorld(event.getPlayer().getWorld()).getTeamHandler().getPlayerTeam(event.getPlayer()).removePlayer(event.getPlayer());
                event.getPlayer().teleport(TTRCore.getInstance().getConfigManager().getMainLobbyLocation());
                event.getPlayer().getInventory().clear();
                event.getPlayer().setLevel(0);
                event.getPlayer().setExp(0.0f);
                event.getPlayer().getInventory().setItem(0, new ItemBuilder(Material.IRON_SWORD).setName(ChatColor.GOLD + "Sélectionnez un match").toItemStack());


            }
        }

    }

    /*@EventHandler
    public void placeBlockEvent(BlockPlaceEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) != null && !(plugin.getMatchFromWorld(event.getPlayer().getWorld()).getStatus() == MatchStatus.INGAME)) {
            event.getPlayer().sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + PluginString.ON_PLACE_BLOCK_ERROR);
            event.setCancelled(true);
        }
    } */

    /*@EventHandler
    public void breakBlockEvent(BlockBreakEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) != null && !plugin.getMatchFromWorld(event.getPlayer().getWorld()).isOnCourse()) {
            event.getPlayer().sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + PluginString.ON_BREAK_BLOCK_ERROR);
            event.setCancelled(true);
        }
    } */

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.enabled() && TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()) !=null && plugin.getMatchFromWorld(event.getEntity().getWorld()).isOnCourse()) {
            Player whoDied =  event.getEntity();
            Player whoKilled = event.getEntity().getKiller();
            TTRMatch match = plugin.getMatchFromWorld(event.getEntity().getWorld());
            plugin.getMatchFromWorld(event.getEntity().getWorld()).playerDeath(whoDied, whoKilled);
            event.setDeathMessage(null);
            if(whoKilled != null) whoKilled.playSound(whoKilled.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10 , 2);
            for(Player p : match.getPlayers()) {
                if(whoKilled == null) {
                    p.sendMessage(ChatColor.DARK_RED + "☠ " +plugin.getConfigManager().getTeamColor(match.getTeamHandler().getPlayerTeam(whoDied).getIdentifier()) + whoDied.getName());
                } else {
                    p.sendMessage( plugin.getConfigManager().getTeamColor(match.getTeamHandler().getPlayerTeam(whoKilled).getIdentifier()) + whoKilled.getName() + ChatColor.DARK_RED + " ► " + plugin.getConfigManager().getTeamColor(match.getTeamHandler().getPlayerTeam(whoDied).getIdentifier()) + whoDied.getName());
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(TTRCore.getInstance().enabled() && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()) != null && TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()).isOnCourse())
            if(event.getPlayer().getLocation().getY() <= (int) TTRCore.getInstance().getConfigManager().getYInstaKill(TTRCore.getInstance().getMatchFromWorld(event.getPlayer().getWorld()).getId())) {
                event.getPlayer().setHealth(0);
            }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()) !=null &&  !(Objects.requireNonNull(plugin.getMatchFromWorld(event.getEntity().getWorld())).getStatus() == MatchStatus.INGAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player && TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()) !=null && TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()).isOnCourse() && TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()).getTeamHandler().getPlayerTeam((Player)event.getEntity()).equals(TTRCore.getInstance().getMatchFromWorld(event.getEntity().getWorld()).getTeamHandler().getPlayerTeam((Player)event.getDamager()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMatchJoined(JoinMatchEvent event) {
        if(event.getMatch().getStatus() == MatchStatus.PREGAME || event.getMatch().getStatus() == MatchStatus.ONCOUNTDOWN && !event.isCancelled()) {
            Player player = event.getPlayer();
            TTRMatch match = event.getMatch();
            player.teleport(TTRCore.getInstance().getConfigManager().getPreGameLobbyLocation(match.getId()));
            Inventory playerInventory = player.getInventory();
            playerInventory.clear();
            playerInventory.setItem(0, new ItemBuilder(Material.WHITE_BANNER).setName(ChatColor.GOLD + "Choisissez votre équipe !").toItemStack());
            playerInventory.setItem(8, new ItemBuilder(Material.RED_BED).setName(ChatColor.RED + "Retourner au Lobby").toItemStack());
            plugin.getAutoStarter().addPlayerToQueue(player, match);
            for(Player p: event.getMatch().getPlayers()) {
               p.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GREEN + "+ " + ChatColor.GRAY + event.getPlayer().getName() + PluginString.ON_PLAYER_JOIN_OUTPUT);
            }
        } else {
            event.setCancelled(true);
            //TODO make this better wsh
            event.getPlayer().sendMessage(ChatColor.RED + "Impossible de rejoindre le match " + event.getMatch().getId());
        }
    }


}
