package me.lucaaa.advanceddisplays.commands.subcommands;

import me.lucaaa.advanceddisplays.AdvancedDisplays;
import me.lucaaa.advanceddisplays.api.displays.enums.DisplayType;
import me.lucaaa.advanceddisplays.displays.ADBaseDisplay;
import me.lucaaa.advanceddisplays.displays.ADTextDisplay;
import org.bukkit.command.CommandSender;

import java.util.List;

public class NextPageSubCommand extends SubCommandsFormat {
    public NextPageSubCommand(AdvancedDisplays plugin) {
        super(plugin);
        this.name = "nextPage";
        this.description = "Switches to the next page of a text display.";
        this.usage = "/ad nextPage [name]";
        this.minArguments = 1;
        this.executableByConsole = true;
        this.neededPermission = "ad.page";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return plugin.getDisplaysManager().getDisplays().values().stream().filter(display -> display.getType() == DisplayType.TEXT).map(ADBaseDisplay::getName).toList();
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        ADBaseDisplay display = plugin.getDisplaysManager().getDisplayFromMap(args[1]);

        if (display == null) {
            sender.sendMessage(plugin.getMessagesManager().getColoredMessage("&cThe display &b" + args[1] + " &cdoes not exist!", true));
            return;
        }

        if (!(display instanceof ADTextDisplay textDisplay)) {
            sender.sendMessage(plugin.getMessagesManager().getColoredMessage("&cThe display &b" + args[1] + " &cis not a text display!", true));
            return;
        }

        textDisplay.nextPage();
        sender.sendMessage(plugin.getMessagesManager().getColoredMessage("&aThe display &e" + args[1] + " &ais now showing its next page.", true));
    }
}