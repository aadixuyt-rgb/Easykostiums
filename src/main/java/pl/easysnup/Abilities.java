package pl.easysnup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.sound.SoundEvents;

/** Umiejętność kostiumu Snupa: 3x shift => pomniejszenie do 0,65. */
public final class Abilities {
    private static final int SHIFTS = 3;
    private static final long TIMEOUT_MS = 3000;
    private static final long DURATION_MS = 10000;
    private static final long COOLDOWN_MS = 120000;
    private static final double SCALE = 0.65;

    private static final Bar BAR = new Bar("snup_ability");

    private static int shifts = 0;
    private static boolean sneakHeld = false;
    private static long lastShift = 0;
    private static boolean active = false;
    private static long activeUntil = 0;
    private static long readyAt = 0;
    private static double previousScale = 1.0;

    private Abilities() {}

    public static void reset() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (active && p != null) applyScale(p, previousScale);
        active = false;
        shifts = 0;
        BAR.hide();
    }

    private static void applyScale(ClientPlayerEntity p, double v) {
        EntityAttributeInstance inst = p.getAttributeInstance(EntityAttributes.SCALE);
        if (inst != null) inst.setBaseValue(v);
    }

    private static String seconds(long ms) {
        return String.valueOf((long) Math.ceil(ms / 1000.0));
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || Wardrobe.worn != Costume.SNUP) {
            if (active || shifts != 0) reset();
            return;
        }
        long now = System.currentTimeMillis();

        if (active) {
            long left = activeUntil - now;
            if (left <= 0) {
                end(p, now);
            } else {
                BAR.show("&7Kostium &bsnupa&7 jest aktywny jeszcze przez &f" + seconds(left) + "&7!",
                        left / (float) DURATION_MS, BossBar.Color.BLUE);
            }
        } else if (now < readyAt) {
            long left = readyAt - now;
            BAR.show("&7Kostium &bsnupa&7 wykorzystany! Odnawianie umiejętności... &8(" + seconds(left) + "s)",
                    1f - left / (float) COOLDOWN_MS, BossBar.Color.WHITE);
        } else {
            BAR.hide();
        }

        boolean down = mc.options.sneakKey.isPressed();
        if (down && !sneakHeld) countShift(p, now);
        sneakHeld = down;
        if (shifts > 0 && now - lastShift > TIMEOUT_MS) shifts = 0;
    }

    private static void countShift(ClientPlayerEntity p, long now) {
        if (active || now < readyAt) return;
        if (shifts == 0 || now - lastShift > TIMEOUT_MS) shifts = 0;
        shifts++;
        lastShift = now;
        if (shifts >= SHIFTS) {
            shifts = 0;
            activate(p, now);
        } else {
            int missing = SHIFTS - shifts;
            Titles.actionbar("&5Shiftnij jeszcze &f" + missing + " &5" + (missing == 1 ? "raz" : "razy")
                    + ", aby uruchomić &dpomniejszenie&5! &7(3s)");
        }
    }

    private static void activate(ClientPlayerEntity p, long now) {
        EntityAttributeInstance inst = p.getAttributeInstance(EntityAttributes.SCALE);
        previousScale = inst == null ? 1.0 : inst.getBaseValue();
        applyScale(p, SCALE);
        active = true;
        activeUntil = now + DURATION_MS;
        Titles.show("&b&lKOSTIUM SNUPA", "&7Aktywowano &dpomniejszenie&7!");
        Titles.sound(SoundEvents.BLOCK_BEACON_ACTIVATE, 1f, 1.4f);
        Fx.activation(Costume.SNUP);
    }

    private static void end(ClientPlayerEntity p, long now) {
        applyScale(p, previousScale);
        active = false;
        readyAt = now + COOLDOWN_MS;
        Titles.chat("&fPomniejszenie się skończyło!");
    }
}
