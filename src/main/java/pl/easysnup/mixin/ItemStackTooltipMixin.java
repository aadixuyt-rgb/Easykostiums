package pl.easysnup.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.easysnup.Costume;
import pl.easysnup.Wardrobe;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Inject(method = "getTooltip", at = @At("HEAD"), cancellable = true)
    private void easysnup$tooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type,
                                  CallbackInfoReturnable<List<Text>> cir) {
        ItemStack self = (ItemStack) (Object) this;
        Costume c = Wardrobe.costumeOf(self);
        if (c != null) {
            cir.setReturnValue(c.fullTooltip(Wardrobe.worn == c));
            return;
        }
        Text armor = Wardrobe.armorNameFor(self);
        if (armor != null) {
            List<Text> l = new ArrayList<>();
            l.add(armor);
            cir.setReturnValue(l);
        }
    }
}
