/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 *
 * instance program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * instance program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with instance program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.PauMAVA.TTR;

import me.PauMAVA.TTR.commands.EnableDisableCommand;
import me.PauMAVA.TTR.commands.StartMatchCommand;
import me.PauMAVA.TTR.config.TTRConfigManager;
import me.PauMAVA.TTR.lang.LanguageManager;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.match.AutoStarter;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.teams.TTRTeamHandler;
import me.PauMAVA.TTR.ui.TTRScoreboard;
import me.PauMAVA.TTR.util.EventListener;
import me.PauMAVA.TTR.util.PacketInterceptor;
import me.PauMAVA.TTR.world.TTRWorldHandler;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TTRCore extends JavaPlugin {

    private static TTRCore instance;
    private boolean enabled = false;
    private ArrayList<TTRMatch> matches = new ArrayList<>();

    private TTRConfigManager configManager;
    private ArrayList<TTRWorldHandler> worldHandler = new ArrayList<>();

    private AutoStarter autoStarter;
    private LanguageManager languageManager;
    private PacketInterceptor packetInterceptor;

    private boolean isCounting = false;

    @Override
    public void onEnable() {
        instance = this;
        new BukkitRunnable() {

            @Override
            public void run() {
                instance.configManager = new TTRConfigManager(instance.getConfig());
                instance.languageManager = new LanguageManager(instance);
                if (instance.getConfig().getBoolean("enable_on_start")) {
                    enabled = true;
                } else {
                    getLogger().warning("" + PluginString.DISABLED_ON_STARTUP_NOTICE);
                }
                instance.packetInterceptor = new PacketInterceptor(instance);
                if (enabled) {
                    instance.autoStarter = new AutoStarter(instance, instance.getConfig());
                    for(TTRMatch match : configManager.getMatchesRegistered()) {
                        instance.matches.add(match);
                        instance.autoStarter.addMatch(match);
                        instance.worldHandler.add(new TTRWorldHandler(instance, match));
                        TTRWorldHandler.getWorldHandler(match).setUpWorld();
                    }

                    instance.getServer().getPluginManager().registerEvents(new EventListener(instance), instance);
                } else {
                    for(TTRMatch match : instance.matches) {
                        match.setStatus(MatchStatus.DISABLED);
                    }
                }

            }

        }.runTaskLater(this, 1);
        
        

        this.getCommand("ttrstart").setExecutor(new StartMatchCommand());
        EnableDisableCommand enableDisableCommand = new EnableDisableCommand(this);
        this.getCommand("ttrenable").setExecutor(enableDisableCommand);
        this.getCommand("ttrdisable").setExecutor(enableDisableCommand);

    }

    @Override
    public void onDisable() {
        try {
            for(TTRMatch match : this.getCurrentMatches()) {
                match.getScoreboard().removeScoreboard();
            }
            for (Player player: this.getServer().getOnlinePlayers()) {
                this.packetInterceptor.removePlayer(player);
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static TTRCore getInstance() {
        return instance;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public ArrayList<TTRMatch> getCurrentMatches() {
        return this.matches;
    }

    @Nullable
    public TTRMatch getMatchFromWorld(World world) {
        for(TTRMatch match : getCurrentMatches()) {

            if(match.getWorld().equals(world)) return match;
        }
        //TODO add placeholder match
        return null;
    }
    public TTRConfigManager getConfigManager() {
        return this.configManager;
    }

    public boolean isCounting() {
        return isCounting;
    }

    public void setCounting(boolean counting) {
        isCounting = counting;
    }

    public AutoStarter getAutoStarter() {
        return autoStarter;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public PacketInterceptor getPacketInterceptor() {
        return packetInterceptor;
    }

}
