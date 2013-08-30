/*
 * Copyright 2013 Alan Kristensen. All rights reserved.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  See LICENSE in the jar file. If not, 
 *   see {http://www.gnu.org/licenses/}.
 */
package ws.kristensen.HopperFilterSimplified;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HfsCommandListener implements CommandExecutor {
    private final HopperFilterSimplified plugin;

    public HfsCommandListener(HopperFilterSimplified plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (!player.hasPermission("hopperfiltersimplified")) {
                return true;
            }
        }
        if (args.length > 0) {
            String arg = args[0];
            args = java.util.Arrays.copyOfRange(args, 1, args.length);
            if (arg.toLowerCase().equals("debug")) {
                return plugin.clSetDebugLevel.onCommand(sender, command, label, args);
            } else if (arg.toLowerCase().equals("allowchestfilters")) {
                return plugin.clAllowChestFilters.onCommand(sender, command, label, args);
            } else if (arg.toLowerCase().equals("debug")) {
                return plugin.clSetDebugLevel.onCommand(sender, command, label, args);
            }
            plugin.sendMessageInfo(sender, "invalid command: " + arg);
        } else {
            plugin.sendMessageInfo(sender, "no command specified.");
        }
        
        return false;
    }
}