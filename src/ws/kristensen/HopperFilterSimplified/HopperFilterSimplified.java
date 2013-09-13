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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HopperFilterSimplified extends JavaPlugin {
    private final HfsBlockListener blockListener = new HfsBlockListener(this);

    private final String ConfigurationSectionName_Basic = "Basic";
    private Integer debugLevel = 0;
    private boolean allowChestFilters = false;

    private final HashMap<Location, String> knownHoppersCache = new HashMap<Location, String>();
    private final String knownHoppersCacheDelimiter = "~";

    private   final HfsCommandListener                  cl                  = new HfsCommandListener(this);
    protected final HfsCommandListenerAllowChestFilters clAllowChestFilters = new HfsCommandListenerAllowChestFilters(this);
    protected final HfsCommandListenerClearCache        clClearCache        = new HfsCommandListenerClearCache(this);
    protected final HfsCommandListenerSetDebugLevel     clSetDebugLevel     = new HfsCommandListenerSetDebugLevel(this);

    /**
     * Called when this plugin is enabled
     */
    @Override
    public void onEnable() {
        // Save a copy of the default config.yml if one is not there
        this.saveDefaultConfig();

        //Get the basic settings
        settings_Basic_Read();
        
        //Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(blockListener, this);
        
        //Register our commands to which we listen
        getCommand("hopperFiltersimplified"                 ).setExecutor(cl);
        getCommand("hopperfiltersimplifiedallowchestfilters").setExecutor(clAllowChestFilters);
        getCommand("hopperfiltersimplifiedclearcache"       ).setExecutor(clClearCache       );
        getCommand("hopperfiltersimplifieddebug"            ).setExecutor(clSetDebugLevel    );
        
    }

    /**
     * Called when this plugin is disabled 
     */
    @Override
    public void onDisable() {
        //code to shut down the plugin safely.
        
        settings_Basic_Write();
        this.saveConfig();
    }

    /**
     * Read the configuration file: Basic section
     */
    private void settings_Basic_Read() {
        debugLevel = this.getConfig().getConfigurationSection(ConfigurationSectionName_Basic).getInt("DebugLevel");
        allowChestFilters = this.getConfig().getConfigurationSection(ConfigurationSectionName_Basic).getBoolean("AllowChestFilters");
    }
    /**
     * Write the configuration file: Basic section
     */
    private void settings_Basic_Write() {
        this.getConfig().getConfigurationSection(ConfigurationSectionName_Basic).set("DebugLevel", debugLevel);
        this.getConfig().getConfigurationSection(ConfigurationSectionName_Basic).set("AllowChestFilters", allowChestFilters);
    }

    /**
     * Get the cache for a specific hopper.
     * 
     * @param hopperBlock Block indicating the hopper to return the cache for.
     * @return String representing the cache for the hopper
     */
    protected String knownHoppersCache_Get(final Block hopperBlock) {
        Location hopperLocation = hopperBlock.getLocation();
        //try to get the cache
        if (knownHoppersCache.containsKey(hopperLocation)) {
            //debug if requested
            if (debugLevel > 1) getLogger().info("  Returning cache for hopper (" + hopperBlock.getLocation().toString() + ")  cache: " + knownHoppersCache.get(hopperLocation));

            //return the cache
            return knownHoppersCache.get(hopperLocation);
        } else {
            //debug if requested
            if (debugLevel > 1) getLogger().info("  Starting new cache for hopper (" + hopperBlock.getLocation().toString() + ")");

            //make a new cache for the location
            String cache = ReturnAttachedAllowedItemsAsString (hopperBlock);
            
            //Save the string for the location in the cache
            knownHoppersCache_Set(hopperBlock.getLocation(), cache);
            
            //debug if requested
            if (debugLevel > 1) getLogger().info("  Completed new cache: " + cache);

            //return the cache
            return cache;
        }
    }
    /**
     * Sets the cache for a specific hopper.
     * 
     * @param hopperLocation Location of the hopper itself.
     * @param cache String of the cache itself.
     * @return boolean true of set was allowed, false otherwise.
     */
    protected boolean knownHoppersCache_Set(final Location hopperLocation, final String cache) {
        //Add the new cache item replacing the value if it already exists
        knownHoppersCache.put(hopperLocation, cache);
        return true;
    }
    /**
     * Clear the cache for all hoppers.
     * 
     * @return boolean true if cache was cleared, false if cache was already empty.
     */    
    protected boolean knownHoppersCache_Clear() {
        if (debugLevel > 1) getLogger().info("      Clearing cache for all hoppers.");

        if (knownHoppersCache.isEmpty()) {
            //The cache already empty
            return false;            
        } else {
            //there is at least one entry, clear it.
            knownHoppersCache.clear();
            return true;
        }
    }
    /**
     * Clear the cache at a specific Location and the 4 spots at the same height around the location. (N/S/E/W)
     * 
     * @param hopperLocation Location of the center area to have the cache cleared.
     * @return boolean true if at least one cache was cleared, false if no caches existed for location.
     */
    protected boolean knownHoppersCache_ClearAroundLocation(final Location hopperLocation) {
        boolean returnValue = false;
        Block block = hopperLocation.getBlock();

        if (knownHoppersCache_ClearLocation(block.getLocation()))                       { returnValue = true; }
        if (knownHoppersCache_ClearLocation(block.getRelative( 1, 0, 0).getLocation())) { returnValue = true; }
        if (knownHoppersCache_ClearLocation(block.getRelative( 0, 0, 1).getLocation())) { returnValue = true; }
        if (knownHoppersCache_ClearLocation(block.getRelative(-1, 0, 0).getLocation())) { returnValue = true; }
        if (knownHoppersCache_ClearLocation(block.getRelative( 0, 0,-1).getLocation())) { returnValue = true; }
        
        return returnValue;
    }
    /**
     * Clear the cache for a specific hopper.
     * 
     * @param hopperLocation Location of the cache to be cleared.
     * @return boolean true if cache cleared, false if no cache existed.
     */
    protected boolean knownHoppersCache_ClearLocation(final Location hopperLocation) {
        if (knownHoppersCache_isFilter(hopperLocation)) {
            if (debugLevel > 1) getLogger().info("      Cleared cache for hopper (" + hopperLocation.toString() + ")");
        } else {
            return false;
        }
        return true;
    }
    /**
     * Indicate if there is a hopper cache at a specific Location or the 4 spots at the same height around the location. (N/S/E/W)
     * 
     * @param hopperLocation Location of the center area to have the cache checked.
     * @return boolean true if at least one cache exists, false if no caches exist for location or surrounding blocks.
     */
    protected boolean knownHoppersCache_isFilterAroundLocation(final Location hopperLocation) {
        Block block = hopperLocation.getBlock();

        if (knownHoppersCache_isFilter(block.getLocation()))                       { return true; }
        if (knownHoppersCache_isFilter(block.getRelative( 1, 0, 0).getLocation())) { return true; }
        if (knownHoppersCache_isFilter(block.getRelative( 0, 0, 1).getLocation())) { return true; }
        if (knownHoppersCache_isFilter(block.getRelative(-1, 0, 0).getLocation())) { return true; }
        if (knownHoppersCache_isFilter(block.getRelative( 0, 0,-1).getLocation())) { return true; }
        //none found, so return false
        return false;
    }
    /**
     * Indicate if the supplied location is a hopper with a filter.
     * 
     * @param hopperLocation Location of the hopper to check
     * @return boolean true if the filter exists, false otherwise
     */
    protected boolean knownHoppersCache_isFilter(final Location hopperLocation) {
        return (knownHoppersCache.containsKey(hopperLocation));
    }

    /**
     * Gets the current debug level.
     * 
     * @return Integer debugLevel value
     */
    protected Integer debugLevel_get() {
        return debugLevel;
    }
    /**
     * Sets the debug level to a valid value.
     * 
     * @param proposedDebugLevel String value between 0 and 4
     * @return boolean true if successful, false otherwise
     */
    protected boolean debugLevel_set(final String proposedLevel) {
        if (proposedLevel.equals("0")) {debugLevel = 0; return true; }
        if (proposedLevel.equals("1")) {debugLevel = 1; return true; }
        if (proposedLevel.equals("2")) {debugLevel = 2; return true; }
        if (proposedLevel.equals("3")) {debugLevel = 3; return true; }
        if (proposedLevel.equals("4")) {debugLevel = 4; return true; }

        return false;
    }

    /**
     * Sets the allowChestFilters flag
     * 
     * @param propsedFlag String value of true or false
     * @return boolean true if successful, false otherwise
     */
    protected boolean allowChestFilters_Set(String propsedFlag){
        if (propsedFlag.toLowerCase().equals("false") || propsedFlag.toLowerCase().equals("true")) {
            allowChestFilters = (propsedFlag.toLowerCase().equals("true"));
            return true;
        }
        return false;
    }
    
    /**
     * Return the newly constructed cache string representation of allowed blocks for a hopper.
     * 
     * @param hopperBlock Block of the hopper in question.
     * @return String representing the cache of allowed items
     */
    private String ReturnAttachedAllowedItemsAsString (Block hopperBlock) {
        String returnValue = knownHoppersCacheDelimiter;
        HashMap<ItemStack, String> itemStackCache = new HashMap<ItemStack, String>();
        
        //get the items from itemFrame objects attached to this hopper and adjacent chests that the hopper does not feed into.
        itemStackCache.putAll(ReturnAttachedAllowedItemsFromItemFrames(hopperBlock));
        if (allowChestFilters)
            itemStackCache.putAll(ReturnAttachedAllowedItemsFromChests(hopperBlock));
        
        //combine all item stacks into one cache string
        Set<ItemStack> keys = itemStackCache.keySet();
        for (ItemStack key : keys) {
            returnValue += itemStackCache.get(key);
        }

        //make sure the returned cache is empty string and not one delimiter if no allowed items exist
        if (returnValue.equals(knownHoppersCacheDelimiter))
            returnValue = "";

        //return the cache list as a string
        return returnValue;
    }
    /**
     * Find all the item frames within a 1 block radius of the hopper and return the item inside the frame.
     * 
     * @param hopperBlock Block of the hopper in question.
     * @return HashMap<ItemStack, String> of the items in any attached frames.
     */
    private HashMap<ItemStack, String> ReturnAttachedAllowedItemsFromItemFrames(Block hopperBlock) {
        if (debugLevel > 2) getLogger().info("    Finding itemFrames attached to hopper.");
        
        //declare return container
        HashMap<ItemStack, String> itemStackCache = new HashMap<ItemStack, String>();
        ItemStack itemStack = null;
        //Look right next to the hopper and avoid the frames on another hopper  
        double radius = 0.45;
        
        //create an entity in the exact center of the block so we can find the surrounding attached entities.
        Entity entity = hopperBlock.getWorld().spawnEntity(hopperBlock.getLocation().add(0.5,0.5,0.5), EntityType.EXPERIENCE_ORB);
        //get all other entities in a radius around this entity not looking up or down. 
        List<Entity> entities = entity.getNearbyEntities(radius, 0, radius);
        //remove the temp entity
        entity.remove();
        
        //loop through all the adjacent entities next the hopper
        for (Entity nearbyEntity : entities) {
            //make sure they are an ItemFrame
            if (nearbyEntity instanceof ItemFrame) {
                //deal with itemFrame
                if (debugLevel > 3) getLogger().info("      Found attached item frame (" + ((ItemFrame)nearbyEntity).getLocation().toString() + ")");
                //get the possible itemStack from the itemFrame
                itemStack = ((ItemFrame) nearbyEntity).getItem();
                //make sure there was something there
                if (itemStack != null) {
                    //deal with itemStack
                    if (debugLevel > 3) getLogger().info("        Found item in frame (" + GetItemInformationForInventory(itemStack, false) + ")");

                    //make sure the item amount is not taken into account
                    itemStack.setAmount(1);                    

                    //Store it
                    itemStackCache.put(itemStack, GetItemInformationForInventory(itemStack, true));
                }
            }  
        }
        
        return itemStackCache;
    }
    /**
     * Find all the chests next to the hopper (not diagonal or above or below) that the hopper does not feed into and return the items inside the chest.
     * 
     * @param hopperBlock the hopper block in question.
     * @return HashMap<ItemStack, String> of the items in any adjacent chests not being fed by the hopper.
     */
    @SuppressWarnings("deprecation")
    private HashMap<ItemStack, String> ReturnAttachedAllowedItemsFromChests(Block hopperBlock) {
        if (debugLevel > 2) getLogger().info("    Finding chests attached to hopper.");
        
        //declare return container
        HashMap<ItemStack, String> itemStackCache = new HashMap<ItemStack, String>();
        //save the direction the hopper outputs
        byte facing = hopperBlock.getData(); // 0=Facing down, 1= unattached to any container, 2=Facing North, 3=Facing South, 4=Facing West, 5=Facing East
        
        Block target = null;
        ArrayList<Chest> list = new ArrayList<Chest>();

        if (debugLevel > 3) getLogger().info("      hopper facing: (" + String.valueOf(((int)facing)) + ")");

        target = hopperBlock.getRelative(BlockFace.NORTH);
        if (target.getState() instanceof Chest && facing != 0x2) {
            //take a snapshot of the chest so we can use it later
            list.add((Chest)target.getState());
        }
        
        target = hopperBlock.getRelative(BlockFace.EAST);
        if (target.getState() instanceof Chest && facing != 0x5) {
            //take a snapshot of the chest so we can use it later
            list.add((Chest)target.getState());
        }
        
        target = hopperBlock.getRelative(BlockFace.SOUTH);
        if (target.getState() instanceof Chest && facing != 0x3) {
            //take a snapshot of the chest so we can use it later
            list.add((Chest)target.getState());
        }
        
        target = hopperBlock.getRelative(BlockFace.WEST);
        if (target.getState() instanceof Chest && facing != 0x4) {
            //take a snapshot of the chest so we can use it later
            list.add((Chest)target.getState());
        }

        //loop through all the adjacent chests not being fed into by the hopper
        for (Chest chest : list) {
            //deal with chest
            if (debugLevel > 3) getLogger().info("      Found attached chest (" + chest.getLocation().toString() + ")");
            
            //get the stacks out of the chest
            ItemStack[] tempStacks = chest.getBlockInventory().getContents();
            
            //loop through all the item stacks in the chest
            for (ItemStack itemStack : tempStacks) {
                if (itemStack != null) {
                    //deal with the itemStack
                    if (debugLevel > 3) getLogger().info("        Found item in chest (" + GetItemInformationForInventory(itemStack, false) + ")");

                    //make sure the item amount is not taken into account
                    itemStack.setAmount(1);
                    
                    //Store it
                    itemStackCache.put(itemStack, GetItemInformationForInventory(itemStack, true));
                }
            }
        }
        
        return itemStackCache;
    }

    /**
     * Extract string data about the item so we can use it to compare or stuff into cache for later use
     * 
     * @param item ItemStack that needs the information extracted 
     * @param forBuildingCache boolean indicating if the returned string is part of a cache being built up
     * @return String containing the extracted information from the ItemStack
     */
    protected String GetItemInformationForInventory(ItemStack item, boolean forBuildingCache) {
        String returnValue = "";
        try {
            //build up the information
            returnValue = (item.getData().toString() + "," + item.getItemMeta().toString() + knownHoppersCacheDelimiter);
            //if the information is for the cache, do not prepend it with the delimiter as there may be more items for the cache
            if (!forBuildingCache) {
                //since this is not for the cache, prepend it with the delimiter so we can get an exact match
                returnValue = knownHoppersCacheDelimiter + returnValue;
            }
            //return the string
            return returnValue;
        } catch (Exception e) {
            if (!forBuildingCache)
                //not for the cache, so include the delimiters
                return knownHoppersCacheDelimiter + knownHoppersCacheDelimiter;
            else 
                //do not include the delimiters for the cache
                return "";
        }
    }

    /**
     * Indicate if the ChestLocation is next to a Hopper that it can be a filter for
     * 
     * @param chestLocation Location of the Chest in question
     * @return boolean true if there is at least one hopper it can be a filter for
     */
    protected boolean isChestNextToHopper(final Location chestLocation) {
        //get the Block of the Chest
        Block chestBlock = chestLocation.getBlock();

        //check each direction and see if there is a valid hopper for the chest to be a filter for
        if (isChestValidForHopperFilter(chestBlock.getRelative(BlockFace.NORTH), BlockFace.SOUTH)) { return true;}
        if (isChestValidForHopperFilter(chestBlock.getRelative(BlockFace.EAST),  BlockFace.WEST))  { return true;}
        if (isChestValidForHopperFilter(chestBlock.getRelative(BlockFace.SOUTH), BlockFace.NORTH)) { return true;}
        if (isChestValidForHopperFilter(chestBlock.getRelative(BlockFace.WEST),  BlockFace.EAST))  { return true;}

        //The given chest is not next to a valid hopper
        return false;
    }
    /**
     * Indicate if the testBlock is a valid hopper for the chest to be a filter of
     * 
     * @param testBlock Block to determine if it is a hopper
     * @param chestDirection BlockFace direction to the chest from the testBlock
     * @return boolean true if the testBlock is a valid hopper for the chest to be a filter for
     */    
    @SuppressWarnings("deprecation")
    private boolean isChestValidForHopperFilter(Block testBlock, BlockFace chestDirection) {
        if (testBlock.getType().equals(Material.HOPPER)) {
            byte testData = testBlock.getData();
            if (testData == 0x2 && chestDirection == BlockFace.NORTH) { return false; } //Hopper is feeding into the chest
            if (testData == 0x5 && chestDirection == BlockFace.EAST)  { return false; } //Hopper is feeding into the chest
            if (testData == 0x3 && chestDirection == BlockFace.SOUTH) { return false; } //Hopper is feeding into the chest
            if (testData == 0x4 && chestDirection == BlockFace.WEST)  { return false; } //Hopper is feeding into the chest
            return true; // testBlock is a hopper and not feeding into the chest 
        }
        return false; //this testBlock is not a hopper
    }

    protected boolean isHopperPartOfFilter(final Location hopperLocation) {
        //try the least expensive test first then progress to the more expensive tests
        if (knownHoppersCache_isFilter(hopperLocation)) { return true; }
        if (isHopperNextToChest(hopperLocation))        { return true; }
        if (isHopperNextToItemFrame(hopperLocation))    { return true; }
        return false;
    }
    /**
     * Indicate if the hopper is next to a chest that can be part of its filter
     * 
     * @param hopperLocation Location of the hopper in question
     * @return boolean true if there is a chest next to the hopper that can be part of its filter
     */
    @SuppressWarnings("deprecation")
    protected boolean isHopperNextToChest(final Location hopperLocation) {
        //get the block of the Hopper
        Block hopperBlock = hopperLocation.getBlock();
        byte directionData = hopperBlock.getData();
    
        //Check each direction and see if there is a valid chest to be part of a filter.
        if (directionData != 0x2 &&
            (hopperBlock.getRelative(BlockFace.NORTH).getType().equals(Material.CHEST) ||
             hopperBlock.getRelative(BlockFace.NORTH).getType().equals(Material.TRAPPED_CHEST))
           ) { return true; }
        if (directionData != 0x5 && 
            (hopperBlock.getRelative(BlockFace.EAST).getType().equals(Material.CHEST) ||
             hopperBlock.getRelative(BlockFace.EAST).getType().equals(Material.TRAPPED_CHEST))
           ) { return true; }
        if (directionData != 0x3 && 
            (hopperBlock.getRelative(BlockFace.SOUTH).getType().equals(Material.CHEST) ||
             hopperBlock.getRelative(BlockFace.SOUTH).getType().equals(Material.TRAPPED_CHEST))
           ) { return true; }
        if (directionData != 0x4 &&  
            (hopperBlock.getRelative(BlockFace.WEST).getType().equals(Material.CHEST) ||
             hopperBlock.getRelative(BlockFace.WEST).getType().equals(Material.TRAPPED_CHEST))
           ) { return true; }

        //The given hopper has no chests next to it
        return false;
    }
    /**
     * Indicate if the hopper is next to an itemFrame that can be part of its filter
     * 
     * @param hopperLocation Location of the hopper in question
     * @return boolean true if there is an itemFrame next to the hopper that can be part of its filter
     */
    protected boolean isHopperNextToItemFrame(final Location hopperLocation) {
        //Look right next to the hopper and avoid the frames on another hopper  
        double radius = 0.45;
        
        //create an entity in the exact center of the block so we can find the surrounding attached entities.
        Entity entity = hopperLocation.getBlock().getWorld().spawnEntity(hopperLocation.add(0.5,0.5,0.5), EntityType.EXPERIENCE_ORB);        
        //get all other entities in a radius around this entity not looking up or down. 
        List<Entity> entities = entity.getNearbyEntities(radius, 0, radius);
        //remove the temp entity
        entity.remove();

        //loop through all the adjacent entities next the hopper
        for (Entity nearbyEntity : entities) {
            if (nearbyEntity instanceof ItemFrame) { return true; }  
        }
        return false;
    }

    /**
     * Sends a message of type info either to the console or the player, depending on the sender variable
     * 
     * @param sender CommandSender is either the player, or null for console.
     * @param msg String message to send to the player/console.
     */
    protected void sendMessageInfo(CommandSender sender, String msg) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(msg);
        } else {
            this.getLogger().info(msg);
        }
    }
    /**
     * Sends a message of type warning either to the console or the player, depending on the sender variable
     * 
     * @param sender CommandSender is either the player, or null for console.
     * @param msg String message to send to the player/console.
     */
    protected void sendMessageWarning(CommandSender sender, String msg) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(msg);
        } else {
            this.getLogger().warning(msg);
        }
    }
}
