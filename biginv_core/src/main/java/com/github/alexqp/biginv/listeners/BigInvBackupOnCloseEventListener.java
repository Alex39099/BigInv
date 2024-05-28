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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Objects;

public class BigInvBackupOnCloseEventListener implements Listener {

    private final BigInvHandler handler;

    public BigInvBackupOnCloseEventListener(BigInvHandler handler) {
        this.handler = Objects.requireNonNull(handler, "BigInvHandler must not be null");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof BigInvHolder) {

            BigInvHolder holder = (BigInvHolder) e.getInventory().getHolder();
            if (holder.isChanged()) {
                handler.saveInventory(holder.getOwner(), false);
                holder.setChanged(false);
            }
        }
    }
}
