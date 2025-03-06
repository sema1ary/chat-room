package ru.sema1ary.chatroom.listener.flower;

import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.vidoskim.bukkit.item.builder.ItemBuilder;
import ru.vidoskim.bukkit.service.ConfigService;

import java.util.List;
import java.util.function.Consumer;

public class FlowerListener implements Listener {
    private final MiniMessage miniMessage;
    private final ConfigService configService;

    private final List<String> flowers;

    public FlowerListener(MiniMessage miniMessage, ConfigService configService) {
        this.miniMessage = miniMessage;
        this.configService = configService;
        flowers = configService.get("flowers-list");
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        handleEvent(player, eventPlayer -> {
            Block clickedBlock = event.getClickedBlock();

            if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || clickedBlock == null || clickedBlock.getType().isAir()
                    || !flowers.contains(clickedBlock.getType().toString())) {
                return;
            }

            Material material = Material.getMaterial(clickedBlock.getType().toString()
                    .replace("POTTED_", ""));

            assert material != null;
            if(player.getInventory().contains(material)) {
                player.sendMessage(miniMessage.deserialize("flower-limit-error"));
                event.setCancelled(true);
                return;
            }

            eventPlayer.getInventory().addItem(ItemBuilder.newBuilder(
                    clickedBlock.getType()).build());

            player.sendMessage(miniMessage.deserialize(configService.get("flower-pickup")));
            event.setCancelled(true);
        });

    }

    @EventHandler
    private void onEntityInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        handleEvent(player, eventPlayer -> {
            if(!(event.getRightClicked() instanceof Player rightClickedPlayer) || event.getHand() == EquipmentSlot.OFF_HAND) {
                return;
            }

            ItemStack itemInMainHand = eventPlayer.getInventory().getItemInMainHand();
            if(!flowers.contains("POTTED_" + itemInMainHand.getType())) {
                return;
            }

            if(rightClickedPlayer.getInventory().contains(itemInMainHand.getType())) {
                eventPlayer.sendMessage(miniMessage.deserialize(
                        configService.get("flower-target-already-have-flower")));
                return;
            }

            rightClickedPlayer.getInventory().addItem(ItemBuilder.newBuilder(itemInMainHand.getType())
                    .build());
            player.getInventory().setItemInMainHand(null);

            player.sendMessage(miniMessage.deserialize(configService.get("flower-gift")));
            rightClickedPlayer.sendMessage(miniMessage.deserialize(configService.get("flower-gift-target")));
        });
    }

    @EventHandler
    private void onPickup(PlayerPickItemEvent event) {
        handleEvent(event.getPlayer(), player -> event.setCancelled(true));
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        handleEvent(event.getPlayer(), player -> event.setCancelled(true));
    }

    private void handleEvent(Player player, Consumer<Player> consumer) {
        if(configService.get("enable-flower-give")) {
            consumer.accept(player);
        }
    }
}
