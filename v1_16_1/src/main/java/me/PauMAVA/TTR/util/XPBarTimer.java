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
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class XPBarTimer {

    private int i;

    private TTRMatch match;
    private static ArrayList<XPBarTimer> instances = new ArrayList<>();

    public XPBarTimer(int time) {
        this.i = time;
        instances.add(this);
    }

    public XPBarTimer(int time, TTRMatch match) {
        this.i = time;
        this.match = match;
        if(match == null) return;
        instances.add(this);
        match.setStatus(MatchStatus.ONCOUNTDOWN);
        BukkitTask timer = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : match.getPlayers()) {
                    player.setLevel(getMatchTimer(match).getTime());
                }
                if (getMatchTimer(match).getTime() <= 5) {
                    for (Player player : match.getPlayers()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                    }
                }
                if (getMatchTimer(match).getTime() <= 0) {
                    match.startMatch();
                    cancel();
                    return;
                }
                getMatchTimer(match).setTime(getMatchTimer(match).getTime()-1);
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 20L);
    }

    public int getTime() { return this.i; }

    public TTRMatch getMatch() { return this.match; }

    public XPBarTimer getMatchTimer(TTRMatch match) {
        if(match.getStatus().equals(MatchStatus.ONCOUNTDOWN)) {
            for (XPBarTimer timer : XPBarTimer.getInstances()) {
                if(timer.getMatch().equals(match)) return timer;
            }
            return null;
        } else return null;
    }

    public void setTime(int time) { this.i = time;}

    public static ArrayList<XPBarTimer> getInstances() {
        return instances;
    }


}
