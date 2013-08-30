Changelog
    Version 0.4 2013-08-30
        - Cleaned up some code to that was causing an error
    Version 0.3 2013-08-28
        - Added ability to find chests and items inside the chest
        - Added additional debug items related to chests.  See v0.2 for complete list
        - Added AllowChestFilters command
        - Added AllowChestFilters entry into plugin.yml
        - Added handler for when itemFrame is added
        - Added handler for when itemFrame is right clicked
        - Added handler for when itemFrame is broken
        - Added handler for when chest is altered.
        - Added handler for when chest is broken
    Version 0.2 2013-08-26
        - Added ability to find itemFrame and items attached to frame
        - Added cache to reduce load on servers.
            Cache builds automatically and on the first push/pull attempt into each destination hopper.
        - Added ClearCache command to reset cache.
        - Added debug capabilities
          level     What is displayed at given level and in what order
            0       no debug reporting
              2       Cache for a given hopper (location) if it exists in cache
              2       Notification that cache entry is being created for a given hopper (location) if it does not exist
               3        Notification that we are looking for item frames
                4         Notification that it found an attached item frame
                4         Information about the item in the itemFrame if one was found
               3        Notification that we are looking for chests
                4         Notification as to what direction the hopper is facing (output direction to avoid looking for a chest there)
                4         Notification that it found a chest N, S, E, or W of the hopper that is not being pushed into by the hopper
                4         Information about each item in the chest if any are found
              2       Displays generated cache
             1      Notification of what item in in what filter (location) is compared against what cache
        - Added Debug command to allow setting of level from 0 to 4
        - Added debug entry into plugin.yml
    Version 0.1 2013-08-29
        Created plugin structure and basic layout