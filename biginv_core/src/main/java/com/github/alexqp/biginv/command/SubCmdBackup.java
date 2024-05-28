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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SubCmdBackup extends BigInvContainerSubCmd {

    private BaseComponent consoleError = new TextComponent(new ComponentBuilder("all values must be specified if command is performed via console.").color(ChatColor.RED).create());
    private BaseComponent partialSuccess;
    private BaseComponent success;

    SubCmdBackup(@NotNull TextComponent helpLine, @NotNull AlexSubCommand parent, @NotNull BigInvContainer container,
                 @NotNull TextComponent noPlayerError, @NotNull TextComponent noDataError, @NotNull  TextComponent partialSuccess, @NotNull TextComponent success) {
        super("backup", helpLine, parent, container, noPlayerError, noDataError);
        this.setPermission(BigInvHolder.getBackupPermissions().get(0));

        this.partialSuccess = partialSuccess;
        this.success = success;

        this.setCmdParamLine(new TextComponent("[player] [target]"));

        this.doFinalizing();
    }

    private void doFinalizing() {
        this.makeFinal();
        this.consoleError = this.getPrefixMessage(consoleError);
        this.partialSuccess = this.getPrefixMessage(partialSuccess);
        this.success = this.getPrefixMessage(success);
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        Player owner;
        Player target;

        if (args.length < startIndex + 2 && (sender instanceof ConsoleCommandSender)) {
            sendMessage(sender, consoleError);
            return true;
        }

        if (args.length > startIndex + 2)
            return false;

        if (args.length >= startIndex + 1) {

            owner = Bukkit.getPlayer(args[startIndex]);

            if (args.length >= startIndex + 2) {
                target = Bukkit.getPlayer(args[startIndex + 1]);
            } else {
                target = (Player) sender;
            }

            if (owner == null || target == null) {
                this.sendNoPlayerError(sender);
                return true;
            }
        } else {
            owner = (Player) sender;
            target = (Player) sender;
        }

        BigInvHandler.BigInvHandlerReturnType handlerReturnType = this.getContainer().restoreBackup(owner, target);

        if (handlerReturnType.equals(BigInvHandler.BigInvHandlerReturnType.NO_PERMISSION)) {
            sendMessage(sender, Objects.requireNonNull(this.getNoPermissionLine()));
        } else if (handlerReturnType.equals(BigInvHandler.BigInvHandlerReturnType.PARTIAL_SUCCESS)) {
            sendMessage(sender, partialSuccess);
        } else if (handlerReturnType.equals(BigInvHandler.BigInvHandlerReturnType.SUCCESS)) {
            sendMessage(sender, success);
        } else if (handlerReturnType.equals(BigInvHandler.BigInvHandlerReturnType.NO_DATA)) {
            this.sendNoDataError(sender);
        } else {
            sendMessage(sender, new ComponentBuilder("INTERNAL ERROR. Please contact server administration with error: no BigInvHandlerReturnType available").color(ChatColor.DARK_RED).create());
        }
        return true;
    }

    @Override
    protected @NotNull List<String> additionalTabCompleterOptions(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission(BigInvHolder.getBackupPermissions().get(1))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        }
        return completions;
    }

    @Override
    protected @NotNull List<String> getTabCompletion(@NotNull CommandSender sender, @NotNull String label, @NotNull List<AlexSubCommand> previousCmds, @NotNull List<String> previousExtraArguments, @NotNull String[] args, int startIndex) {
        List<String> completions = new ArrayList<>();
        if (args.length > startIndex && sender.hasPermission(BigInvHolder.getBackupPermissions().get(1))) {
            List<String> pNames = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                pNames.add(p.getName());
            }
            StringUtil.copyPartialMatches(args[startIndex], pNames, completions);
            Collections.sort(completions);
        }
        return completions;
    }
}
