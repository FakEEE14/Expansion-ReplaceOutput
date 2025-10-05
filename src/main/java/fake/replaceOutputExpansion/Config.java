package fake.replaceOutputExpansion;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Config {

    private final ReplaceOutputExpansion expansion;
    private final PlaceholderAPIPlugin plugin;
    private FileConfiguration config;
    private File file;

    Config(ReplaceOutputExpansion expansion) {
        this.expansion = expansion;
        this.plugin = expansion.getPlaceholderAPI();
        reload();
    }

    private void reload() {
        if (this.file == null) {
            this.file = new File(this.plugin.getDataFolder()+File.separator+"expansions"+ File.separator + this.expansion.getIdentifier(), "config.yml");
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        this.config.options().header(
                  "ReplaceOutput Configuration\n" +
                        "\n" +
                        "How to use the placeholder:\n" +
                        "  %replaceoutput_replacement_value%\n" +
                        "   replacement: Config you Want\n" +
                        "   value: value you want to \n" +
                        "\n" +
                        "NOTE: Do not put underline in the replacements\n" +
                        " wrong  : my_placeholder\n" +
                        " correct: my-placeholder\n" +
                        "\n" +
                        "VARIABLES: Internal placeholders\n" +
                        " %value%: Return the value you inserted\n" +
                        "\n" +
                        "PLACEHOLDERS: Start with { and End with }\n" +
                        " example:\n" +
                        "  {player_name}\n" +
                        "\n" +
                        "NEGATE: Put ! Before the Condition\n" +
                        " example:\n" +
                        "  !equalsignorecase\n" +
                        "\n" +
                        "\n" +
                        "Available condition types:\n" +
                        "\n" +
                        "RANGE CONDITIONS:\n" +
                        " range::min~max, ~~::<min>~<max>  - Value is between min and max (inclusive)\n" +
                        "\n" +
                        "EQUALITY CONDITIONS:\n" +
                        " <value>, equals::<value>         - Exact match\n" +
                        " equalsignorecase::<value>        - Match (Case-insensitive)\n" +
                        "\n" +
                        "STRING CONDITIONS:\n" +
                        " startswith::<text>               - Starts with text\n" +
                        " endswith::<text>                 - Ends with text\n" +
                        " startendwith::<start>~<end>      - Does not end with text\n" +
                        " contains::<text>                 - Contains text\n" +
                        " containsignoreCase::<text>       - Contains text (case-insensitive)\n" +
                        "\n" +
                        "NUMERIC CONDITIONS:\n" +
                        " <value>, ==::<value>             - Exact match\n" +
                        " greater::number, >::number       - Greater than\n" +
                        " lower::number, <::number         - Less than\n" +
                        " greaterthan::number, >=::number  - Greater than or equal\n" +
                        " lowerthan::number, <=::number    - Less than or equal\n" +
                        "\n" +
//                        "JAVASCRIPT: \n" +
//                        " JS::<evaluator>                  - JavaScript evaluator (use %value%)\n"+
//                        "  examples:\n" +
//                        "   js::%value%.equals('hi') || %value%.equals('hello'):\n" +
//                        "   js::0 <= %value% && %value% <= 100\n" +
//                        "\n" +
                        "ELSE:\n" +
                        " else                             - Anything else"
        );

        String settingsPath = "Settings";
        if (!this.config.isBoolean(settingsPath + ".Debug")) {
            this.config.set(settingsPath + ".Debug", false);
        }

        String replacementsPath = "Replacements";
        if (!this.config.isConfigurationSection(replacementsPath)) {

            this.config.set(replacementsPath + ".ping.~~::0~50", "&a%ping%");
            this.config.set(replacementsPath + ".ping.~~::51~100", "&e%ping%");
            this.config.set(replacementsPath + ".ping.~~::101~200", "&6%ping%");
            this.config.set(replacementsPath + ".ping.>::200", "&c%ping%");
            this.config.set(replacementsPath + ".ping.else", "&4%ping%");

            this.config.set(replacementsPath + ".my-shop-placeholder.~~::-100~-1", "Negative");
            this.config.set(replacementsPath + ".my-shop-placeholder.0", "Zero");
            this.config.set(replacementsPath + ".my-shop-placeholder.~~::1~100", "Positive");
            this.config.set(replacementsPath + ".my-shop-placeholder.<::{placeholder-for-price}", "No Money");
            this.config.set(replacementsPath + ".my-shop-placeholder.else", "NAN");

            this.config.set(replacementsPath + ".my-placeholder.empty", "");
            this.config.set(replacementsPath + ".my-placeholder.else", "[{my_placeholder}] ");

            this.config.set(replacementsPath + ".health.~~::0~20", "&c❤❤❤❤❤");
            this.config.set(replacementsPath + ".health.~~::21~40", "&6❤❤❤❤♡");
            this.config.set(replacementsPath + ".health.~~::41~60", "&e❤❤❤♡♡");
            this.config.set(replacementsPath + ".health.~~::61~80", "&a❤❤♡♡♡");
            this.config.set(replacementsPath + ".health.~~::81~100", "&a❤♡♡♡♡");
            this.config.set(replacementsPath + ".health.else", "&7♡♡♡♡♡");

            this.config.set(replacementsPath + ".permission-level.==::0", "&7Guest");
            this.config.set(replacementsPath + ".permission-level.==::1", "&fMember");
            this.config.set(replacementsPath + ".permission-level.==::2", "&aModerator");
            this.config.set(replacementsPath + ".permission-level.==::3", "&cAdmin");
            this.config.set(replacementsPath + ".permission-level.>=::4", "&4Owner");
            this.config.set(replacementsPath + ".permission-level.else", "&7Unknown");

            this.config.set(replacementsPath + ".online.true", "&aOnline");
            this.config.set(replacementsPath + ".online.!equals::true", "&cOffline");
            this.config.set(replacementsPath + ".online.else", "&7Unknown");
        }

        save();
    }

    FileConfiguration load() {
        if (this.config == null) {
            reload();
        }
        return this.config;
    }

    private void save() {
        if (this.config == null || this.file == null) {return;}
        try {
            File parent = this.file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not Crate Directory");
            }
            load().save(this.file);
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.file, ex);
        }
    }
}