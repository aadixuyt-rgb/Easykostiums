package pl.easysnup;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Fake serca kostiumu Snupa (tylko HUD).
 *  - dodatkowe serca regenerują się TYLKO gdy masz pełne prawdziwe życie,
 *  - gdy tracisz prawdziwe (czerwone) życie, fake serca znikają od razu (wszystkie),
 *  - złote serca (koks / refil = absorpcja) nie ruszają fake serc - liczy się tylko utrata czerwonych.
 */
public final class Hearts {
    /** co ile ticków wraca 1 HP (pół serca) fake życia */
    private static final int REGEN_TICKS = 50;

    private static float fake = 0f;
    private static float prevHealth = -1f;
    private static int regen = 0;

    private Hearts() {}

    public static void reset() {
        fake = 0f;
        prevHealth = -1f;
        regen = 0;
    }

    public static void onWear(ClientPlayerEntity p) {
        Costume c = Wardrobe.worn;
        if (c == null || c.bonusHealth <= 0 || p == null) { reset(); return; }
        prevHealth = p.getHealth();
        fake = p.getHealth() >= p.getMaxHealth() - 0.01f ? c.bonusHealth : 0f;
        regen = 0;
    }

    public static float bonusMax() {
        Costume c = Wardrobe.worn;
        return c == null ? 0f : c.bonusHealth;
    }

    public static float shownFake() {
        return Wardrobe.worn == null ? 0f : fake;
    }

    public static void tick(ClientPlayerEntity p) {
        Costume c = Wardrobe.worn;
        if (c == null || c.bonusHealth <= 0) {
            fake = 0f;
            prevHealth = -1f;
            return;
        }
        float h = p.getHealth();
        if (prevHealth >= 0f && h < prevHealth - 0.01f && fake > 0f) {
            fake = 0f;          // stracone czerwone serce => znikają fake serca
            regen = 0;
        }
        if (h >= p.getMaxHealth() - 0.01f) {
            if (fake < c.bonusHealth && ++regen >= REGEN_TICKS) {
                fake = Math.min(c.bonusHealth, fake + 1f);
                regen = 0;
            }
        } else {
            regen = 0;
        }
        prevHealth = h;
    }

    public static float shownHealth(PlayerEntity p) {
        return p.getHealth() + shownFake();
    }
}
