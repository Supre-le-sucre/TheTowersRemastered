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

package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.util.TTRPrefix;
import me.PauMAVA.TTR.util.XPBarTimer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartMatchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender theSender, Command command, String label, String[] args) {
        //TODO add permission
        if (TTRCore.getInstance().enabled() && theSender instanceof Player && !TTRCore.getInstance().getMatchFromWorld(((Player)theSender).getWorld()).isOnCourse()) {
            int timer;
            if (args == null || args.length == 0) {
                timer = 10;
            } else {
                try {
                    timer = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    theSender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GRAY + PluginString.ERROR_EXPECTED_INTEGER);
                    return false;
                }
            }
            new XPBarTimer(timer, TTRCore.getInstance().getMatchFromWorld(((Player)theSender).getWorld()));

        }
        return false;
    }
}
