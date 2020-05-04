# brigadier-bukkit

This module adapts **brigadier** to bukkit.  
Either take this default plugin or build it yourself with the same classes to have it in your own system.  

## Installation

First install this project into your local repository.  
Add this dependency to your pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>net.volix.bookshelf</groupId>
        <artifactId>brigadier-bukkit</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

And then simply build the plugin and put it into your `/plugins` folder.

## Features

We just created a custom parameter `PlayerParamType`, so that `ParameterSet#get(index, Player.class)` works.  
In this param type we parse either the name or the uuid to a player online on the same server.
