/*
 *  TheTowersRemastered (TTR)
 *  Copyright (c) 2019-2021  Pau Machetti Vallverdú
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.PauMAVA.TTR.config;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

public class TTRConfigManager {

    private FileConfiguration configuration;
    private World world;
    private ConfigurationSection teamsSection;
    private ConfigurationSection lobbySection;
    private ConfigurationSection autoStartSection;
    private static ArrayList<TTRMatch> matches = new ArrayList<TTRMatch>();

    public TTRConfigManager(FileConfiguration configuration) {
        this.configuration = configuration;
        this.world = Bukkit.getServer().getWorld("TheTower");
        if (!new File(TTRCore.getInstance().getDataFolder() + "/config.yml").exists()) {
            setUpFile();
            saveConfig();
        } else {
            this.teamsSection = this.configuration.getConfigurationSection("teams");
            this.autoStartSection = this.configuration.getConfigurationSection("autostart");
            this.lobbySection = this.configuration.getConfigurationSection("lobby");
        }
    }

    public int getMaxPoints(int id) {
        return this.configuration.getConfigurationSection("match."+id).getInt("maxpoints");
    }

    public int getMaxHealth(int id) {
        return this.configuration.getConfigurationSection("match."+id).getInt("maxhealth");
    }

    public int getTime(int id) {
        return this.configuration.getConfigurationSection("match."+id).getInt("time");
    }

    public String getWeather(int id) {
        return this.configuration.getConfigurationSection("match."+id).getString("weather");
    }

    public Location getPreGameLobbyLocation(int id) { return this.configuration.getConfigurationSection("match."+id+".map").getLocation("lobby"); }
    public Location getMainLobbyLocation() { return this.lobbySection.getLocation("main_lobby"); }

    public List<Location> getIronSpawns(int id) {
        return (List<Location>) this.configuration.getConfigurationSection("match."+id+".map").getList("ironspawns");
    }

    public List<Location> getXPSpawns(int id) {
        return (List<Location>) this.configuration.getConfigurationSection("match."+id+".map").getList("xpspawns");
    }

    public int getTeamCount() {
        return this.teamsSection.getKeys(false).size();
    }

    public Set<String> getTeamNames() {
        return this.teamsSection.getKeys(false);
    }

    private ConfigurationSection getTeam(String teamName) {
        for (String key : this.teamsSection.getKeys(false)) {
            if (key.equalsIgnoreCase(teamName)) {
                return this.teamsSection.getConfigurationSection(key);
            }
        }
        return null;
    }

    public ChatColor getTeamColor(String teamName) {
        return ChatColor.valueOf(getTeam(teamName).getString("color"));
    }

    public Location getTeamSpawn(String teamName) {
        return getTeam(teamName).getLocation("spawn");
    }

    public Location getTeamCage(String teamName) {
        return getTeam(teamName).getLocation("cage");
    }

    public HashMap<Location, TTRTeam> getTeamCages(TTRMatch match) {
        HashMap<Location, TTRTeam> cages = new HashMap<Location, TTRTeam>();
        for (String teamName : getTeamNames()) {
            cages.put(getTeamCage(teamName), match.getTeamHandler().getTeam(teamName, match));
        }
        return cages;
    }

    public boolean isEnabled() {
        return this.configuration.getBoolean("enable_on_start");
    }

    public void setEnableOnStart(boolean value) {
        this.configuration.set("enable_on_start", value);
        saveConfig();
    }

    public String getLocale() {
        return this.configuration.getString("locale");
    }

    private void saveConfig() {
        TTRCore.getInstance().saveConfig();
    }

    public void resetFile() {
        setUpFile();
    }

    public ArrayList<TTRMatch> getMatchesRegistered() {
        int i = 0;
        if(matches.isEmpty()) {
            while (true) {
                ConfigurationSection section = this.configuration.getConfigurationSection("match." + i);
                if (section != null) {
                    matches.add(new TTRMatch(MatchStatus.PREGAME, this.configuration.getConfigurationSection("match." + i + ".map").getLocation("lobby").getWorld(), i));
                } else {
                    return matches;
                }
                i++;

            }
        } else {
            return matches;
        }
    }

    private void setUpFile() {
        this.configuration.addDefault("enable_on_start", false);
        this.configuration.addDefault("locale", "en");
        this.autoStartSection = this.configuration.createSection("autostart");
        autoStartSection.addDefault("enabled", true);
        autoStartSection.addDefault("count", 4);
        this.lobbySection = this.configuration.createSection("lobby");
        this.lobbySection.addDefault("main_lobby", new Location(Bukkit.getWorld("TheTowerHub"), 0,0,0));
        ConfigurationSection match0 = this.configuration.createSection("match.0");
        match0.addDefault("time", 10000);
        match0.addDefault("weather", "CLEAR");
        match0.addDefault("maxpoints", 10);
        match0.addDefault("maxhealth", 20);
        ConfigurationSection mapSection = this.configuration.createSection("match.0.map");
        mapSection.addDefault("lobby", new Location(this.world, 0, 207, 1014));
        mapSection.addDefault("ironspawns", new ArrayList<Location>(Arrays.asList(new Location(this.world, -0, 206, 1138))));
        mapSection.addDefault("xpspawns", new ArrayList<Location>(Arrays.asList(new Location(this.world, -0, 206, 1166))));
        this.teamsSection = this.configuration.createSection("teams");
        ConfigurationSection team1section = teamsSection.createSection("Red Team");
        ConfigurationSection team2section = teamsSection.createSection("Blue Team");
        team1section.addDefault("color", "RED");
        team2section.addDefault("color", "BLUE");
        team1section.addDefault("spawn", new Location(this.world, 84, 192, 1152));
        team2section.addDefault("spawn", new Location(this.world, -83, 192, 1152));
        team1section.addDefault("cage", new Location(this.world, 84, 200, 1152));
        team2section.addDefault("cage", new Location(this.world, -83, 200, 1152));
        this.configuration.options().copyDefaults(true);
    }
}