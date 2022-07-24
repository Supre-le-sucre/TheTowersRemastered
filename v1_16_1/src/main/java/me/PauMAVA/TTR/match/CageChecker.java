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
import me.PauMAVA.TTR.chat.TTRChatManager;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CageChecker {

    private List<Cage> cages = new ArrayList<Cage>();
    private int checkerTaskPID;


    public void startChecking(TTRMatch match) {
        this.checkerTaskPID = new BukkitRunnable() {

            @Override
            public void run() {
                for (Player p : match.getPlayers()) {
                    for (Cage cage : cages) {
                        Location particleLocation = new Location(cage.getLocation().getWorld(), cage.getLocation().getBlockX(), cage.getLocation().getBlockY() + 1, cage.getLocation().getBlockZ());
                        particleLocation.add(particleLocation.getX() > 0 ? 0.5 : -0.5, 0.0, particleLocation.getZ() > 0 ? 0.5 : -0.5);
                        cage.getLocation().getWorld().spawnParticle(Particle.SPELL, particleLocation, 100);
                        if (cage.isInCage(p) && match.getTeamHandler().getPlayerTeam(p) != null) {
                            if (cage.getOwner().equals(match.getTeamHandler().getPlayerTeam(p))) {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 10, 1);
                                p.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + PluginString.ALLY_CAGE_ENTER_OUTPUT);
                                p.teleport(TTRCore.getInstance().getConfigManager().getTeamSpawn(match.getTeamHandler().getPlayerTeam(p).getIdentifier()));
                            } else {
                                cage.getLocation().getWorld().strikeLightningEffect(cage.getLocation());
                                playerOnCage(p);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 10L).getTaskId();
    }

    public void stopChecking() {
        Bukkit.getScheduler().cancelTask(this.checkerTaskPID);
    }

    private void playerOnCage(Player player) {
        if(TTRCore.getInstance().getMatchFromWorld(player.getWorld()) != null) {
            TTRTeam playersTeam = TTRCore.getInstance().getMatchFromWorld(player.getWorld()).getTeamHandler().getPlayerTeam(player);
            player.teleport(TTRCore.getInstance().getConfigManager().getTeamSpawn(playersTeam.getIdentifier()));
            playersTeam.addPoints(1);
            TTRCore.getInstance().getMatchFromWorld(player.getWorld()).getScoreboard().refreshScoreboard();
            TTRChatManager.broadcastMatchMessage(TTRCore.getInstance().getMatchFromWorld(player.getWorld()),TTRPrefix.TTR_GAME + "" + TTRCore.getInstance().getConfigManager().getTeamColor(playersTeam.getIdentifier()) + player.getName() + PluginString.SCORE_OUTPUT);
            for (Player p : TTRCore.getInstance().getMatchFromWorld(player.getWorld()).getPlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
            }
            if (playersTeam.getPoints() >= TTRCore.getInstance().getConfigManager().getMaxPoints(TTRCore.getInstance().getMatchFromWorld(player.getWorld()).getId())) {
                TTRCore.getInstance().getMatchFromWorld(player.getLocation().getWorld()).endMatch(playersTeam);
            }
        }
    }

    public void setCages(HashMap<Location, TTRTeam> cages, int effectiveRadius) {
        for (Location cage : cages.keySet()) {
            this.cages.add(new Cage(cage, effectiveRadius, cages.get(cage)));
        }
    }
}
