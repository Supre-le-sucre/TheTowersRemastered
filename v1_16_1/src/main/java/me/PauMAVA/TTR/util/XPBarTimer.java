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
        if(match.getStatus().equals(MatchStatus.ONCOUNTDOWN)) return;
        this.i = time;
        this.match = match;
        if(match == null) return;
        instances.add(this);
        XPBarTimer XPtimer = this;
        match.setStatus(MatchStatus.ONCOUNTDOWN);
        for (Player player : match.getPlayers()) {
            player.setLevel(this.i);
            player.setExp(1.0f);
        }
        final int[] tick = {0};
        BukkitTask timer = new BukkitRunnable() {
            @Override
            public void run() {

                if(match.getPlayers().size() <= 1) {
                    cancel();
                    match.setStatus(MatchStatus.PREGAME);
                    if(match.getPlayers().size() == 1)
                        match.getPlayers().forEach(x -> {
                            x.setLevel(0);
                            x.setExp(0.0f);
                        });
                    return;
                }

                if(tick[0] == 20) {
                    getMatchTimer(match).setTime(getMatchTimer(match).getTime()-1);
                    tick[0] = 0;
                }

                for (Player player : match.getPlayers()) {
                    if(player.getExp() == 0.0f) {
                        player.setLevel(getMatchTimer(match).getTime());
                        player.setExp(1.0f);
                        if (getMatchTimer(match).getTime() <= 5) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                        }
                    }
                    if(player.getExp()-0.05f >= 0) player.setExp(player.getExp()-0.05f);
                    else player.setExp(0.0f);


                }

                if (getMatchTimer(match).getTime() <= 0) {
                    match.startMatch();
                    instances.remove(XPtimer);
                    cancel();
                    return;
                }
                tick[0]++;

            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 1L);
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
