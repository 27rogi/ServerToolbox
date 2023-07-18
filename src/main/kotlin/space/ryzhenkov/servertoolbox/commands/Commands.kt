package space.ryzhenkov.servertoolbox.commands

import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.chat.*
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.entity.EntityRenameCommand
import space.ryzhenkov.servertoolbox.commands.items.ColorCommand
import space.ryzhenkov.servertoolbox.commands.items.LoreCommand
import space.ryzhenkov.servertoolbox.commands.items.RenameCommand
import space.ryzhenkov.servertoolbox.commands.items.TrimCommand
import space.ryzhenkov.servertoolbox.commands.mod.STCommand
import space.ryzhenkov.servertoolbox.commands.player.*
import space.ryzhenkov.servertoolbox.commands.player.gui.*
import space.ryzhenkov.servertoolbox.commands.teleportation.SpawnCommand
import space.ryzhenkov.servertoolbox.commands.world.SetSpawnCommand
import space.ryzhenkov.servertoolbox.commands.world.SignCommand
import space.ryzhenkov.servertoolbox.config.GeneralConfig
import space.ryzhenkov.servertoolbox.utils.Registrable


object Commands : Registrable("commands") {
    enum class Categories(val items: ArrayList<CommandBase>) {
        MOD(
            arrayListOf(
                STCommand("st", arrayOf("servertoolbox", "server-toolbox"), 0)
            )
        ),
        CHAT(
            arrayListOf(
                BroadcasterCommand("broadcaster", null, PermissionLevel.BAN_RIGHTS.level),
                ClearChatCommand("clearchat", null, PermissionLevel.BAN_RIGHTS.level),
                StaffCommand("staff", arrayOf("admins"), PermissionLevel.COMMAND_RIGHTS.level)
            )
        ),
        PLAYER(
            arrayListOf(
                GameModeCommand("gm", null, PermissionLevel.COMMAND_RIGHTS.level),
                HealCommand("heal", null, PermissionLevel.BAN_RIGHTS.level),
                FeedCommand("feed", arrayOf("saturate", "hunger"), PermissionLevel.BAN_RIGHTS.level),
                FlyCommand("fly", null, PermissionLevel.BAN_RIGHTS.level),
            )
        ),
        CUSTOMIZATION(
            arrayListOf(
                LoreCommand("lore", arrayOf("setlore"), PermissionLevel.COMMAND_RIGHTS.level),
                RenameCommand("rename", arrayOf("setname"), PermissionLevel.COMMAND_RIGHTS.level),
                ColorCommand("color", arrayOf("setcolor"), PermissionLevel.COMMAND_RIGHTS.level),
                TrimCommand("trim", arrayOf("settrim"), PermissionLevel.COMMAND_RIGHTS.level),
            )
        ),
        GUI(
            arrayListOf(
                WorkbenchCommand("workbench", arrayOf("crafting", "craft"), PermissionLevel.BAN_RIGHTS.level),
                // Unstable behaviour, deletes items when exited + recipes don't work, requires future investigation
                // FurnaceCommand("furnace", null, PermissionLevel.BAN_RIGHTS.level),
                AnvilCommand("anvil", null, PermissionLevel.BAN_RIGHTS.level),
                EnchantmentTableCommand("enchantment", arrayOf("enchantment-table"), PermissionLevel.BAN_RIGHTS.level),
                EnderChestCommand("enderchest", arrayOf("echest"), PermissionLevel.BAN_RIGHTS.level),
                GrindstoneCommand("grindstone", null, PermissionLevel.BAN_RIGHTS.level),
                StonecutterCommand("stonecutter", arrayOf("cutter"), PermissionLevel.BAN_RIGHTS.level),
            )
        ),
        TELEPORTATION(arrayListOf()),
        WORLD(
            arrayListOf(
                EntityRenameCommand("entityrename", arrayOf("setentityname"), PermissionLevel.COMMAND_RIGHTS.level),
                SignCommand("sign", arrayOf("signedit"), PermissionLevel.COMMAND_RIGHTS.level),
            )
        )
    }

    override fun onRegister(): Boolean {
        if (GeneralConfig.Modules.spawn) {
            Categories.TELEPORTATION.items.add(
                SpawnCommand(
                    "spawn",
                    arrayOf("s"),
                    PermissionLevel.COMMAND_RIGHTS.level
                )
            )
            Categories.TELEPORTATION.items.add(
                SetSpawnCommand(
                    "setspawn",
                    arrayOf("spawnset"),
                    PermissionLevel.BAN_RIGHTS.level
                )
            )
        }
        if (GeneralConfig.Modules.kits) {
            Categories.PLAYER.items.add(KitCommand("kit", null, PermissionLevel.COMMAND_RIGHTS.level))
            Categories.PLAYER.items.add(KitsCommand("kits", null, PermissionLevel.COMMAND_RIGHTS.level))
        }
        if (GeneralConfig.Modules.motd) Categories.CHAT.items.add(
            MotdCommand(
                "motd",
                null,
                PermissionLevel.COMMAND_RIGHTS.level
            )
        )
        if (GeneralConfig.Modules.rules) Categories.CHAT.items.add(
            RulesCommand(
                "rules",
                null,
                PermissionLevel.COMMAND_RIGHTS.level
            )
        )

        for (category in Categories.entries) {
            for (command in category.items) {
                command.register()
            }
        }
        return true
    }
}