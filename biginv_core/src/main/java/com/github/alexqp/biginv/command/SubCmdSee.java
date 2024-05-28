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

package com.github.alexqp.biginv.command;

import com.github.alexqp.biginv.data.BigInvContainer;
import com.github.alexqp.biginv.data.BigInvHolder;
import com.github.alexqp.biginv.listeners.BigInvHandler;
import com.github.alexqp.commons.command.AlexSubCommand;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubCmdSee extends BigInvContainerSubCmd {

    SubCmdSee(@NotNull TextComponent helpLine, @NotNull AlexSubCommand parent, @NotNull BigInvContainer container, @NotNull TextComponent noPlayerError, @NotNull TextComponent noDataError) {
        super("see", helpLine, parent, container, noPlayerError, noDataError);
        this.setPermission(BigInvHolder.getSeePermissions().get(0));

        this.setCmdParamLine(new TextComponent("[player]"));
        this.setIsConsoleCmd(false);

        this.makeFinal();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        assert (sender instanceof Player); // is no console cmd

        Player owner;

        if (args.length > startIndex + 2)
            return false;

        if (args.length == startIndex + 1) {
            owner = Bukkit.getPlayer(args[startIndex]);
            if (owner == null) {
                sendNoPlayerError(sender);
                return true;
            }
        } else {
            owner = (Player) sender;
        }

        BigInvHandler.BigInvHandlerReturnType handlerReturnType = this.getContainer().openInventory(owner, (Player) sender);

        if (handlerReturnType.equals(BigInvHandler.BigInvHandlerReturnType.NO_PERMISSION)) {
            sender.spigot().sendMessage(Objects.requireNonNull(this.getNoPermissionLine()));
        } else if (handlerReturnType.equals(BigInvHandler.BigInvHandlerReturnType.NO_DATA)) {
            this.sendNoDataError(sender);
        }
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission(BigInvHolder.getSeePermissions().get(1))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        }
        return completions;
    }
}
