package pl.easysnup.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.easysnup.Looks;

@Mixin(PlayerEntity.class)
public abstract class LivingEntityMixin {
    /**
     * Celujemy w PlayerEntity, bo w 1.21.4 LivingEntity#getEquippedStack jest abstrakcyjne (brak kodu do wstrzyknięcia).
     * Tylko w trakcie renderowania Twojego modelu (F5): zamiast prawdziwej zbroi - strój kostiumu,
     * zamiast hidden itemu - pusta ręka (dzięki temu ręka nie "trzyma" niewidzialnego itemu).
     */
    @Inject(method = "getEquippedStack", at = @At("HEAD"), cancellable = true)
    private void easysnup$look(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (!Looks.rendering) return;
        if ((Object) this != MinecraftClient.getInstance().player) return;
        ItemStack look = Looks.forRender(slot);
        if (look != null) cir.setReturnValue(look);
    }
}
