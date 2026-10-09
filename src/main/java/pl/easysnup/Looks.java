package pl.easysnup;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.EnumMap;
import java.util.Map;

/** Budowanie "wyglądów": fake item, części armoru kostiumu, głowa. */
public final class Looks {
    /** true tylko w trakcie updateRenderState lokalnego gracza (F5 / odbicia). */
    public static boolean rendering = false;

    private static final Map<Costume, ItemStack> ITEMS = new EnumMap<>(Costume.class);
    private static final Map<Costume, ItemStack[]> ARMOR = new EnumMap<>(Costume.class);
    private static final Map<Costume, ItemStack> HEADS = new EnumMap<>(Costume.class);

    private Looks() {}

    /** Skórzana klata w kolorze kostiumu - to widzisz w ekwipunku / na ziemi. */
    public static ItemStack fakeItem(Costume c) {
        return ITEMS.computeIfAbsent(c, k -> {
            ItemStack st = new ItemStack(Items.LEATHER_CHESTPLATE);
            st.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(k.dye, false));
            return st;
        });
    }

    private static ItemStack armorPiece(Costume c, EquipmentSlot slot, Item base, String slotName) {
        ItemStack st = new ItemStack(base);
        String json = "{\"slot\":\"" + slotName + "\",\"asset_id\":\"" + c.armorAsset() + "\"}";
        EquippableComponent comp = EquippableComponent.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .result().orElse(null);
        if (comp != null) st.set(DataComponentTypes.EQUIPPABLE, comp);
        st.remove(DataComponentTypes.DYED_COLOR);
        return st;
    }

    public static ItemStack armor(Costume c, EquipmentSlot slot) {
        ItemStack[] arr = ARMOR.computeIfAbsent(c, k -> new ItemStack[]{
                armorPiece(k, EquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE, "chest"),
                armorPiece(k, EquipmentSlot.LEGS, Items.LEATHER_LEGGINGS, "legs"),
                armorPiece(k, EquipmentSlot.FEET, Items.LEATHER_BOOTS, "feet")});
        return switch (slot) {
            case CHEST -> arr[0];
            case LEGS -> arr[1];
            case FEET -> arr[2];
            default -> ItemStack.EMPTY;
        };
    }

    /** Głowa z własnym modelem (kostka ze skina). */
    public static ItemStack head(Costume c) {
        return HEADS.computeIfAbsent(c, k -> {
            ItemStack st = new ItemStack(Items.STICK);
            st.set(DataComponentTypes.ITEM_MODEL, k.headModel());
            return st;
        });
    }

    /**
     * Podmiana tego, co renderer gracza "widzi" w danym slocie.
     * null = bez zmian (oryginalny item).
     */
    public static ItemStack forRender(EquipmentSlot slot) {
        Costume w = Wardrobe.worn;
        if (w == null) return null;
        switch (slot) {
            case HEAD:
                return head(w);
            case CHEST:
            case LEGS:
            case FEET:
                return armor(w, slot);
            case MAINHAND:
                return Wardrobe.mainHandHidden() ? ItemStack.EMPTY : null;
            case OFFHAND:
                return Wardrobe.offHandHidden() ? ItemStack.EMPTY : null;
            default:
                return null;
        }
    }
}
