package pl.easysnup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Cała logika kostiumów po stronie klienta.
 *
 * Są dwa rodzaje "kostiumowych" przedmiotów:
 *  - SWAP: jeden konkretny slot z prawdziwym przedmiotem (np. 1 płot dębowy w ręce) wygląda jak kostium.
 *    Inne takie same przedmioty w ekwipunku NIE są zmieniane (śledzimy slot, nie typ przedmiotu).
 *  - FAKE: "duch" - przedmiot istnieje tylko u Ciebie w pustym slocie, nic nie podmienia i nie da się go wyrzucić.
 *
 * Gdy kostium jest założony, przedmiot jest niewidzialny (ręka wygląda na pustą), nie da się go wyrzucić,
 * ale można go przesuwać.
 */
public final class Wardrobe {

    public static final class Tracked {
        public final Costume costume;
        public final boolean fake;
        public int slot;           // indeks w PlayerInventory (0-8 pasek, 9-35 główny, 36-39 zbroja, 40 off-hand)
        public final Item item;
        public boolean carried;    // trzymany na kursorze
        public boolean moving;     // shift-click, czekamy na nowy slot
        public int wait;
        public int dropping;       // ticki od naciśnięcia Q
        public ItemStack ghost;    // tylko FAKE

        Tracked(Costume costume, boolean fake, int slot, Item item) {
            this.costume = costume;
            this.fake = fake;
            this.slot = slot;
            this.item = item;
        }
    }

    private static final class Ground {
        final Costume costume;
        Ground(Costume c) { this.costume = c; }
    }

    private static final class PendingDrop {
        final Costume costume; final Item item; int ttl = 40;
        PendingDrop(Costume c, Item i) { costume = c; item = i; }
    }

    private static final class PendingPickup {
        final Costume costume; final Item item; int wait = 3;
        PendingPickup(Costume c, Item i) { costume = c; item = i; }
    }

    public static Costume worn;
    public static final List<Tracked> TRACKED = new ArrayList<>();

    private static final Map<Integer, Ground> GROUND = new HashMap<>();
    private static final Map<ItemStack, Costume> GROUND_STACKS = new IdentityHashMap<>();
    private static final List<PendingDrop> DROPS = new ArrayList<>();
    private static final List<PendingPickup> PICKUPS = new ArrayList<>();

    private static int ticks = 0;
    private static boolean restored = false;
    private static int lastNag = -100;
    private static int lastSwitch = -100;

    private Wardrobe() {}

    // ------------------------------------------------------------------ pomocnicze

    private static MinecraftClient mc() { return MinecraftClient.getInstance(); }

    private static ItemStack stackAt(int slot) {
        ClientPlayerEntity p = mc().player;
        if (p == null || slot < 0 || slot > 40) return ItemStack.EMPTY;
        return p.getInventory().getStack(slot);
    }

    public static Tracked at(int slot) {
        for (Tracked t : TRACKED) if (!t.carried && t.slot == slot) return t;
        return null;
    }

    private static Tracked carriedTracked() {
        for (Tracked t : TRACKED) if (t.carried) return t;
        return null;
    }

    private static Tracked find(Costume c, boolean fake) {
        for (Tracked t : TRACKED) if (t.costume == c && t.fake == fake) return t;
        return null;
    }

    public static boolean isBlocked(Tracked t) {
        return t.fake || worn == t.costume;
    }

    /** Który kostium reprezentuje ten konkretny ItemStack (tylko ten, nie inne takie same). */
    public static Tracked trackedOf(ItemStack s) {
        if (s == null || s.isEmpty() || TRACKED.isEmpty()) return null;
        ClientPlayerEntity p = mc().player;
        if (p == null) return null;
        for (Tracked t : TRACKED) {
            if (t.carried) {
                ItemStack cur = p.currentScreenHandler.getCursorStack();
                if (cur == s && s.isOf(t.item)) return t;
            } else if (stackAt(t.slot) == s) {
                return t;
            }
        }
        return null;
    }

    public static Costume costumeOf(ItemStack s) {
        Tracked t = trackedOf(s);
        if (t != null) return t.costume;
        if (s != null && !GROUND_STACKS.isEmpty()) return GROUND_STACKS.get(s);
        return null;
    }

    public static Costume groundCostume(ItemStack s) {
        return GROUND_STACKS.isEmpty() ? null : GROUND_STACKS.get(s);
    }

    /** Niewidzialny, gdy jego kostium jest założony. */
    public static boolean isHidden(ItemStack s) {
        Tracked t = trackedOf(s);
        return t != null && worn == t.costume;
    }

    public static boolean mainHandHidden() {
        ClientPlayerEntity p = mc().player;
        if (p == null) return false;
        Tracked t = at(p.getInventory().selectedSlot);
        return t != null && worn == t.costume;
    }

    public static boolean offHandHidden() {
        Tracked t = at(40);
        return t != null && worn == t.costume;
    }

    /** Nazwa dla prawdziwej zbroi na Tobie, gdy masz założony kostium ("Adixu armor"). */
    public static Text armorNameFor(ItemStack s) {
        Costume w = worn;
        ClientPlayerEntity p = mc().player;
        if (w == null || p == null || s == null || s.isEmpty()) return null;
        for (int i = 36; i <= 39; i++) {
            if (p.getInventory().getStack(i) == s) return Legacy.parse(w.armorName);
        }
        return null;
    }

    // ------------------------------------------------------------------ zakładanie

    public static void wear(Costume c, boolean viaItem) {
        ClientPlayerEntity p = mc().player;
        if (p == null || worn == c) return;
        if (worn != null) takeOff(false);
        worn = c;
        Hearts.onWear(p);
        Fx.activation(c);
        Titles.sound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.7f, 1.3f);
        Titles.chat("&7Założyłeś &7Kostium " + c.nameLegacy + "&7.");
        save();
    }

    public static void takeOff(boolean message) {
        if (worn == null) return;
        Costume old = worn;
        worn = null;
        Abilities.reset();
        Hearts.reset();
        if (message) {
            Titles.sound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), 1f, 1f);
            Titles.chat("&7Zdjąłeś &7Kostium " + old.nameLegacy + "&7.");
        }
        save();
    }

    public static void toggle(Costume c) {
        if (worn == c) takeOff(true); else wear(c, false);
    }

    // ------------------------------------------------------------------ swap / fake

    /** Zamienia TYLKO przedmiot trzymany w ręce (ten jeden slot). */
    public static boolean swapHeld(Costume c) {
        ClientPlayerEntity p = mc().player;
        if (p == null) { Titles.chat("&cMusisz być w grze."); return false; }
        int slot = p.getInventory().selectedSlot;
        ItemStack held = p.getInventory().getStack(slot);
        if (held.isEmpty()) { Titles.chat("&cWeź coś do ręki."); return false; }
        Tracked here = at(slot);
        if (here != null && here.fake) { Titles.chat("&cWeź do ręki prawdziwy przedmiot."); return false; }

        Tracked old = find(c, false);
        if (old != null) TRACKED.remove(old);        // przenosimy kostium na nowy przedmiot
        if (here != null) TRACKED.remove(here);      // ten slot należał do innego kostiumu
        TRACKED.add(new Tracked(c, false, slot, held.getItem()));
        save();
        Titles.chat("&7Gotowe — ten jeden przedmiot wygląda jak Kostium " + c.nameLegacy
                + "&7. &8(widzisz to tylko Ty, kliknij go prawym, aby założyć)");
        return true;
    }

    /** Dodaje fake item (niczego nie podmienia, nie da się go wyrzucić). */
    public static boolean giveFake(Costume c) {
        ClientPlayerEntity p = mc().player;
        if (p == null) { Titles.chat("&cMusisz być w grze."); return false; }
        if (find(c, true) != null) { Titles.chat("&cMasz już ten kostium w ekwipunku."); return false; }
        int slot = freeSlot();
        if (slot < 0) { Titles.chat("&cBrak miejsca w ekwipunku!"); return false; }
        ItemStack ghost = Looks.fakeItem(c).copy();
        p.getInventory().setStack(slot, ghost);
        Tracked t = new Tracked(c, true, slot, ghost.getItem());
        t.ghost = ghost;
        TRACKED.add(t);
        save();
        Titles.chat("&7Otrzymałeś &7Kostium " + c.nameLegacy + "&7. &8(widzisz go tylko Ty)");
        return true;
    }

    private static int freeSlot() {
        for (int i = 9; i <= 35; i++) if (stackAt(i).isEmpty() && at(i) == null) return i;
        for (int i = 0; i <= 8; i++) if (stackAt(i).isEmpty() && at(i) == null) return i;
        return -1;
    }

    /** Usuwa wszystko: zamienione wracają do normy, nadane znikają, kostium jest zdejmowany. */
    public static void clearAll() {
        ClientPlayerEntity p = mc().player;
        takeOff(false);
        for (Tracked t : TRACKED) {
            if (t.fake && p != null && t.ghost != null && stackAt(t.slot) == t.ghost) {
                p.getInventory().setStack(t.slot, ItemStack.EMPTY);
            }
        }
        TRACKED.clear();
        GROUND.clear();
        GROUND_STACKS.clear();
        DROPS.clear();
        PICKUPS.clear();
        Fx.clear();
        save();
        Titles.chat("&7Usunięto wszystkie kostiumy. Przedmioty wyglądają znowu normalnie.");
    }

    // ------------------------------------------------------------------ prawy klik

    /** @return true = anuluj normalne użycie przedmiotu */
    public static boolean onRightClick(boolean offHand) {
        ClientPlayerEntity p = mc().player;
        if (p == null) return false;
        Tracked t = at(offHand ? 40 : p.getInventory().selectedSlot);
        if (t == null) return false;
        if (worn == t.costume) {
            if (ticks - lastNag > 20) {
                lastNag = ticks;
                Titles.chat("&cMasz już ten kostium założony. Zdejmij go w &f/szafka&c.");
            }
        } else {
            wear(t.costume, true);   // zakładanie innego kostiumu automatycznie ściąga poprzedni
        }
        return true;
    }

    // ------------------------------------------------------------------ kliknięcia w ekwipunku

    /** @return true = anuluj kliknięcie */
    public static boolean onClick(int slotId, int button, SlotActionType type, PlayerEntity player) {
        if (TRACKED.isEmpty()) return false;
        ScreenHandler h = player.currentScreenHandler;
        Slot s = (slotId >= 0 && slotId < h.slots.size()) ? h.slots.get(slotId) : null;
        int idx = (s != null && s.inventory instanceof PlayerInventory) ? s.getIndex() : -1;
        Tracked here = idx >= 0 ? at(idx) : null;

        switch (type) {
            case PICKUP: {
                if (slotId == -999) {                      // klik poza oknem = wyrzucenie z kursora
                    Tracked c = carriedTracked();
                    if (c != null) {
                        if (isBlocked(c)) return true;
                        onDropped(c);
                    }
                    return false;
                }
                if (here != null && here.fake) return true;
                Tracked c = carriedTracked();
                if (c != null && idx >= 0) {
                    c.slot = idx;
                    c.carried = false;
                    if (here != null && here != c) here.carried = true;   // zamiana z tym, co leżało w slocie
                } else if (c != null) {
                    TRACKED.remove(c);                     // wylądował w skrzyni itp. - przestajemy śledzić
                } else if (here != null) {
                    here.carried = true;
                }
                return false;
            }
            case QUICK_MOVE: {
                if (here != null) {
                    if (here.fake) return true;
                    here.moving = true;
                    here.wait = 0;
                }
                return false;
            }
            case SWAP: {
                int other = button == 40 ? 40 : button;     // 0-8 pasek, 40 off-hand
                Tracked tOther = (other >= 0 && other <= 8) || other == 40 ? at(other) : null;
                if ((here != null && here.fake) || (tOther != null && tOther.fake)) return true;
                if (here != null && idx >= 0 && (other <= 8 || other == 40)) here.slot = other;
                if (tOther != null) {
                    if (idx >= 0) tOther.slot = idx; else TRACKED.remove(tOther);
                }
                return false;
            }
            case THROW: {
                if (here != null) {
                    if (isBlocked(here)) return true;     // kostium założony => nie wyrzucisz
                    onDropped(here);
                }
                return false;
            }
            default: {
                return here != null && here.fake;
            }
        }
    }

    private static void onDropped(Tracked t) {
        TRACKED.remove(t);
        DROPS.add(new PendingDrop(t.costume, t.item));
        save();
    }

    /** Wywoływane na początku ticka: zjada klawisz Q, gdy kostium blokuje wyrzucanie. */
    public static void eatDropKey(MinecraftClient c) {
        ClientPlayerEntity p = c.player;
        if (p == null || c.currentScreen != null) return;
        Tracked t = at(p.getInventory().selectedSlot);
        if (t == null) return;
        if (isBlocked(t)) {
            while (c.options.dropKey.wasPressed()) { /* zjedz */ }
        } else if (c.options.dropKey.isPressed()) {
            t.dropping = 5;
        }
    }

    // ------------------------------------------------------------------ tick

    public static void tick(MinecraftClient c) {
        ticks++;
        ClientPlayerEntity p = c.player;
        if (p == null || c.world == null) {
            restored = false;
            return;
        }
        if (!restored && p.age >= 30) {
            restored = true;
            restore(p);
        }

        // 1. utrzymywanie fake itemów + kontrola śledzonych slotów
        Iterator<Tracked> it = TRACKED.iterator();
        List<Tracked> toRelocate = new ArrayList<>();
        while (it.hasNext()) {
            Tracked t = it.next();
            if (t.dropping > 0) t.dropping--;
            if (t.fake) {
                if (stackAt(t.slot) != t.ghost) {
                    if (stackAt(t.slot).isEmpty()) {
                        p.getInventory().setStack(t.slot, t.ghost);
                    } else {
                        int ns = freeSlot();
                        if (ns >= 0) { t.slot = ns; p.getInventory().setStack(ns, t.ghost); }
                    }
                }
                continue;
            }
            if (t.carried) {
                if (p.currentScreenHandler.getCursorStack().isEmpty()) {
                    t.carried = false;
                    toRelocate.add(t);
                }
                continue;
            }
            if (t.moving) {
                if (++t.wait >= 3) { t.moving = false; toRelocate.add(t); }
                continue;
            }
            ItemStack st = stackAt(t.slot);
            if (st.isEmpty() || !st.isOf(t.item)) {
                if (t.dropping > 0 && st.isEmpty()) {
                    it.remove();
                    DROPS.add(new PendingDrop(t.costume, t.item));
                } else {
                    toRelocate.add(t);
                }
            }
        }
        for (Tracked t : toRelocate) relocate(t);

        // 2. przedmioty leżące na ziemi
        tickGround(c, p);

        // 3. trzymasz drugi kostium => zakładasz go, a pierwszy się ściąga
        if (worn != null && ticks - lastSwitch > 5) {
            Tracked held = at(p.getInventory().selectedSlot);
            if (held != null && held.costume != worn) {
                lastSwitch = ticks;
                wear(held.costume, true);
            }
        }
    }

    private static void relocate(Tracked t) {
        int old = t.slot;
        for (int i = 0; i <= 40; i++) {
            if (i == old && stackAt(i).isOf(t.item)) { return; }
        }
        for (int i = 0; i <= 40; i++) {
            if (i >= 36 && i <= 39) continue;
            if (at(i) != null) continue;
            if (stackAt(i).isOf(t.item)) { t.slot = i; return; }
        }
        TRACKED.remove(t);
        save();
    }

    private static void tickGround(MinecraftClient c, ClientPlayerEntity p) {
        // nowo wyrzucone
        Iterator<PendingDrop> di = DROPS.iterator();
        while (di.hasNext()) {
            PendingDrop d = di.next();
            List<ItemEntity> list = c.world.getEntitiesByClass(ItemEntity.class, p.getBoundingBox().expand(6),
                    e -> e.getStack().isOf(d.item) && !GROUND.containsKey(e.getId()) && e.age < 60);
            if (!list.isEmpty()) {
                ItemEntity best = list.get(0);
                for (ItemEntity e : list) if (e.age < best.age) best = e;
                GROUND.put(best.getId(), new Ground(d.costume));
                di.remove();
            } else if (--d.ttl <= 0) {
                di.remove();
            }
        }
        // odświeżenie mapy stacków + zebrane przedmioty
        GROUND_STACKS.clear();
        Iterator<Map.Entry<Integer, Ground>> gi = GROUND.entrySet().iterator();
        while (gi.hasNext()) {
            Map.Entry<Integer, Ground> e = gi.next();
            net.minecraft.entity.Entity ent = c.world.getEntityById(e.getKey());
            if (ent instanceof ItemEntity ie && !ie.isRemoved()) {
                GROUND_STACKS.put(ie.getStack(), e.getValue().costume);
            } else {
                if (ent == null || ent.isRemoved()) {
                    // jeśli stałeś blisko, to prawdopodobnie go podniosłeś
                    if (ent != null && ent.squaredDistanceTo(p) < 25) {
                        Item it = ent instanceof ItemEntity x ? x.getStack().getItem() : null;
                        if (it != null) PICKUPS.add(new PendingPickup(e.getValue().costume, it));
                    }
                }
                gi.remove();
            }
        }
        // podniesione => wracają jako kostium
        Iterator<PendingPickup> pi = PICKUPS.iterator();
        while (pi.hasNext()) {
            PendingPickup pk = pi.next();
            if (--pk.wait > 0) continue;
            pi.remove();
            if (find(pk.costume, false) != null) continue;
            for (int i = 0; i <= 35; i++) {
                if (at(i) == null && stackAt(i).isOf(pk.item)) {
                    TRACKED.add(new Tracked(pk.costume, false, i, pk.item));
                    save();
                    break;
                }
            }
        }
    }

    // ------------------------------------------------------------------ zapis / odczyt

    private static void restore(ClientPlayerEntity p) {
        TRACKED.clear();
        for (Config.Saved sv : Config.d.saved) {
            Costume c = Costume.byId(sv.costume);
            Identifier id = Identifier.tryParse(sv.item == null ? "" : sv.item);
            if (c == null || id == null) continue;
            Item item = Registries.ITEM.get(id);
            if (sv.fake) {
                if (find(c, true) != null) continue;
                int slot = (sv.slot >= 0 && sv.slot <= 35 && stackAt(sv.slot).isEmpty() && at(sv.slot) == null) ? sv.slot : freeSlot();
                if (slot < 0) continue;
                ItemStack ghost = Looks.fakeItem(c).copy();
                p.getInventory().setStack(slot, ghost);
                Tracked t = new Tracked(c, true, slot, ghost.getItem());
                t.ghost = ghost;
                TRACKED.add(t);
            } else if (stackAt(sv.slot).isOf(item) && find(c, false) == null && at(sv.slot) == null) {
                TRACKED.add(new Tracked(c, false, sv.slot, item));
            }
        }
        Costume w = Costume.byId(Config.d.worn);
        if (w != null) {
            worn = w;
            Hearts.onWear(p);
        }
    }

    public static void save() {
        Config.d.saved.clear();
        for (Tracked t : TRACKED) {
            if (t.carried) continue;
            Config.Saved sv = new Config.Saved();
            sv.costume = t.costume.id;
            sv.fake = t.fake;
            sv.slot = t.slot;
            sv.item = Registries.ITEM.getId(t.item).toString();
            Config.d.saved.add(sv);
        }
        Config.d.worn = worn == null ? null : worn.id;
        Config.save();
    }
}
