package me.lucaaa.advanceddisplays.actions;

import me.lucaaa.advanceddisplays.common.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Action {
    private final int delay;
    private final boolean global;
    private final boolean globalPlaceholders;
    private boolean correctFormat = true;
    private final List<String> missingFields = new ArrayList<>();
    protected boolean isCorrect = true;

    public Action(List<String> requiredFields, ConfigurationSection section, boolean canBeGlobal) {
        this.delay = section.getInt("delay", 0);
        this.global = canBeGlobal && section.getBoolean("global", false);
        this.globalPlaceholders = section.getBoolean("global-placeholders", true);

        for (String requiredField : requiredFields) {
            if (section.get(requiredField) == null) {
                this.missingFields.add(requiredField);
                this.correctFormat = false;
            }
        }
    }

    public Action(List<String> requiredFields, ConfigurationSection section) {
        this(requiredFields, section, true);
    }

    /**
     * Runs the action for a specific player.
     * @param clickedPlayer The player who clicked the display.
     * @param actionPlayer Who to run the action for.
     */
    public abstract void runAction(Player clickedPlayer, Player actionPlayer);

    public int getDelay() {
        return delay;
    }

    public boolean isGlobal() {
        return global;
    }

    public boolean useGlobalPlaceholders() {
        return globalPlaceholders;
    }

    public boolean isFormatCorrect() {
        return correctFormat;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public BaseComponent[] getTextComponent(String message, Player clickedPlayer, Player actionPlayer) {
        return Utils.getTextComponent(message, clickedPlayer, actionPlayer, useGlobalPlaceholders());
    }

    public String getTextString(String message, Player clickedPlayer, Player actionPlayer) {
        return BaseComponent.toLegacyText(Utils.getTextComponent(message, clickedPlayer, actionPlayer, useGlobalPlaceholders()));
    }
}