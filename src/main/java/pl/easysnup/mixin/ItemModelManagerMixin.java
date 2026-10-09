package pl.easysnup.mixin;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pl.easysnup.Costume;
import pl.easysnup.Looks;
import pl.easysnup.Wardrobe;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    private static final String UPDATE =
            "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V";

    /**
     * Konkretny ItemStack (ten jeden, nie wszystkie takie same) wygląda jak kostium,
     * a gdy kostium jest założony - jest niewidzialny.
     */
    @ModifyVariable(method = UPDATE, at = @At("HEAD"), argsOnly = true)
    private ItemStack easysnup$look(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;
        if (Wardrobe.isHidden(stack)) return ItemStack.EMPTY;
        Costume c = Wardrobe.costumeOf(stack);
        return c == null ? stack : Looks.fakeItem(c);
    }
}
