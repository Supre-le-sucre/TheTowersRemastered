/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2020  Pau Machetti Vallverdu
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
package me.PauMAVA.TTR;

import me.PauMAVA.TTR.commands.StartMatchCommand;
import me.PauMAVA.TTR.config.TTRConfigManager;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.teams.TTRTeamHandler;
import me.PauMAVA.TTR.util.EventListener;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class TTRCore extends JavaPlugin {

    private static TTRCore instance;
    private boolean enabled = true;
    private TTRMatch match;
    private TTRTeamHandler teamHandler;
    private TTRConfigManager configManager;
    private World matchWorld;

    @Override
    public void onEnable() {
        instance = this;
        if (this.getConfig().getBoolean("enableOnStart")) {
            enabled = true;
        }
        if(enabled) {
            this.match = new TTRMatch(MatchStatus.PREGAME);
        } else {
            this.match = new TTRMatch(MatchStatus.DISABLED);
        }
        this.teamHandler = new TTRTeamHandler();
        this.teamHandler.setUpDefaultTeams();
        this.configManager = new TTRConfigManager(this.getConfig());
        this.matchWorld = this.getServer().getWorlds().get(0);
        this.matchWorld.setSpawnLocation(this.configManager.getLobbyLocation());
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getCommand("ttrstart").setExecutor(new StartMatchCommand());
    }

    @Override
    public void onDisable() {

    }

    public static TTRCore getInstance() {
        return instance;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public TTRMatch getCurrentMatch() {
        return this.match;
    }

    public TTRTeamHandler getTeamHandler() {
        return this.teamHandler;
    }

    public TTRConfigManager getConfigManager() {
        return this.configManager;
    }

    public World getMatchWorld() {
        return this.matchWorld;
    }
}
