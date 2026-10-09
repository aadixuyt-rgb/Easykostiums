package pl.easysnup.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pl.easysnup.Wardrobe;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    /** Niewidzialny item = pusta ręka w pierwszej osobie. */
    @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack easysnup$hiddenAsEmpty(ItemStack stack) {
        return Wardrobe.isHidden(stack) ? ItemStack.EMPTY : stack;
    }
}
