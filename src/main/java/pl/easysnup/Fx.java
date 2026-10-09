package pl.easysnup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Particles: wokół gracza po aktywacji kostiumu + eksplozja po zabójstwie (Adix). */
public final class Fx {
    private static final class Ring { Costume c; int age; }

    private static final class Blast {
        final Vec3d origin; final Vec3d[] dirs; final int rgb; int age;
        Blast(Vec3d origin, Vec3d[] dirs, int rgb) { this.origin = origin; this.dirs = dirs; this.rgb = rgb; }
    }

    private static final List<Ring> RINGS = new ArrayList<>();
    private static final List<Blast> BLASTS = new ArrayList<>();
    private static final int RING_TICKS = 24;
    /** 1,5 sekundy = 30 ticków */
    public static final int BLAST_TICKS = 30;

    private Fx() {}

    /** Kółka cząsteczek dookoła gracza w kolorze kostiumu. */
    public static void activation(Costume c) {
        Ring r = new Ring();
        r.c = c;
        RINGS.add(r);
    }

    /** Czerwone cząsteczki lecące we wszystkie strony z miejsca zabójstwa, przez 1,5 s. */
    public static void blast(Vec3d origin, int rgb) {
        java.util.Random rnd = new java.util.Random();
        Vec3d[] dirs = new Vec3d[48];
        for (int i = 0; i < dirs.length; i++) {
            double x, y, z, len;
            do {
                x = rnd.nextDouble() * 2 - 1;
                y = rnd.nextDouble() * 2 - 1;
                z = rnd.nextDouble() * 2 - 1;
                len = Math.sqrt(x * x + y * y + z * z);
            } while (len < 0.1 || len > 1.0);
            dirs[i] = new Vec3d(x / len, y / len, z / len);
        }
        BLASTS.add(new Blast(origin, dirs, rgb));
    }

    public static void clear() {
        RINGS.clear();
        BLASTS.clear();
    }

    private static DustParticleEffect dust(int rgb, float size) {
        return new DustParticleEffect(rgb, size);
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld w = mc.world;
        ClientPlayerEntity p = mc.player;
        if (w == null || p == null) {
            clear();
            return;
        }
        Iterator<Ring> it = RINGS.iterator();
        while (it.hasNext()) {
            Ring r = it.next();
            double prog = r.age / (double) RING_TICKS;
            int n = 14;
            for (int k = 0; k < n; k++) {
                double a = r.age * 0.45 + k * (Math.PI * 2 / n);
                double rad = 0.85 + Math.sin(prog * Math.PI) * 0.35;
                double y = p.getY() + 0.1 + prog * (p.getHeight() + 0.3);
                w.addParticle(dust(r.c.particleRgb, 1.1f),
                        p.getX() + Math.cos(a) * rad, y, p.getZ() + Math.sin(a) * rad, 0, 0, 0);
            }
            if (++r.age >= RING_TICKS) it.remove();
        }
        Iterator<Blast> bi = BLASTS.iterator();
        while (bi.hasNext()) {
            Blast b = bi.next();
            double t = b.age;
            // droga rośnie szybko, potem zwalnia
            double dist = 0.55 * t - 0.006 * t * t;
            for (Vec3d d : b.dirs) {
                Vec3d pos = b.origin.add(d.multiply(dist));
                w.addParticle(dust(b.rgb, 1.5f), pos.x, pos.y, pos.z, 0, 0, 0);
            }
            if (++b.age >= BLAST_TICKS) bi.remove();
        }
    }
}
