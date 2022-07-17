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

package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.JoinMatchEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MatchSelector extends CustomUI implements Listener {

    private Player owner;
    private int selected = -1;
    private String lastTeam;

    public MatchSelector(Player player) {
        super(27, "Match Selection");
        this.owner = player;
        setUp();
        TTRCore.getInstance().getServer().getPluginManager().registerEvents(this, TTRCore.getInstance());
    }

    public void openSelector() {
        super.openUI(this.owner);
    }

    public void closeSelector() {
        super.closeUI(this.owner);
    }

    public void setUp() {
        int i = 0;
        for (TTRMatch match : TTRCore.getInstance().getCurrentMatches()) {
            //TODO put lore according to MatchStatus (learn how enum works)
            setSlot(i, new ItemStack(Material.IRON_SWORD, 1), "Match " + match.getId(), null);
            i++;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == super.getInventory() && event.getClickedInventory().getItem(event.getSlot()) != null) {
            this.selected = event.getSlot();
            TTRMatch match =  TTRCore.getInstance().getCurrentMatches().get(event.getSlot());
            Player player = (Player) event.getWhoClicked();
            JoinMatchEvent joinMatchEvent = new JoinMatchEvent(player, match);
            Bukkit.getPluginManager().callEvent(joinMatchEvent);

        }
    }
}
