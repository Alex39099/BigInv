/*
 * Copyright (C) 2019-2024 Alexander Schmid
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.alexqp.biginv.listeners;

import com.github.alexqp.biginv.data.BigInvHolder;
import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BigInvClickEventListener implements Listener {

    private final JavaPlugin plugin;
    private final BigInvHandler handler;

    public BigInvClickEventListener(JavaPlugin plugin, BigInvHandler handler) {
        this.plugin = plugin;
        this.handler = handler;
    }

    private boolean getCancelForEvents(Player clicker, BigInvHolder holder) {
        if (holder.getCanChange(clicker)) {
            holder.setChanged(true);
            ConsoleMessage.debug(this.getClass(), plugin, "Granted " + ConsoleMessage.getPlayerString(clicker) + " changing of biginv from " + ConsoleMessage.getPlayerString(holder.getOwner()));
            return false;
        } else {
            ConsoleMessage.debug(this.getClass(), plugin, "Prevented " + ConsoleMessage.getPlayerString(clicker) + " changing of biginv from " + ConsoleMessage.getPlayerString(holder.getOwner()));
            return true;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player clicker = (Player) e.getWhoClicked();

        // open by crafting slot?
        if (e.getInventory().getType().equals(InventoryType.CRAFTING) && e.getSlotType().equals(InventoryType.SlotType.RESULT) && e.getInventory().getItem(e.getRawSlot()) == null) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    handler.openInventory(clicker, clicker);
                }
            }.runTask(plugin);
        }

        // prevent changing?
        if (e.getClickedInventory() != null && (e.getClickedInventory().getHolder() instanceof BigInvHolder)) {
            BigInvHolder holder = (BigInvHolder) e.getClickedInventory().getHolder();
            e.setCancelled(this.getCancelForEvents(clicker, holder));
        }

        // shift clicks on bottom inventory
        else if ((e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT))
                    && e.getView().getTopInventory().getHolder() instanceof BigInvHolder) {
            BigInvHolder holder = (BigInvHolder) e.getView().getTopInventory().getHolder();
            e.setCancelled(this.getCancelForEvents(clicker, holder));
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player clicker = (Player) e.getWhoClicked();

        if (e.getView().getTopInventory().getHolder() instanceof BigInvHolder) {
            BigInvHolder holder = (BigInvHolder) e.getView().getTopInventory().getHolder();
            e.setCancelled(this.getCancelForEvents(clicker, holder));
        }
    }
}
