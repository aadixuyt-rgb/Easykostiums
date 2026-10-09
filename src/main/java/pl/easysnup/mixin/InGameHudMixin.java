package pl.easysnup.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.easysnup.Hearts;
import pl.easysnup.Wardrobe;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    private ItemStack currentStack;

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void easysnup$noHiddenName(DrawContext context, CallbackInfo ci) {
        if (Wardrobe.isHidden(this.currentStack)) ci.cancel();
    }

    /** Fake serca: pokazujemy życie + fake. */
    @Redirect(method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getHealth()F"))
    private float easysnup$health(PlayerEntity player) {
        return Hearts.shownHealth(player);
    }

    /** Fake serca: dodatkowe pojemniki serc. */
    @Redirect(method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D"))
    private double easysnup$maxHealth(PlayerEntity player, RegistryEntry<EntityAttribute> attribute) {
        double v = player.getAttributeValue(attribute);
        if (attribute == EntityAttributes.MAX_HEALTH) v += Hearts.bonusMax();
        return v;
    }
}
