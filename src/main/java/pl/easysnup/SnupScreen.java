package pl.easysnup;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.time.LocalDateTime;

/** Menu moda (domyślnie klawisz B). */
public class SnupScreen extends Screen {
    private TextFieldWidget dateField;
    private ButtonWidget modeButton, snupButton, adixButton, clearButton, foreverButton;
    private String status = "";
    private int statusColor = 0xFFFFFF;

    public SnupScreen() {
        super(Text.literal("EasySnup"));
    }

    private static boolean give() {
        return Config.d.giveMode;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 82;

        modeButton = addDrawableChild(ButtonWidget.builder(modeLabel(), b -> {
            Config.d.giveMode = !Config.d.giveMode;
            Config.save();
            refresh();
        }).dimensions(cx - 100, y, 200, 20).build());

        snupButton = addDrawableChild(ButtonWidget.builder(mainLabel(Costume.SNUP), b -> run(Costume.SNUP))
                .dimensions(cx - 100, y + 26, 98, 20).build());
        adixButton = addDrawableChild(ButtonWidget.builder(mainLabel(Costume.ADIX), b -> run(Costume.ADIX))
                .dimensions(cx + 2, y + 26, 98, 20).build());

        clearButton = addDrawableChild(ButtonWidget.builder(Legacy.parse("&cUsuń wszystko"), b -> {
            Wardrobe.clearAll();
            ok("Usunięto wszystko (zamienione itemy wróciły do normy, nadane zniknęły).");
        }).dimensions(cx - 100, y + 52, 200, 20).build());

        dateField = new TextFieldWidget(this.textRenderer, cx - 100, y + 92, 200, 20, Text.empty());
        dateField.setMaxLength(32);
        dateField.setText(Config.expiryText());
        dateField.setChangedListener(this::onDate);
        addDrawableChild(dateField);

        foreverButton = addDrawableChild(ButtonWidget.builder(foreverLabel(), b -> {
            Config.d.forever = !Config.d.forever;
            Config.save();
            refresh();
        }).dimensions(cx - 100, y + 118, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), b -> close())
                .dimensions(cx - 100, y + 144, 200, 20).build());
    }

    private Text modeLabel() {
        return Legacy.parse(give()
                ? "&7Tryb: &dFAKE ITEM &8(nadaje, nic nie zamienia)"
                : "&7Tryb: &bZAMIANA &8(trzymany przedmiot)");
    }

    private Text mainLabel(Costume c) {
        return Legacy.parse(give() ? "&fNadaj: " + c.nameLegacy : "&fZamień: " + c.nameLegacy);
    }

    private Text foreverLabel() {
        return Text.literal("Data: na zawsze (nie pokazuj daty): " + (Config.d.forever ? "TAK" : "NIE"));
    }

    private void refresh() {
        modeButton.setMessage(modeLabel());
        snupButton.setMessage(mainLabel(Costume.SNUP));
        adixButton.setMessage(mainLabel(Costume.ADIX));
        foreverButton.setMessage(foreverLabel());
    }

    private void run(Costume c) {
        if (this.client == null || this.client.player == null) { error("Musisz być w grze."); return; }
        boolean done = give() ? Wardrobe.giveFake(c) : Wardrobe.swapHeld(c);
        if (done) ok(give() ? "Gotowe — nadano fake item (widzisz go tylko Ty)." : "Gotowe — ten jeden przedmiot wygląda jak kostium.");
        else error("Nie udało się (sprawdź czat).");
    }

    private void onDate(String text) {
        try {
            LocalDateTime t = LocalDateTime.parse(text.trim(), Config.DATE);
            Config.setExpiry(t);
            ok("Zapisano datę ważności.");
        } catch (Exception e) {
            error("Zły format daty (dd-MM-yyyy, HH:mm:ss).");
        }
    }

    private void ok(String s) { status = s; statusColor = 0x55FF55; }

    private void error(String s) { status = s; statusColor = 0xFF5555; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int y = this.height / 2 - 82;
        ctx.drawCenteredTextWithShadow(this.textRenderer, Legacy.parse("&b&lEasySnup"), cx, y - 26, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Legacy.parse("&7Kostium &bsnupa &7/ &cadixa"), cx, y - 14, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Data ważności (dd-MM-yyyy, HH:mm:ss)"), cx, y + 80, 0xAAAAAA);
        if (!status.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(status), cx, y + 172, statusColor);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
