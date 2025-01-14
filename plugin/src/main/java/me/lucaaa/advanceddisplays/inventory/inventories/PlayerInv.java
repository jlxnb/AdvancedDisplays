package me.lucaaa.advanceddisplays.inventory.inventories;

import me.lucaaa.advanceddisplays.AdvancedDisplays;
import me.lucaaa.advanceddisplays.api.displays.BaseDisplay;
import me.lucaaa.advanceddisplays.api.displays.enums.EditorItem;
import me.lucaaa.advanceddisplays.data.NamedEnum;
import me.lucaaa.advanceddisplays.inventory.Button;
import me.lucaaa.advanceddisplays.inventory.InventoryMethods;
import me.lucaaa.advanceddisplays.inventory.items.GlobalItems;
import me.lucaaa.advanceddisplays.inventory.items.InventoryItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;

import java.util.*;

public class PlayerInv {
    private final Player player;
    private final List<EditorItem> disabledItems;
    private InventoryRows currentRow = InventoryRows.SCALE_TRANSLATION;
    private final Transformation transformation;
    private final BaseDisplay display;

    // Rows of buttons
    private final Map<InventoryRows, Map<Integer, Button.PlayerButton>> rows = new HashMap<>();
    // Buttons independent of the rows
    private final Map<Integer, Button.PlayerButton> buttons = new HashMap<>();

    public PlayerInv(AdvancedDisplays plugin, Player player, List<EditorItem> disabledItems, BaseDisplay display) {
        this.player = player;
        this.disabledItems = disabledItems;
        this.display = display;
        this.transformation = display.getTransformation();

        InventoryItems items = new InventoryItems(display);

        // ---[ SCALE & TRANSLATION ]----
        addScaleTranslationButtons(items);
        // ----------

        // ---[ LEFT ROTATION AND YAW & PITCH ]----
        addLeftRotButtons(items);
        // ----------

        // ---[ RIGHT ROTATION AND HITBOX ]----
        addRightRotButtons(items);
        // ----------

        // ---[ GLOBAL BUTTONS ]----
        addGlobalButton(6, new Button.PlayerButton(items.CHANGE_ROW) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                currentRow = getItem().changeEnumValue(true);
                player.getInventory().setItem(8, getItem().getItemStack());
                setContents(currentRow);
            }
        });

        addGlobalButton(7, new Button.PlayerButton(items.OPEN_GUI) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                InventoryMethods inventory = new EditorGUI(plugin, disabledItems, display);
                plugin.getInventoryManager().handleOpen(event.getPlayer(), inventory, display);
            }
        });

        addGlobalButton(8, new Button.PlayerButton(GlobalItems.DONE) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                plugin.getInventoryManager().getEditingPlayer(player).finishEditing();
                player.sendMessage(plugin.getMessagesManager().getColoredMessage("&aYour old inventory has been successfully given back to you.", true));
            }
        });
        // ----------

        setContents(currentRow);
    }

    private void addButtons(InventoryRows row, Map<Integer, Button.PlayerButton> buttons) {
        rows.put(row, buttons);
    }

    private void addGlobalButton(int slot, Button.PlayerButton button) {
        buttons.put(slot, button);
    }

    public void handleClick(int slot, PlayerInteractEvent event) {
        if (buttons.containsKey(slot)) {
            buttons.get(slot).onClick(event);
        } else {
            rows.get(currentRow).get(slot).onClick(event);
        }
    }

    public void setContents(InventoryRows hotbarRow) {
        // The player's inventory has 36 slots
        ItemStack[] itemArray = new ItemStack[36];

        // Hotbar buttons
        Map<InventoryRows, Map<Integer, Button.PlayerButton>> maps = new HashMap<>(rows);
        for (Map.Entry<Integer, Button.PlayerButton> entry : maps.remove(hotbarRow).entrySet()) {
            itemArray[entry.getKey()] = entry.getValue().getItem().getItemStack();
        }

        // Inventory buttons
        int i = 27;
        for (Map.Entry<InventoryRows, Map<Integer, Button.PlayerButton>> entry : maps.entrySet()) {
            for (Map.Entry<Integer, Button.PlayerButton> row : entry.getValue().entrySet()) {
                itemArray[i + row.getKey()] = row.getValue().getItem().getItemStack();
            }
            i = i - 9;
        }

        // Global buttons
        for (Map.Entry<Integer, Button.PlayerButton> entry : buttons.entrySet()) {
            itemArray[entry.getKey()] = entry.getValue().getItem().getItemStack();
        }

        player.getInventory().setContents(itemArray);
    }

    private boolean isLeftClick(PlayerInteractEvent event) {
        return event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
    }

    private void addScaleTranslationButtons(InventoryItems items) {
        Map<Integer, Button.PlayerButton> scaleTranslationMap = new HashMap<>();

        scaleTranslationMap.put(0, getCheckedAllowed(EditorItem.SCALE, new Button.PlayerButton(items.SCALE_X) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(0, getItem().getItemStack());
                transformation.getScale().set(newValue, transformation.getScale().y, transformation.getScale().z);
                display.setTransformation(transformation);
            }
        }));

        scaleTranslationMap.put(1, getCheckedAllowed(EditorItem.SCALE, new Button.PlayerButton(items.SCALE_Y) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(1, getItem().getItemStack());
                transformation.getScale().set(transformation.getScale().x, newValue, transformation.getScale().z);
                display.setTransformation(transformation);
            }
        }));

        scaleTranslationMap.put(2, getCheckedAllowed(EditorItem.SCALE, new Button.PlayerButton(items.SCALE_Z) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(2, getItem().getItemStack());
                transformation.getScale().set(transformation.getScale().x, transformation.getScale().y, newValue);
                display.setTransformation(transformation);
            }
        }));

        scaleTranslationMap.put(3, getCheckedAllowed(EditorItem.TRANSLATION, new Button.PlayerButton(items.TRANSLATION_X) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(3, getItem().getItemStack());
                transformation.getTranslation().set(newValue, transformation.getTranslation().y, transformation.getTranslation().z);
                display.setTransformation(transformation);
            }
        }));

        scaleTranslationMap.put(4, getCheckedAllowed(EditorItem.TRANSLATION, new Button.PlayerButton(items.TRANSLATION_Y) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(4, getItem().getItemStack());
                transformation.getTranslation().set(transformation.getTranslation().x, newValue, transformation.getTranslation().z);
                display.setTransformation(transformation);
            }
        }));

        scaleTranslationMap.put(5, getCheckedAllowed(EditorItem.TRANSLATION, new Button.PlayerButton(items.TRANSLATION_Z) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(5, getItem().getItemStack());
                transformation.getTranslation().set(transformation.getTranslation().x, transformation.getTranslation().y, newValue);
                display.setTransformation(transformation);
            }
        }));

        addButtons(InventoryRows.SCALE_TRANSLATION, scaleTranslationMap);
    }

    private void addLeftRotButtons(InventoryItems items) {
        Map<Integer, Button.PlayerButton> leftRotationMap = new HashMap<>();

        leftRotationMap.put(0, getCheckedAllowed(EditorItem.LEFT_ROTATION, new Button.PlayerButton(items.LEFT_ROTATION_X) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, 1.0, isLeftClick(event), true);
                player.getInventory().setItem(0, getItem().getItemStack());
                transformation.getLeftRotation().setAngleAxis(transformation.getLeftRotation().angle(), (float) newValue, transformation.getLeftRotation().y, transformation.getLeftRotation().z);
                display.setTransformation(transformation);
            }
        }));

        leftRotationMap.put(1, getCheckedAllowed(EditorItem.LEFT_ROTATION, new Button.PlayerButton(items.LEFT_ROTATION_Y) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, 1.0, isLeftClick(event), true);
                player.getInventory().setItem(1, getItem().getItemStack());
                transformation.getLeftRotation().setAngleAxis(transformation.getLeftRotation().angle(), transformation.getLeftRotation().x, (float) newValue, transformation.getLeftRotation().z);
                display.setTransformation(transformation);
            }
        }));

        leftRotationMap.put(2, getCheckedAllowed(EditorItem.LEFT_ROTATION, new Button.PlayerButton(items.LEFT_ROTATION_Z) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, 1.0, isLeftClick(event), true);
                player.getInventory().setItem(2, getItem().getItemStack());
                transformation.getLeftRotation().setAngleAxis(transformation.getLeftRotation().angle(), transformation.getLeftRotation().x, transformation.getLeftRotation().y, (float) newValue);
                display.setTransformation(transformation);
            }
        }));

        leftRotationMap.put(3, getCheckedAllowed(EditorItem.LEFT_ROTATION, new Button.PlayerButton(items.LEFT_ROTATION_ANGLE) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(3, getItem().getItemStack());
                transformation.getLeftRotation().setAngleAxis((float) Math.toRadians(newValue), transformation.getLeftRotation().x, transformation.getLeftRotation().y, transformation.getLeftRotation().z);
                display.setTransformation(transformation);
            }
        }));

        leftRotationMap.put(4, getCheckedAllowed(EditorItem.ROTATION, new Button.PlayerButton(items.YAW) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(4, getItem().getItemStack());
                display.setRotation((float) newValue, display.getPitch());
            }
        }));

        leftRotationMap.put(5, getCheckedAllowed(EditorItem.ROTATION, new Button.PlayerButton(items.PITCH) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(5, getItem().getItemStack());
                display.setRotation(display.getYaw(), (float) newValue);
            }
        }));

        addButtons(InventoryRows.LEFT_ROTATION_YAW_PITCH, leftRotationMap);
    }

    private void addRightRotButtons(InventoryItems items) {
        Map<Integer, Button.PlayerButton> rightRotationMap = new HashMap<>();

        rightRotationMap.put(0, getCheckedAllowed(EditorItem.RIGHT_ROTATION, new Button.PlayerButton(items.RIGHT_ROTATION_X) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, 1.0, isLeftClick(event), true);
                player.getInventory().setItem(0, getItem().getItemStack());
                transformation.getRightRotation().setAngleAxis(transformation.getRightRotation().angle(), (float) newValue, transformation.getRightRotation().y, transformation.getRightRotation().z);
                display.setTransformation(transformation);
            }
        }));

        rightRotationMap.put(1, getCheckedAllowed(EditorItem.RIGHT_ROTATION, new Button.PlayerButton(items.RIGHT_ROTATION_Y) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, 1.0, isLeftClick(event), true);
                player.getInventory().setItem(1, getItem().getItemStack());
                transformation.getRightRotation().setAngleAxis(transformation.getRightRotation().angle(), transformation.getRightRotation().x, (float) newValue, transformation.getRightRotation().z);
                display.setTransformation(transformation);
            }
        }));

        rightRotationMap.put(2, getCheckedAllowed(EditorItem.RIGHT_ROTATION, new Button.PlayerButton(items.RIGHT_ROTATION_Z) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, 1.0, isLeftClick(event), true);
                player.getInventory().setItem(2, getItem().getItemStack());
                transformation.getRightRotation().setAngleAxis(transformation.getRightRotation().angle(), transformation.getRightRotation().x, transformation.getRightRotation().y, (float) newValue);
                display.setTransformation(transformation);
            }
        }));

        rightRotationMap.put(3, getCheckedAllowed(EditorItem.RIGHT_ROTATION, new Button.PlayerButton(items.RIGHT_ROTATION_ANGLE) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(3, getItem().getItemStack());
                transformation.getRightRotation().setAngleAxis((float) Math.toRadians(newValue), transformation.getRightRotation().x, transformation.getRightRotation().y, transformation.getRightRotation().z);
                display.setTransformation(transformation);
            }
        }));

        rightRotationMap.put(4, getCheckedAllowed(EditorItem.HITBOX_SIZE, new Button.PlayerButton(items.HITBOX_WIDTH) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(4, getItem().getItemStack());
                display.setHitboxSize(true, (float) newValue, display.getHitboxHeight());
            }
        }));

        rightRotationMap.put(5, getCheckedAllowed(EditorItem.HITBOX_SIZE, new Button.PlayerButton(items.HITBOX_HEIGHT) {
            @Override
            public void onClick(PlayerInteractEvent event) {
                double newValue = getItem().changeDoubleValue(event.getPlayer().isSneaking(), 0.0, null, isLeftClick(event), true);
                player.getInventory().setItem(5, getItem().getItemStack());
                display.setHitboxSize(true, display.getHitboxWidth(), (float) newValue);
            }
        }));

        addButtons(InventoryRows.RIGHT_ROTATION_HITBOX, rightRotationMap);
    }

    public enum InventoryRows implements NamedEnum {
        SCALE_TRANSLATION("Scale & Translation"),
        LEFT_ROTATION_YAW_PITCH("Left rotation, yaw & pitch"),
        RIGHT_ROTATION_HITBOX("Right rotation & hitbox");

        private final String name;

        InventoryRows(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private Button.PlayerButton getCheckedAllowed(EditorItem requirement, Button.PlayerButton button) {
        if (!disabledItems.contains(requirement)) {
            return button;
        }

        ItemStack itemStack = button.getItem().getItemStack();
        ItemMeta meta = Objects.requireNonNull(itemStack.getItemMeta());

        meta.setDisplayName(meta.getDisplayName() + ChatColor.RED + ChatColor.BOLD + " (Disabled)");

        List<String> lore = new ArrayList<>();
        for (String line : Objects.requireNonNull(meta.getLore())) {
            if (!line.startsWith(ChatColor.YELLOW + "")) continue;

            lore.add(line);
        }

        lore.add("");
        lore.add(ChatColor.RED + "" + ChatColor.BOLD + "Setting disabled!");
        lore.add(ChatColor.GRAY + "You won't be able to change it");
        lore.add("");
        lore.add(ChatColor.BLUE + "Current value: " + ChatColor.GRAY + button.getItem().getValue());
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        return new Button.PlayerButton(button.getItem()) {
            @Override
            public void onClick(PlayerInteractEvent event) {}
        };
    }
}