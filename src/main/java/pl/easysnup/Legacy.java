package pl.easysnup;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Zamienia kody &x na Text. */
public final class Legacy {
    private static final Style BASE = Style.EMPTY.withItalic(false);

    private Legacy() {}

    public static MutableText parse(String s) {
        MutableText out = Text.empty();
        Style style = BASE;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length()) {
                Formatting f = Formatting.byCode(s.charAt(i + 1));
                if (f != null) {
                    if (buf.length() > 0) {
                        out.append(Text.literal(buf.toString()).setStyle(style));
                        buf.setLength(0);
                    }
                    if (f == Formatting.RESET) {
                        style = BASE;
                    } else if (f.isColor()) {
                        style = BASE.withColor(f);
                    } else {
                        style = style.withFormatting(f);
                    }
                    i++;
                    continue;
                }
            }
            buf.append(c);
        }
        if (buf.length() > 0) {
            out.append(Text.literal(buf.toString()).setStyle(style));
        }
        return out;
    }
}
