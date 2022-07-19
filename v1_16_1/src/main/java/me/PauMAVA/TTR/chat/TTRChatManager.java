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
 *
 */

package me.PauMAVA.TTR.chat;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

public class TTRChatManager {

    public static void sendMessage(Player sender, String originalMessage) {
        if(TTRCore.getInstance().getMatchFromWorld(sender.getWorld()) != null) {
            if (originalMessage.startsWith("!")) {
                dispatchGlobalMessage(originalMessage, sender);
            } else {
                dispatchTeamMessage(originalMessage, sender);
            }
        } else {
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                if(p.getWorld().equals(sender.getWorld())) {
                    p.sendMessage(originalMessage);
                }
            }
        }
    }

    private static void dispatchGlobalMessage(String string,  Player sender) {
        string  = string.substring(1);
        TTRTeam playerTeam = TTRCore.getInstance().getMatchFromWorld(sender.getWorld()).getTeamHandler().getPlayerTeam(sender);
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if(p.getWorld().equals(sender.getWorld()))
                p.sendMessage(TTRPrefix.TTR_GLOBAL + "" + TTRCore.getInstance().getConfigManager().getTeamColor(playerTeam.getIdentifier()) + sender.getName() + ChatColor.GRAY + " > " + ChatColor.GRAY + string);
        }
    }

    private static void dispatchTeamMessage(String string, Player sender) {
        TTRTeam playerTeam = TTRCore.getInstance().getMatchFromWorld(sender.getWorld()).getTeamHandler().getPlayerTeam(sender);
        if (playerTeam == null) {
            return;
        }
        for (Player p : playerTeam.getPlayers()) {
            p.sendMessage(TTRPrefix.TTR_TEAM + "" + TTRCore.getInstance().getConfigManager().getTeamColor(playerTeam.getIdentifier()) + sender.getName() + ChatColor.GRAY + " > " + ChatColor.GRAY + string);
        }
    }

}
