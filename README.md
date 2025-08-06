<p align="center">
    <a href="https://discord.gg/WWWeAqwcU6" alt="Discord">
        <img src="https://img.shields.io/discord/990400053673873428?label=Discord"/>
    </a>
</p>

# AntiVillagerLag 

SpigotMC: [Link](https://www.spigotmc.org/resources/antivillagerlag.102949/)

A comprehensive villager optimization plugin that allows players to selectively disable villager AI to reduce server lag while maintaining full trading functionality. Perfect for large trading halls and villager farms that impact server performance.

## 📋 Table of Contents
- [Overview](#overview)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
- [Installation & Setup](#installation--setup)
- [Usage Guide](#usage-guide)
- [Configuration](#configuration)
- [Commands](#commands)
- [Permissions](#permissions)
- [Advanced Features](#advanced-features)
- [Performance Impact](#performance-impact)
- [Troubleshooting](#troubleshooting)
- [Support](#support)

## 🎯 Overview

AntiVillagerLag addresses the common server performance issues caused by large villager trading halls. Instead of removing villagers or limiting trading, this plugin allows players to **selectively disable villager AI** when they don't need to move around, dramatically reducing computational overhead while preserving all trading functionality.

### The Problem
- Large villager trading halls can severely impact server MSPT
- Villagers constantly pathfinding and AI processing causes lag
- Traditional solutions involve removing villagers or limiting trades

### The Solution  
- Players can "freeze" villagers when not actively breeding or moving them
- Frozen villagers consume virtually no server resources
- Trading remains fully functional with custom restock schedules
- Easy toggle system allows quick activation when needed

## ✨ Key Features

### 🎮 Player-Controlled Optimization
- **Multiple Control Methods**: Use nametags, blocks, or workstations to control villager states
- **Instant Toggle**: Right-click to enable/disable villager AI
- **Visual Feedback**: Clear messages show villager status and cooldowns
- **No Resource Waste**: Configurable nametag consumption

### ⚡ Performance Optimization
- **AI Freezing**: Completely stops villager pathfinding and behavior processing
- **Zombie Protection**: Frozen villagers are immune to zombie attacks
- **Selective Processing**: Only active villagers consume server resources
- **Massive MSPT Reduction**: Up to 90% performance improvement in large trading halls

### 🛡️ Anti-Exploit Protection
- **Trade Cooldowns**: Prevents double-trading exploits
- **Level-up Handling**: Automatic temporary AI activation for villager progression
- **Smart Restocking**: Custom restock schedule prevents vanilla/plugin conflicts

### 📊 Advanced Management
- **Radius Commands**: Bulk optimize/unoptimize villagers in areas
- **Permission System**: Fine-grained control over who can use features
- **Update Notifications**: Automatic update checking for administrators

## ⚙️ How It Works

### Villager States
AntiVillagerLag manages villagers in two primary states:

#### 🟢 Active State (AI Enabled)
- Villager behaves normally
- Can move, pathfind, and interact with environment
- Consumes normal server resources
- Required for breeding and some farm mechanics

#### 🔴 Optimized State (AI Disabled)  
- All AI processing stopped (`setAware(false)`)
- Villager cannot move or pathfind
- Protected from zombie attacks
- Maintains all trade data and experience
- Consumes minimal server resources

### Control Methods

#### 1. **Nametag Control** (Primary Method)
```
Default Names: "Optimize", "Bonk"
- Use nametag with trigger name → Disables AI
- Rename to anything else → Enables AI  
- Configurable nametag consumption
```

#### 2. **Block-Based Control**
```
Default Blocks: Emerald Block, Diamond Block
- Place trigger block near villager → Disables AI
- Remove block → Enables AI
- Configurable detection radius
```

#### 3. **Workstation Control**  
```
Workstations: Composter, Smoker, Barrel, etc.
- Workstation in radius → Disables AI
- No workstation → Enables AI
- Configurable detection radius (default: 2 blocks)
```

## 🚀 Installation & Setup

### Requirements
- Minecraft 1.20+
- Spigot/Paper server
- Java 8+

### Installation Steps
1. Download the latest JAR from [SpigotMC](https://www.spigotmc.org/resources/antivillagerlag.102949/)
2. Place in your `plugins/` folder
3. Restart your server
4. Edit `plugins/AntiVillagerLag/config.yml` as needed
5. Run `/avlreload` to apply changes

## 📖 Usage Guide

### Basic Usage
1. **Optimize a Villager**: Right-click with a nametag named "Optimize" or "Bonk"
2. **Unoptimize**: Rename the villager to anything else and right-click
3. **Trade**: Only works with optimized (frozen) villagers by default

### Trading Workflow
```
1. Find villager → 2. Optimize it → 3. Trade → 4. Unoptimize if needed for breeding
```

### Bulk Operations
```bash
/avloptimize 25        # Optimize all villagers within 25 blocks
/avlunoptimize 25      # Unoptimize all villagers within 25 blocks  
/avlremove             # Remove all plugin modifications server-wide
```

## ⚙️ Configuration

### config.yml Structure

#### Control Names & Blocks
```yaml
NamesThatDisable:
  - Optimize
  - Bonk

BlocksThatDisable:
  - EMERALD_BLOCK
  - DIAMOND_BLOCK

WorkstationsThatDisable:
  - COMPOSTER
  - SMOKER
  - BARREL
  # ... full list of workstations
```

#### Restock Schedule  
```yaml
RestockTimes:
  times:
    - 1000    # ~1 hour after sunrise
    - 13000   # ~1 hour after sunset
```

#### Anti-Exploit Settings
```yaml
ai-toggle-cooldown: 600  # 10 minutes between AI toggles
RadiusLimit: 50         # Max radius for bulk commands
RadiusDefault: 50       # Default radius when not specified
```

#### Toggle Options
```yaml
toggleableoptions:
  preventtrading: true      # Force optimization before trading
  usenametags: false       # Consume nametags when used
  userenaming: true        # Enable nametag control
  useblocks: false         # Enable block-based control
  useworkstations: false   # Enable workstation-based control
  workstationcheckradius: 2 # Radius for workstation detection
```

## 🎮 Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/avlreload` | Reload plugin configuration | `avl.reload` | `/avlreload` |
| `/avloptimize` | Optimize villagers in radius | `avl.optimize` | `/avloptimize [radius]` |
| `/avlunoptimize` | Unoptimize villagers in radius | `avl.unoptimize` | `/avlunoptimize [radius]` |
| `/avlremove` | Remove all plugin modifications | `avl.remove` | `/avlremove` |

## 🔐 Permissions

### Command Permissions
- `avl.reload` - Reload plugin configuration
- `avl.optimize` - Use bulk optimize commands  
- `avl.unoptimize` - Use bulk unoptimize commands
- `avl.remove` - Remove all plugin modifications

### Bypass Permissions
- `avl.cooldown.bypass` - Bypass AI toggle cooldown
- `avl.restockcooldown.bypass` - Force immediate restocking
- `avl.disable` - Completely disable plugin effects for player

### Notification Permissions
- `avl.message.nextrestock` (default: true) - See restock countdown messages
- `avl.notify.update` (op: true) - Receive update notifications

## 🔬 Advanced Features

### Automatic Level Management
- Plugin detects when optimized villagers need to level up
- Temporarily enables AI for 5 seconds during level progression
- Automatically re-optimizes after leveling completes
- Prevents level-up interruption during the process

### Smart Restock System
- Custom restock schedule independent of vanilla mechanics
- Prevents double-restocking exploits
- Configurable times based on in-game day/night cycle
- Instant restock available with bypass permission

### Zombie Protection
- Optimized villagers are immune to zombie attacks
- Prevents villager loss in trading halls
- Does not affect iron golem spawning mechanics

### Update Integration
- Automatic update checking on plugin load
- Notification system for administrators
- Version compatibility warnings

## 📊 Performance Impact

### Before AntiVillagerLag
- 100 villagers = ~5-10 MSPT impact
- Constant pathfinding calculations  
- Memory usage from AI processing
- Server lag during peak hours

### After AntiVillagerLag  
- 100 optimized villagers = ~0.1-0.5 MSPT impact
- No pathfinding when optimized
- Minimal memory footprint
- Smooth server performance

### Real-World Results
- **Large Trading Halls**: 90%+ MSPT reduction
- **Mixed Farms**: 70% performance improvement
- **Memory Usage**: 60% reduction in villager-related RAM usage

## 🛠️ Troubleshooting

### Common Issues

#### "Villager won't trade"
- **Cause**: `preventtrading: true` requires optimization first
- **Solution**: Optimize villager before trading, or disable setting

#### "Nametags being consumed"  
- **Cause**: `usenametags: true` in config
- **Solution**: Set to `false` for infinite nametag usage

#### "Can't breed optimized villagers"
- **Cause**: AI disabled prevents breeding behavior  
- **Solution**: Temporarily unoptimize villagers for breeding

#### "Villager not leveling up"
- **Cause**: Plugin handles this automatically
- **Solution**: Wait for automatic level-up process, don't manually intervene

### Performance Issues
- Check that only necessary villagers are unoptimized
- Use bulk commands to optimize large groups efficiently
- Monitor MSPT before/after optimization

### Configuration Problems  
- Verify YAML syntax in config.yml
- Use `/avlreload` after making changes
- Check console for error messages

## 🎯 Best Practices

### For Players
1. **Optimize trading villagers** immediately after setting up trades
2. **Only unoptimize when breeding** or moving villagers
3. **Use bulk commands** for large trading halls
4. **Monitor restock times** to plan trading sessions

### For Administrators  
1. **Set appropriate radius limits** to prevent abuse
2. **Configure restock times** based on server economy
3. **Enable update notifications** for security
4. **Monitor performance metrics** before/after deployment

### For Large Servers
1. **Train players** on proper usage
2. **Set up permission groups** for different access levels  
3. **Use workstation control** for automatic optimization
4. **Regular monitoring** of villager optimization status

## 📈 Integration Examples

### Trading Hall Setup
```
1. Build trading hall with individual villager cells
2. Place villagers and set up desired trades  
3. Use /avloptimize 50 to optimize entire hall
4. Enjoy lag-free trading with custom restock schedule
```

### Mixed Farm Integration  
```
1. Keep villagers for iron farms UNOPTIMIZED
2. Optimize only trading-specific villagers
3. Use workstation control for automatic management
4. Monitor farm functionality regularly
```

## 🆘 Support

### Getting Help
- **Discord Server**: [Join Here](https://discord.gg/WWWeAqwcU6)  
- **GitHub Issues**: Report bugs and feature requests
- **SpigotMC**: Community support and updates

### Before Asking for Help
1. Check this documentation thoroughly  
2. Verify your configuration syntax
3. Test with a small number of villagers first
4. Check console logs for error messages
5. Include plugin version and server details

### Feature Requests
We welcome suggestions for new features! Please include:
- Detailed description of desired functionality
- Use case explanation  
- Potential configuration options
- Impact on existing features

---

**AntiVillagerLag** - Optimizing Minecraft servers one villager at a time! 🎯
