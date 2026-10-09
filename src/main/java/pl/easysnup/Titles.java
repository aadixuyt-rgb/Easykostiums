package pl.easysnup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

public final class Titles {
    private Titles() {}

    public static void show(String title, String subtitle) {
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        hud.setTitleTicks(10, 50, 10);
        hud.setSubtitle(Legacy.parse(subtitle));
        hud.setTitle(Legacy.parse(title));
    }

    public static void sound(SoundEvent event, float volume, float pitch) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getSoundManager().play(PositionedSoundInstance.master(event, pitch, volume));
    }

    public static void chat(String legacy) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Legacy.parse(legacy));
    }

    public static void actionbar(String legacy) {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Legacy.parse(legacy), false);
    }
}
