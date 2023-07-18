package space.ryzhenkov.servertoolbox.mixins;

import eu.pb4.placeholders.api.TextParserUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.silkmc.silk.commands.PermissionLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import space.ryzhenkov.servertoolbox.config.GeneralConfig;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow
    private String newItemName;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Unique
    private Text getNewName() {
        if (!GeneralConfig.Modules.INSTANCE.getColorfulAnvils()) return Text.literal(this.newItemName);
        if (!Permissions.check(this.player, "server-toolbox.colorful.anvils", PermissionLevel.COMMAND_RIGHTS.getLevel())) {
            return Text.literal(this.newItemName);
        }
        return TextParserUtils.formatTextSafe(this.newItemName).copy().styled(style -> style.withItalic(false));
    }

    @ModifyArg(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCustomName(Lnet/minecraft/text/Text;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    public Text updateResult(Text name) {
        return getNewName();
    }

    @ModifyArg(method = "setNewItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCustomName(Lnet/minecraft/text/Text;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    public Text newItemName(Text name) {
        return getNewName();
    }
}
