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

/**
 * This class listens to and handles the commands:<br/>
 * <br/> 
 *      - hopperfiltersimplified<br/>
 *      - hfs<br/>
 * <br/>
 * Upon a successful match, the routine redirects to the other classes that this shortcut represents.
 * 
 */
public class HfsCommandListener implements CommandExecutor {
    private final HopperFilterSimplified plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin HopperFilterSimplified class so we can point back to the base class at protected functions.
     */
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
            //capture the first argument so we know where to redirect
            String arg = args[0];
            //strip the first argument from the array and pass on the rest
            args = java.util.Arrays.copyOfRange(args, 1, args.length);
            //find the correct command to call
                 if (arg.toLowerCase().equals("debug"))             { return plugin.clSetDebugLevel.onCommand(sender, command, label, args);     } 
            else if (arg.toLowerCase().equals("allowchestfilters")) { return plugin.clAllowChestFilters.onCommand(sender, command, label, args); } 
            else if (arg.toLowerCase().equals("clearcache"))        { return plugin.clClearCache.onCommand(sender, command, label, args);        }
        }
        
        //command was not found, so return false so the usage from the plugin.yml is displayed.
        return false;
    }
}
