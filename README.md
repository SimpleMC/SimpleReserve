# SimpleReserve

Simple, easy-to-use Reserve slot plugin

Tired of not being able to offer VIPs reserved slots or being unable to join your own full server? SimpleReserve
provides an easy way to add that functionality with Bukkit permission support.

## Features

- Simple to use reserve slots plugin with bukkit permission support
- Provides functionality for 2 reserve slot methods:
  - Full method: Users with 'simplereserve.enter.full' may enter past the imposed player limit
  - Kick method: Users with 'simplereserve.enter.kick' may enter a server when full by kicking the first player found that is able to be kicked. Users with the 'simplereserve.kick.prevent' permission are immune to being kicked(Utilize inheritance!)

## Config

The config file for SimpleReserve is very simple and will be auto-generated on first run. The file should contain:

```yaml
reserve:
  method: both
  server-full-message: The server is full!
  full:
    cap: 5
    kick-fallback: false
    over-capacity-message: All reserve slots full!
  kick:
    message: Kicked to make room for reserved user!
```

### Reserve Methods

- `full`: Allow reserves to log on past the server's configured player limit
- `kick`: Attempt to kick a player without kick immunity to make room
- `both`: Both methods of reservation based on Permission
  - **NOTE**: If a player has permission for kick and full, full takes precedence
- `none`: No reservation. Effectively disables mod without needing to remove

## Permissions

Permissions for SimpleReserve are...well...simple. There are only 4 basic permissions to worry about.

- `simplereserve.enter.full`: User may join past server player cap
- `simplereserve.enter.kick`: User may join full server by kicking another player
- `simplereserve.kick.prevent`: User cannot be kicked to make room for a joining player
- `simplereserve.reload`: Gives access to SimpleReserve reload command

...and for convenience
- `simplereserve.*`: All SimpleReserve permissions
- `simplereserve.enter`: Enter full and enter kick permissions

See also [plugin.yml](src/main/resources/plugin.yml).

### Examples

- Lets say you have 4 usergroups. Guests(default), Users, Moderators, and Admins. You want to give Admins and Moderators
  joining full server via "kick" method, but you only want to be able to kick at the expense of guests. Permissions:
  ```yaml
    groups:
        Guests:
            default: true
        Users:
            default: false
            inheritance: Guests
            permissions:
               - 'simplereserve.kick.prevent'
        Moderators:
            default: false
            inheritance: Users
            permissions:
               - 'simplereserve.enter.kick'
        Admins:
            default: false
            inheritance:
            permissions:
               - '*'
  ```
  Note that Users only have the `prevent` permission. Any groups that inherit from Users will also have the same
  permission. Now to ensure we're using the right type of reserve slot, the config.yml would look like:
  ```yaml
  reserve:
    method: kick
    # ...
  ```
- Same situation but we want to be able to join over capacity instead of kicking. We only need to change the
  `Moderators`'s group `simplereserve.enter.kick` permission to `simplereserve.enter.full` and change `method: kick` to
  `method: full` in config.
- We could also allow mods to join using the `kick` method and admins to join using `full`. Set `method: both` in the
  config and give mods `simplereserve.enter.kick` permission. Now, Admins have both `kick` and `full` which will default
  to using `full` when both are available while mods can join using the `kick` permission.
