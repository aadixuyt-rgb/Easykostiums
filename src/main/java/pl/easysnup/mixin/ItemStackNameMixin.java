package pl.easysnup.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.easysnup.Costume;
import pl.easysnup.Wardrobe;

@Mixin(ItemStack.class)
public abstract class ItemStackNameMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void easysnup$name(CallbackInfoReturnable<Text> cir) {
        ItemStack self = (ItemStack) (Object) this;
        Costume c = Wardrobe.costumeOf(self);
        if (c != null) {
            cir.setReturnValue(c.name());
            return;
        }
        Text armor = Wardrobe.armorNameFor(self);   // "Adixu armor" / "Armor snupika"
        if (armor != null) cir.setReturnValue(armor);
    }
}
