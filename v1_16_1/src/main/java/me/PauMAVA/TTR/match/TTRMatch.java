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

package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.teams.TTRTeamHandler;
import me.PauMAVA.TTR.ui.TTRScoreboard;
import me.PauMAVA.TTR.util.ItemBuilder;
import me.PauMAVA.TTR.util.ReflectionUtils;
import me.PauMAVA.TTR.world.TTRWorldHandler;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TTRMatch {

    private TTRTeamHandler teamHandler;
    private MatchStatus status;
    private LootSpawner lootSpawner;
    private CageChecker checker;
    private ArrayList<Player> players = new ArrayList<>();
    private HashMap<Player, Integer> kills = new HashMap<Player, Integer>();

    private TTRScoreboard scoreboard;
    private int id;

    private World world;

    public TTRMatch(MatchStatus initialStatus, World world, int id) {
        status = initialStatus;
        this.world = world;
        this.scoreboard = new TTRScoreboard(this);
        this.teamHandler = new TTRTeamHandler();
        this.teamHandler.setUpDefaultTeams(this);

        this.id = id;
    }

    public boolean isOnCourse() {
        return this.status == MatchStatus.INGAME;
    }

    public void addPlayer(Player player) {
        if(!this.players.contains(player))
            this.players.add(player);
    }

    public ArrayList<Player> getPlayers() { return this.players; }

    public void startMatch() {
        this.status = MatchStatus.INGAME;
        if(this.lootSpawner != null) this.lootSpawner.startSpawning();
        else {
            this.lootSpawner = new LootSpawner(this);
            this.lootSpawner.startSpawning();
        }
        if(this.checker != null) this.checker.startChecking(this);
        else {
            this.checker = new CageChecker();
            this.checker.setCages(TTRCore.getInstance().getConfigManager().getTeamCages(this), 2);
            this.checker.startChecking(this);
        }
        TTRWorldHandler.getWorldHandler(this).configureTime();
        TTRWorldHandler.getWorldHandler(this).configureWeather();
        TTRWorldHandler.getWorldHandler(this).setWorldDifficulty(Difficulty.PEACEFUL);
        this.getScoreboard().startScoreboardTask();
        balanceTeams();
        for (Player player : this.getPlayers()) {
            TTRTeam playerTeam = this.getTeamHandler().getPlayerTeam(player);
            TTRCore.getInstance().getAutoStarter().removePlayerFromQueue(player, this);
            if (playerTeam == null) {
               playerTeam = pickTeam(player);
            }
            player.teleport(TTRCore.getInstance().getConfigManager().getTeamSpawn(playerTeam.getIdentifier()));
            player.setBedSpawnLocation(TTRCore.getInstance().getConfigManager().getTeamSpawn(playerTeam.getIdentifier()), true);
            player.getInventory().clear();
            player.setExp(0);
            player.setGameMode(GameMode.SURVIVAL);
            double health = TTRCore.getInstance().getConfigManager().getMaxHealth(this.id);
            AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            maxHealth.setBaseValue(health);
            player.setHealthScale(health);
            player.setHealth(health);
            player.setFoodLevel(20);
            player.setSaturation(20);
            setPlayerArmor(player);
            this.kills.put(player, 0);
        }
    }

    public void endMatch(TTRTeam team) {
        if(team != null) {
            this.status = MatchStatus.ENDED;
            this.lootSpawner.stopSpawning();
            this.getScoreboard().stopScoreboardTask();
            ArrayList<Player> players = new ArrayList<>();
            TTRMatch match = this;
            this.checker.stopChecking();
            for (Player player : this.getPlayers()) {
                player.setGameMode(GameMode.SPECTATOR);
                ChatColor teamColor = TTRCore.getInstance().getConfigManager().getTeamColor(team.getIdentifier());
                player.sendTitle(teamColor + "" + ChatColor.BOLD + team.getIdentifier(), ChatColor.AQUA + "" + PluginString.WIN_OUTPUT, 10, 100, 20);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10, 1);
                player.setBedSpawnLocation(null);
                players.add(player);
            }
            TTRWorldHandler.getWorldHandler(this).enableDayLightCycle();
            TTRWorldHandler.getWorldHandler(this).enableWeatherCycle();
            TTRWorldHandler.getWorldHandler(this).restoreDifficulty();
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {

                    for (Player player : players) {
                        match.getScoreboard().removeScoreboard();
                        player.setGameMode(GameMode.SURVIVAL);
                        player.getInventory().clear();
                        player.getInventory().setItem(0, new ItemBuilder(Material.IRON_SWORD).setName(ChatColor.GOLD + "Sélectionnez un match").toItemStack());
                        match.getTeamHandler().getPlayerTeam(player).removePlayer(player);
                        match.removePlayer(player);
                        match.setStatus(MatchStatus.PREGAME);
                        player.teleport(TTRCore.getInstance().getConfigManager().getMainLobbyLocation());

                    }
                    match.getTeamHandler().getTeams(match).forEach(x -> {
                        x.setPoints(0);
                    });

                }

            }.runTaskLater(TTRCore.getInstance(), 200L);
        } else {

            this.status = MatchStatus.PREGAME;
            this.lootSpawner.stopSpawning();
            this.getScoreboard().stopScoreboardTask();
            this.checker.stopChecking();
            this.getTeamHandler().getTeams(this).forEach(x -> {
                x.setPoints(0);
            });
            TTRWorldHandler.getWorldHandler(this).enableDayLightCycle();
            TTRWorldHandler.getWorldHandler(this).enableWeatherCycle();
            TTRWorldHandler.getWorldHandler(this).restoreDifficulty();
        }

    }

    public void playerDeath(Player player, Player killer) {
        /* try {
            Object packet = ReflectionUtils.createNMSInstance("PacketPlayInClientCommand", List.of(), List.of());
            Class<?> enumClientCommand = ReflectionUtils.getNMSClass("PacketPlayInClientCommand$EnumClientCommand");
            Object performRespawnConstant = null;
            for (Object constant : enumClientCommand.getEnumConstants()) {
                if (constant.toString().equalsIgnoreCase("PERFORM_RESPAWN")) {
                    performRespawnConstant = constant;
                    break;
                }
            }
            if (performRespawnConstant == null) {
                throw new IllegalStateException("Class PacketPlayInClientCommand.EnumClientCommand does not contain a PERFORM_RESPAWN constant...");
            }
            Field a;
            a = packet.getClass().getDeclaredField("a");
            a.setAccessible(true);
            a.set(packet, performRespawnConstant);
            Object playerConnection = ReflectionUtils.getPlayerConnection(player);
            Class<?> packetClass = ReflectionUtils.getNMSClass("PacketPlayInClientCommand");
            Method aMethod = playerConnection.getClass().getMethod("a", packetClass);
            aMethod.invoke(playerConnection, packetClass.cast(packet));
        } catch (Exception e) {
            e.printStackTrace();
        } */
        if (TTRCore.getInstance().getMatchFromWorld(player.getWorld()) != null) {
            /*if (team != null) {
                player.teleport(TTRCore.getInstance().getConfigManager().getTeamSpawn(team.getIdentifier()));
            } */
            setPlayerArmor(player);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 1);
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 10, 1);
            kills.put(killer, getKills(killer) + 1);
        }
    }

    private void setPlayerArmor(Player player) {

        TTRMatch match = this;
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {

                TTRTeam team = match.getTeamHandler().getPlayerTeam(player);
                ChatColor color;
                if (team != null) {
                    color = TTRCore.getInstance().getConfigManager().getTeamColor(team.getIdentifier());
                } else {
                    return;
                }
                ItemStack[] armor = new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS, 1), new ItemStack(Material.LEATHER_LEGGINGS, 1), new ItemStack(Material.LEATHER_CHESTPLATE, 1), new ItemStack(Material.LEATHER_HELMET, 1)};
                for (ItemStack itemStack : armor) {
                    LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                    Color armorColor = Color.fromRGB(0, 0, 0);
                    try {
                        meta.setColor((Color) armorColor.getClass().getDeclaredField(color.name()).get(armorColor));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
                    itemStack.setItemMeta(meta);
                }
                player.getInventory().setArmorContents(armor);
            }

        }.runTaskLater(TTRCore.getInstance(), 1L);
    }


    public MatchStatus getStatus() {
        return this.status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public int getKills(Player player) {
        return this.kills.getOrDefault(player, 0);
    }

    public World getWorld() { return this.world; }

    public TTRScoreboard getScoreboard() { return this.scoreboard; }

    public TTRTeamHandler getTeamHandler() {
        return this.teamHandler;
    }

    public int getId() { return this.id; }



    public void removePlayer(Player player) {
        this.players.remove(player);
    }
    private TTRTeam pickTeam(Player p) {
        TTRTeam lessPlayerTeam = this.getTeamHandler().getTeams(this).get(0);
        TTRTeam current;
        boolean isEqual = true;
        for(int i = 1; this.getTeamHandler().getTeams(this).size() > i ; i++) {
            current =  this.getTeamHandler().getTeams(this).get(i);
            if(current.getPlayers().size() < lessPlayerTeam.getPlayers().size()) {
                isEqual = false;
                lessPlayerTeam = current;
            }
        } if(!isEqual) {
            lessPlayerTeam.addPlayer(p);
            return lessPlayerTeam;
        }
        else {
            Random rand = new Random();
            int pointer = rand.nextInt(this.getTeamHandler().getTeams(this).size());
            this.getTeamHandler().getTeams(this).get(pointer).addPlayer(p);
            return this.getTeamHandler().getTeams(this).get(pointer);
        }
    }

    private void balanceTeams() {
        HashMap<TTRTeam, Integer> teamsBalancing =  new HashMap<>();
        ArrayList<TTRTeam> readyToGive = new ArrayList<>();
        ArrayList<TTRTeam> inNeed = new ArrayList<>();
        int howMuchNeeded = -this.getPlayers().size()/this.getTeamHandler().getTeams(this).size();
        int remaining = this.getPlayers().size()%this.getTeamHandler().getTeams(this).size();
        for(TTRTeam team : this.getTeamHandler().getTeams(this)) {
            teamsBalancing.put(team, howMuchNeeded-remaining+team.getPlayers().size());
            if(remaining!=0) remaining--;
            if(teamsBalancing.get(team) < 0) inNeed.add(team);
            else if(teamsBalancing.get(team) > 0) readyToGive.add(team);
        }
        if(readyToGive.isEmpty()) return;
        if(inNeed.isEmpty()) return;
        for(TTRTeam teamInNeed : inNeed) {
            for(TTRTeam teamThatGives : readyToGive) {
                if(teamsBalancing.get(teamInNeed) >= 0) break;
                for (int i = teamsBalancing.get(teamThatGives); i > 0; i--) {
                    if(teamsBalancing.get(teamInNeed) >= 0) break;
                    Random rand = new Random();
                    int pointer = rand.nextInt(teamThatGives.getPlayers().size());
                    teamInNeed.addPlayer(teamThatGives.getPlayers().get(pointer));
                    teamsBalancing.put(teamInNeed, teamsBalancing.get(teamInNeed)+1);
                    teamThatGives.removePlayer(teamThatGives.getPlayers().get(pointer));
                    teamsBalancing.put(teamThatGives, teamsBalancing.get(teamThatGives)-1);
                }
            }
        }
    }

}
