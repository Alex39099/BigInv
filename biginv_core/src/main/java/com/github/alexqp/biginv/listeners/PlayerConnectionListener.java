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

import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerConnectionListener implements Listener {

    private final JavaPlugin plugin;
    private final BigInvHandler handler;
    private final String neededBackupMsg;

    public PlayerConnectionListener(JavaPlugin plugin, BigInvHandler handler, String rawNeededBackupMsg) {
        this.plugin = plugin;
        this.handler = handler;
        this.neededBackupMsg = ChatColor.translateAlternateColorCodes('&', rawNeededBackupMsg);
    }

    @EventHandler
    public void onConnect(PlayerJoinEvent e) {
        ConsoleMessage.debug(this.getClass(), plugin, "Initiating loading of BigInv for " + ConsoleMessage.getPlayerString(e.getPlayer()) + "...");
        if (handler.loadInventory(e.getPlayer()).equals(BigInvHandler.BigInvHandlerReturnType.NEEDED_BACKUP)) {
            e.getPlayer().sendMessage(neededBackupMsg);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        ConsoleMessage.debug(this.getClass(), plugin, "Initiating saving of BigInv for " + ConsoleMessage.getPlayerString(e.getPlayer()) + "...");
        handler.saveInventory(e.getPlayer());
    }
}
