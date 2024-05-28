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
import com.github.alexqp.commons.command.AlexCommand;
import com.github.alexqp.commons.command.AlexSubCommand;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.commons.messages.MessageTranslator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BigInvCommand extends AlexCommand {

    public BigInvCommand(@NotNull JavaPlugin plugin, @NotNull BigInvContainer container, ConfigurationSection msgSection) {
        super("biginv", plugin, ChatColor.AQUA);

        ConfigChecker configChecker = new ConfigChecker(plugin);
        this.setNoPermissionLine(MessageTranslator.translateBukkitColorCodes(Objects.requireNonNull(configChecker.checkString(msgSection, "no_permission", ConsoleErrorType.WARN, "&4You do not have permission."))));

        String cmdPrefixDefault = "default";
        String cmdPrefix = configChecker.checkString(msgSection, "cmd_prefix", ConsoleErrorType.WARN, cmdPrefixDefault);
        assert cmdPrefix != null;
        if (!cmdPrefix.equals(cmdPrefixDefault)) {
            this.setPrefix(MessageTranslator.translateBukkitColorCodes(cmdPrefix));
            ConsoleMessage.debug(this.getClass(), plugin, "Command prefix was changed.");
        }

        String creditLine = "Use /biginv help for all available commands.";
        String mechanicExplanationLine = "To access your bigger inventory, click on your crafting result while not crafting.";

        ConfigurationSection section = configChecker.checkConfigSection(msgSection, "credits", ConsoleErrorType.ERROR);
        if (section != null) {
            creditLine = configChecker.checkString(section, "credit", ConsoleErrorType.WARN, creditLine);
            mechanicExplanationLine = configChecker.checkString(section, "mechanic_explanation", ConsoleErrorType.WARN, mechanicExplanationLine);
        }
        assert creditLine != null;
        assert mechanicExplanationLine != null;
        this.addCreditLine(MessageTranslator.translateBukkitColorCodes(creditLine));
        this.addCreditLine(MessageTranslator.translateBukkitColorCodes(mechanicExplanationLine));

        String helpHeader = "List of all available commands:";
        String seeHelpLine = "Opens a player's bigger inventory.";
        String backupHelpLine = "Restores a backup into the target's inventory.";
        section = configChecker.checkConfigSection(msgSection, "help", ConsoleErrorType.ERROR);
        if (section != null) {
            helpHeader = configChecker.checkString(section, "header", ConsoleErrorType.WARN, helpHeader);
            seeHelpLine = configChecker.checkString(section, "see", ConsoleErrorType.WARN, seeHelpLine);
            backupHelpLine = configChecker.checkString(section, "backup", ConsoleErrorType.WARN, backupHelpLine);
        }
        assert helpHeader != null;
        this.addHelpCmdHeaderLine(MessageTranslator.translateBukkitColorCodes(helpHeader));

        String noPlayerMsg = "&CThere is no player with this name online.";
        section = configChecker.checkConfigSection(msgSection, "wrongCmdUsage", ConsoleErrorType.ERROR);
        if (section != null) {
            this.setUsagePrefix(MessageTranslator.translateBukkitColorCodes(Objects.requireNonNull(configChecker.checkString(section, "prefix", ConsoleErrorType.WARN, "&CUsage:"))));
            noPlayerMsg = configChecker.checkString(section, "no_player", ConsoleErrorType.WARN, noPlayerMsg);
        }

        String seeNoDataLine = "&6There is no biginv available";
        section = configChecker.checkConfigSection(msgSection, "seeCmd", ConsoleErrorType.ERROR);
        if (section != null) {
            seeNoDataLine = configChecker.checkString(section, "no_data", ConsoleErrorType.WARN, seeNoDataLine);
        }

        String backupNoDataLine = "&6There is no backup available";
        String backupSuccessLine = "&2Backup restoring was successful.";
        String backupPartialSuccessLine = "&6Added items to inventory, but there are still items left in the backup.";
        section = configChecker.checkConfigSection(msgSection, "backupCmd", ConsoleErrorType.ERROR);
        if (section != null) {
            backupNoDataLine = configChecker.checkString(section, "no_data", ConsoleErrorType.WARN, backupNoDataLine);
            backupSuccessLine = configChecker.checkString(section, "success", ConsoleErrorType.WARN, backupSuccessLine);
            backupPartialSuccessLine = configChecker.checkString(section, "partial_success", ConsoleErrorType.WARN,  backupPartialSuccessLine);
        }

        assert noPlayerMsg != null;
        TextComponent noPlayerError = new TextComponent(MessageTranslator.translateBukkitColorCodes(noPlayerMsg));

        List<AlexSubCommand> subCmds = new ArrayList<>();

        assert seeHelpLine != null;
        assert seeNoDataLine != null;
        subCmds.add(new SubCmdSee(new TextComponent(MessageTranslator.translateBukkitColorCodes(seeHelpLine)), this, container, noPlayerError,
                new TextComponent(MessageTranslator.translateBukkitColorCodes(seeNoDataLine))));

        assert backupHelpLine != null;
        assert backupNoDataLine != null;
        assert backupPartialSuccessLine != null;
        assert backupSuccessLine != null;
        subCmds.add(new SubCmdBackup(new TextComponent(MessageTranslator.translateBukkitColorCodes(backupHelpLine)), this, container, noPlayerError,
                new TextComponent(MessageTranslator.translateBukkitColorCodes(backupNoDataLine)),
                new TextComponent(MessageTranslator.translateBukkitColorCodes(backupPartialSuccessLine)),
                new TextComponent(MessageTranslator.translateBukkitColorCodes(backupSuccessLine))));

        this.addSubCmds(subCmds);
        this.register();
    }
}
