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

import com.github.alexqp.biginv.data.BigInvForceCloseEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BigInvForceCloseEventListener implements Listener {

    private final String msg;

    public BigInvForceCloseEventListener(String rawCloseMsg) {
        this.msg = ChatColor.translateAlternateColorCodes('&', rawCloseMsg);
    }

    @EventHandler
    public void onInvClose(BigInvForceCloseEvent e) {
        e.getViewer().sendMessage(msg);
    }
}
