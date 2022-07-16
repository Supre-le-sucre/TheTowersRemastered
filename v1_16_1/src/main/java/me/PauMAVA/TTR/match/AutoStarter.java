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
import me.PauMAVA.TTR.util.XPBarTimer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoStarter {

    private TTRCore plugin;

    private boolean enabled;

    private int target;

    private static HashMap<TTRMatch, List<Player>> queue = new HashMap<>();

    public AutoStarter(TTRCore plugin, FileConfiguration configuration) {
        this.plugin = plugin;
        this.target = configuration.getInt("autostart.count");
        this.enabled = configuration.getBoolean("autostart.enabled");
    }

    public void addMatch(TTRMatch match) {
        queue.put(match, new ArrayList<>());
    }

    public void addPlayerToQueue(Player player, TTRMatch match) {
        if (enabled) {
            if (!isPlayerInQueue(player, match)) {
                queue.get(match).add(player);
                match.addPlayer(player);
            }
            if (enabled && target <= queue.get(match).size()) {
                new XPBarTimer(20, match);
            }
        }
    }

    public void removePlayerFromQueue(Player player, TTRMatch match) {
        if (enabled) {
            queue.getOrDefault(match, new ArrayList<>()).remove(player);
            match.removePlayer(player);

        }
    }

    /* private void checkStartGame() {
        if (enabled && target <= queue.size()) {
            try {
                new XPBarTimer(20, plugin.getCurrentMatch().getClass().getMethod("startMatch")).runTaskTimer(plugin, 0L, 20L);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    } */

    private boolean isPlayerInQueue(Player player, TTRMatch match) {
        for (Player p : queue.get(match)) {
            if (player.getUniqueId().equals(p.getUniqueId())) {
                return true;
            }
        }
        return false;
    }


}
