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
import com.github.alexqp.commons.command.AlexSubCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class BigInvContainerSubCmd extends AlexSubCommand {

    private final BigInvContainer container;
    private BaseComponent noPlayerError;
    private BaseComponent noDataError;

    protected BigInvContainerSubCmd(@NotNull String name, @NotNull TextComponent helpLine, @NotNull AlexSubCommand parent,
            @NotNull BigInvContainer container, @NotNull TextComponent noPlayerError, @NotNull TextComponent noDataError) {
        super(name, helpLine, parent);
        this.container = container;

        this.noPlayerError = noPlayerError.duplicate();
        this.noDataError = noDataError.duplicate();
    }

    protected BigInvContainer getContainer() {
        return this.container;
    }

    public void sendNoPlayerError(@NotNull CommandSender sender) {
        sender.spigot().sendMessage(noPlayerError);
    }

    public void sendNoDataError(@NotNull CommandSender sender) {
        sender.spigot().sendMessage(noDataError);
    }

    @Override
    public void makeFinal() {
        this.internalMakeFinal();
        this.noPlayerError = this.getPrefixMessage(noPlayerError);
        this.noDataError = this.getPrefixMessage(noDataError);
    }
}
