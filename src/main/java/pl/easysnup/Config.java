package pl.easysnup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Config {
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final class Saved {
        public String costume;
        public boolean fake;
        public int slot;
        public String item;
    }

    public static final class Data {
        public boolean forever = true;
        public String expiry = null;
        /** false = zamiana trzymanego przedmiotu, true = fake item */
        public boolean giveMode = false;
        public String worn = null;
        public List<Saved> saved = new ArrayList<>();
    }

    public static Data d = new Data();

    private Config() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("easysnup.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                Data read = GSON.fromJson(Files.readString(p), Data.class);
                if (read != null) d = read;
            }
        } catch (Exception ignored) {
        }
        if (d.saved == null) d.saved = new ArrayList<>();
        if (d.expiry == null) {
            d.expiry = LocalDateTime.now().plusDays(30).withNano(0).toString();
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(d));
        } catch (IOException ignored) {
        }
    }

    public static LocalDateTime expiryDate() {
        try {
            return LocalDateTime.parse(d.expiry);
        } catch (Exception e) {
            return LocalDateTime.now().plusDays(30).withNano(0);
        }
    }

    public static String expiryText() {
        return DATE.format(expiryDate());
    }

    public static void setExpiry(LocalDateTime t) {
        d.expiry = t.toString();
        save();
    }
}
