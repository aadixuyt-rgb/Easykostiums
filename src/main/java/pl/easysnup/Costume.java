package pl.easysnup;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Definicje kostiumów. Żeby dodać kolejny, dopisz tu wpis + tekstury w assets/easysnup. */
public enum Costume {
    SNUP("snup", "&bsnupa", 0x3AB3DA, 0x33AAFF, "&bArmor snupika", 4.0f, List.of(
            "&7Został on dodany przez &csnupxb&7.",
            "",
            "&7Dzięki temu przedmiotowi otrzymasz",
            "&funikatowy &7wygląd oraz &fepickie &7bonusy",
            "",
            "&7Lista bonusów:",
            " &8» &c5% &7do każdego perka",
            " &8» &4+2 &7serc",
            " &8» &cPomniejszenie&7, które pomniejsza",
            "&7Cię do rozmiaru &e0,65 &8(3x shift)")),
    ADIX("adix", "&cadixa", 0x1A1A1A, 0xFF2020, "&cAdixu armor", 0.0f, List.of(
            "&7Kostium adixa stworzony przez &cAdixu_YT&7.",
            "",
            "&7Lista bonusów:",
            " &8» &ckolorowe hitboxy",
            " &8» &cphonk &7po zabiciu jakiegoś gracza"));

    public final String id;
    public final String nameLegacy;
    public final int dye;
    public final int particleRgb;
    public final String armorName;
    public final float bonusHealth;
    private final List<String> lore;

    Costume(String id, String nameLegacy, int dye, int particleRgb, String armorName, float bonusHealth, List<String> lore) {
        this.id = id;
        this.nameLegacy = nameLegacy;
        this.dye = dye;
        this.particleRgb = particleRgb;
        this.armorName = armorName;
        this.bonusHealth = bonusHealth;
        this.lore = lore;
    }

    public Identifier headModel() { return Identifier.of("easysnup", id + "_head"); }

    public Identifier armorAsset() { return Identifier.of("easysnup", id); }

    /** "&7Kostium &bsnupa" */
    public String title() { return "&7Kostium " + nameLegacy; }

    public Text displayName() { return Legacy.parse(title()); }

    public static Costume byId(String id) {
        for (Costume c : values()) if (c.id.equals(id)) return c;
        return null;
    }

    public List<Text> tooltip(boolean worn) {
        List<Text> out = new ArrayList<>();
        for (String l : lore) out.add(Legacy.parse(l));
        out.add(Text.empty());
        if (!Config.d.forever) {
            out.add(Legacy.parse("&7Kostium wygaśnie: &f" + Config.expiryText()));
            out.add(Text.empty());
        }
        out.add(Legacy.parse(worn ? "&cKliknij, aby zdjąć kostium!" : "&aKliknij prawym, aby założyć!"));
        return out;
    }

    /** Pełny tooltip: nazwa + opis. */
    public List<Text> fullTooltip(boolean worn) {
        List<Text> out = new ArrayList<>();
        out.add(displayName());
        out.addAll(tooltip(worn));
        return out;
    }
}
