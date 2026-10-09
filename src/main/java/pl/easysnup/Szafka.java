package pl.easysnup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** /szafka - menu kostiumów. */
public final class Szafka {
    private static final int SLOT_MAIN = 13;
    private static final int SLOT_SNUP = 13;
    private static final int SLOT_ADIX = 12;
    private static final int SLOT_BACK = 22;
    /** po 2 szare klaty "Coming soon" z każdej strony (slot 12 zajmuje Adix) */
    private static final int[] COMING_SOON = {11, 14, 15};

    private Szafka() {}

    public static void open() {
        MinecraftClient.getInstance().setScreen(main());
    }

    private static List<Text> lines(String... ls) {
        List<Text> out = new ArrayList<>();
        for (String l : ls) out.add(Legacy.parse(l));
        return out;
    }

    public static SzafkaScreen main() {
        List<SzafkaScreen.Entry> e = new ArrayList<>();
        e.add(new SzafkaScreen.Entry(SLOT_MAIN, new ItemStack(Items.ARMOR_STAND), lines(
                "&6Kostiumy",
                " &8» &7W tym miejscu odnajdziesz pełną listę",
                " &8» &7wszystkich dostępnych &ekostiumów",
                " &8» &7wraz z ich efektami dodatkowymi!",
                "",
                " &8» &aKliknij &2LEWYM&a, aby przejść!"),
                slot -> MinecraftClient.getInstance().setScreen(costumes())));
        return new SzafkaScreen(Legacy.parse("&8Szafka"), 3, e);
    }

    private static ItemStack comingSoon() {
        ItemStack st = new ItemStack(Items.LEATHER_CHESTPLATE);
        st.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x808080, false));
        return st;
    }

    public static SzafkaScreen costumes() {
        List<SzafkaScreen.Entry> e = new ArrayList<>();
        e.add(costumeEntry(SLOT_SNUP, Costume.SNUP));
        e.add(costumeEntry(SLOT_ADIX, Costume.ADIX));
        for (int s : COMING_SOON) {
            e.add(new SzafkaScreen.Entry(s, comingSoon(), lines("&cComing soon"), null));
        }
        e.add(new SzafkaScreen.Entry(SLOT_BACK, new ItemStack(Items.ARROW), lines("&cPowrót"),
                slot -> MinecraftClient.getInstance().setScreen(main())));
        return new SzafkaScreen(Legacy.parse("&8Lista kostiumów"), 3, e);
    }

    private static SzafkaScreen.Entry costumeEntry(int slot, Costume c) {
        return new SzafkaScreen.Entry(slot, Looks.fakeItem(c), c.fullTooltip(Wardrobe.worn == c), s -> {
            Wardrobe.toggle(c);
            MinecraftClient.getInstance().setScreen(costumes());
        });
    }
}
