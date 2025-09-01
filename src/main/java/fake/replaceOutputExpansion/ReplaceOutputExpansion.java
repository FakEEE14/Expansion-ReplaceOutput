package fake.replaceOutputExpansion;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ReplaceOutputExpansion extends PlaceholderExpansion implements Cacheable, Configurable {

    @Override
    public @NotNull String getIdentifier() {return "gcro";}
    @Override
    public @NotNull String getAuthor() {return "FakEE7";}
    @Override
    public @NotNull String getVersion() {return "1.0";}
    @Override
    public Map<String, Object> getDefaults() {return Map.of();}
    @Override
    public boolean canRegister() {return true;}

    @Override
    public void clear() {
        this.replacements.clear();
        debug("Cleared all cached data");
    }

    private final Map<String, Map<String, String>> replacements = new HashMap<>();
    private boolean debug = false;

    @Override
    public boolean register() {
        if (!canRegister()) {
            return false;
        }
        FileConfiguration config = new Config(this).load();

        ConfigurationSection settings = config.getConfigurationSection("Settings");
        if (settings != null) {
            this.debug = settings.getBoolean("Debug", false);
        }

        ConfigurationSection replacementsSection = config.getConfigurationSection("Replacements");
        if (replacementsSection != null) {
            for (String placeholderName : replacementsSection.getKeys(false)) {
                ConfigurationSection placeholderSection = replacementsSection.getConfigurationSection(placeholderName);
                if (placeholderSection != null) {
                    Map<String, String> placeholderConfig = new LinkedHashMap<>();

                    for (String condition : placeholderSection.getKeys(false)) {
                        String replacement = placeholderSection.getString(condition);
                        placeholderConfig.put(condition, replacement);
                        debug("Loading condition '" + condition + "' with replacement '" + replacement + "' for placeholder '" + placeholderName + "'");
                    }

                    this.replacements.put(placeholderName, placeholderConfig);
                }
            }
        }

        debug("Loaded placeholder configurations");
        return super.register();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] parts = processNestedPlaceholders(player,params).split("_", 2);
        if (parts.length < 2) {
            debug("Invalid format for params: " + params + ". Expected: placeholderName_value");
            return "";
        }

        String placeholderName = parts[0];
        String value = parts[1];

        if (this.replacements.get(placeholderName) == null) {
            debug("Unknown placeholder: " + placeholderName);
            return value;
        }

        Map<String, String> placeholderConfig = this.replacements.get(placeholderName);
        if (placeholderConfig == null) {
            debug("No configuration found for placeholder: " + placeholderName);
            return value;
        }

        String result = processReplacement(player ,placeholderConfig, value);
        debug("Processed " + placeholderName + " with value '" + value + "' -> '" + result + "'");
        return processNestedPlaceholders(player,result);
    }

    private String processReplacement(OfflinePlayer player,Map<String, String> config, String value) {

        if (config.containsKey(value)) {
            return config.get(value);
        }

        for (Map.Entry<String, String> entry : config.entrySet()) {
            String condition = entry.getKey();
            if (condition.equals("else")) continue;

            if (matchesCondition(processNestedPlaceholders(player, condition), value)) {
                debug("Value matches condition: " + condition);
                return config.get(condition);
            }
        }

        if (config.containsKey("else")) {
            String result = config.get("else");
            debug("Using else clause for value: " + value);
            return result;
        }

        debug("No match found for value: " + value + ", returning original");
        return value;
    }


    private boolean matchesCondition(String condition, String value) {
        try {
            String[] parts = condition.split("::", 2);
            String conditionType = parts[0].toLowerCase();
            String conditionValue = parts.length > 1 ? parts[1] : "";

            boolean negated = conditionType.startsWith("!");
            if (negated) {
                conditionType = conditionType.substring(1);
            }

            switch (conditionType.toLowerCase()) {
//                case "js":
//                    return evalJs(conditionValue, value, negated);

                case "~~":
                case "range":
                    String[] rangeParts = conditionValue.split("~", 2);
                    if (rangeParts.length == 2) {
                        double numValue = Double.parseDouble(value);
                        double first = Double.parseDouble(rangeParts[0]);
                        double second = Double.parseDouble(rangeParts[1]);
                        double min = Math.min(first, second);
                        double max = Math.max(first, second);
                        boolean inRange = min <= numValue && numValue <= max;
                        return negated != inRange;
                    }
                    return false;

                case "==":
                case "equals":
                    boolean equals = value.equals(conditionValue);
                    return negated != equals;

                case "equalsignorecase":
                    boolean equalsIgnore = value.equalsIgnoreCase(conditionValue);
                    return negated != equalsIgnore;

                case "startswith":
                    boolean startsWith = value.startsWith(conditionValue);
                    return negated != startsWith;

                case "endswith":
                    boolean endsWith = value.endsWith(conditionValue);
                    return negated != endsWith;

                case "startendwith":
                    String[] seeParts = conditionValue.split("~", 2);
                    if (seeParts.length == 2) {
                        boolean startsEndsWith = value.startsWith(seeParts[0]) && value.endsWith(seeParts[1]);
                        return negated != startsEndsWith;
                    }
                    return false;

                case "contains":
                    boolean contains = value.contains(conditionValue);
                    return negated != contains;

                case "containsignorecase":
                    boolean containsIgnore = value.toLowerCase().contains(conditionValue.toLowerCase());
                    return negated != containsIgnore;

                case ">":
                case "greater":
                    double numValue1 = Double.parseDouble(value);
                    double compareValue1 = Double.parseDouble(conditionValue);
                    return negated ? numValue1 <= compareValue1 : numValue1 > compareValue1;

                case "<":
                case "lower":
                    double numValue2 = Double.parseDouble(value);
                    double compareValue2 = Double.parseDouble(conditionValue);
                    return negated ? numValue2 >= compareValue2 : numValue2 < compareValue2;

                case ">=":
                case "greaterthan":
                    double numValue3 = Double.parseDouble(value);
                    double compareValue3 = Double.parseDouble(conditionValue);
                    return negated ? numValue3 < compareValue3 : numValue3 >= compareValue3;

                case "<=":
                case "lowerthan":
                    double numValue4 = Double.parseDouble(value);
                    double compareValue4 = Double.parseDouble(conditionValue);
                    return negated ? numValue4 > compareValue4 : numValue4 <= compareValue4;
            }
        } catch (NumberFormatException e) {
            debug("Invalid numeric value for condition: " + condition + " with value: " + value);
        }

        return false;
    }
    private String processNestedPlaceholders(OfflinePlayer offlinePlayer, String identifier) {
        while (identifier.contains("{") && identifier.contains("}")) {
            int startIndex = identifier.lastIndexOf("{");
            int endIndex = identifier.indexOf("}", startIndex);

            if (startIndex != -1 && endIndex != -1) {
                String placeholderName = identifier.substring(startIndex + 1, endIndex);
                String placeholderValue = PlaceholderAPI.setPlaceholders((Player) offlinePlayer, "%" + placeholderName + "%");
                identifier = identifier.substring(0, startIndex) + placeholderValue + identifier.substring(endIndex + 1);
            } else {
                break;
            }
        }

        return identifier;
//        return this.provider.onPlaceholderRequest(null, offlinePlayer.getUniqueId(), identifier);
    }
//    private boolean evalJs(String script, String value, boolean negated) {
//        try {
//            ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
//
//            script = script.replace("%value%", "\"" + value + "\"");
//
//            Object evalResult = engine.eval(script);
//            if (!(evalResult instanceof Boolean)) {return false;}
//
//            return negated != (Boolean) evalResult;
//
//        } catch (ScriptException e) {
//            debug("Invalid JS condition: " + script + " error: " + e.getMessage());
//            return false;
//        }
//    }

    private void debug(String message) {
        if (this.debug) {
            System.out.println(getIdentifier() + " [DEBUG] - " + message);
        }
    }
}