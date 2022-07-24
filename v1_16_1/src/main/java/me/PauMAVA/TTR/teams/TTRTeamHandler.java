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

package me.PauMAVA.TTR.teams;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.util.JoinTeamEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TTRTeamHandler {
    private HashMap<TTRMatch, List<TTRTeam>> teams = new HashMap<>();

    public void setUpDefaultTeams(TTRMatch match) {
        this.teams.put(match, new ArrayList<>());
        for (String team : TTRCore.getInstance().getConfigManager().getTeamNames()) {
            this.teams.get(match).add(new TTRTeam(team));
        }
    }

    public boolean addPlayerToTeam(Player player, String teamIdentifier) {
        TTRMatch match = TTRCore.getInstance().getMatchFromWorld(player.getWorld());
        TTRTeam team = getTeam(teamIdentifier, match);
        if (team == null) {
            return false;
        }
        team.addPlayer(player);
        JoinTeamEvent event = new JoinTeamEvent(player, match, getTeam(teamIdentifier, match));
        Bukkit.getPluginManager().callEvent(event);
        return true;
    }

    public boolean removePlayerFromTeam(Player player, String teamIdentifier) {
        TTRMatch match = TTRCore.getInstance().getMatchFromWorld(player.getWorld());
        TTRTeam team = getTeam(teamIdentifier, match);
        if (team == null) {
            return false;
        }
        team.removePlayer(player);
        return true;
    }

    public TTRTeam getPlayerTeam(Player player) {
        TTRMatch match = TTRCore.getInstance().getMatchFromWorld(player.getWorld());
        for (TTRTeam team : this.teams.get(match)) {
            if (team.getPlayers().contains(player)) {
                return team;
            }
        }
        return null;
    }

    public TTRTeam getTeam(String teamIdentifier, TTRMatch match) {
        for (TTRTeam team : this.teams.get(match)) {
            teamIdentifier = ChatColor.stripColor(teamIdentifier);
            if (teamIdentifier.contentEquals(team.getIdentifier())) {
                return team;
            }
        }
        return null;
    }

    public List<TTRTeam> getTeams(TTRMatch match) {
        return this.teams.get(match);
    }

    public void addPlayer(String teamIdentifier, Player player) {
        TTRMatch match = TTRCore.getInstance().getMatchFromWorld(player.getWorld());
        if(getTeam(teamIdentifier, match).getPlayers().contains(player)) return;
        getTeam(teamIdentifier, match).addPlayer(player);
        JoinTeamEvent event = new JoinTeamEvent(player, match, getTeam(teamIdentifier, match));
        Bukkit.getPluginManager().callEvent(event);
    }

    public void removePlayer(String teamIdentifier, Player player) {
        TTRMatch match = TTRCore.getInstance().getMatchFromWorld(player.getWorld());
        if(!getTeam(teamIdentifier, match).getPlayers().contains(player)) return;
        getTeam(teamIdentifier, match).removePlayer(player);
    }
}
