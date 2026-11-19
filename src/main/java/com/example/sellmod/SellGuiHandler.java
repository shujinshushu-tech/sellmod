package com.example.sellmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.List;

public class SellGuiHandler extends GenericContainerScreenHandler {

    public SellGuiHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, new SimpleInventory(27), 3);
        populateDisplayItems();
    }

    private void populateDisplayItems() {
        List<SellPrices.SellEntry> entries = SellPrices.ENTRIES;
        for (int i = 0; i < entries.size() && i < 27; i++) {
            SellPrices.SellEntry entry = entries.get(i);
            ItemStack displayStack = new ItemStack(entry.item(), entry.requiredAmount());

            NbtCompound displayNbt = displayStack.getOrCreateSubNbt("display");
            displayNbt.putString("Name", Text.Serializer.toJson(
                Text.literal("§e§lSell " + entry.item().getName().getString())
            ));

            NbtList loreList = new NbtList();
            loreList.add(NbtString.of(Text.Serializer.toJson(
                Text.literal("§7Amount Required: §f" + entry.requiredAmount() + "x")
            )));
            loreList.add(NbtString.of(Text.Serializer.toJson(
                Text.literal("§7Reward: §a" + entry.emeraldReward() + " Emerald(s)")
            )));
            loreList.add(NbtString.of(Text.Serializer.toJson(Text.literal(" "))));
            loreList.add(NbtString.of(Text.Serializer.toJson(
                Text.literal("§bLeft-Click: §fSell 1 batch")
            )));
            loreList.add(NbtString.of(Text.Serializer.toJson(
                Text.literal("§6Shift-Click: §fSell ALL in inventory")
            )));

            displayNbt.put("Lore", loreList);
            this.getInventory().setStack(i, displayStack);
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < 27) {
            if (player instanceof ServerPlayerEntity serverPlayer && slotIndex < SellPrices.ENTRIES.size()) {
                SellPrices.SellEntry entry = SellPrices.ENTRIES.get(slotIndex);
                boolean sellAll = (actionType == SlotActionType.QUICK_MOVE);
                processSale(serverPlayer, entry, sellAll);
            }
            return;
        }
        if (actionType == SlotActionType.QUICK_MOVE) return;
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    private void processSale(ServerPlayerEntity player, SellPrices.SellEntry entry, boolean sellAll) {
        int available = countCleanItems(player.getInventory(), entry.item());
        int batches = available / entry.requiredAmount();

        if (batches <= 0) {
            player.sendMessage(Text.literal("§cYou need at least " + entry.requiredAmount() + "x "
                + entry.item().getName().getString() + " to sell!"), false);
            return;
        }

        int batchesToSell = sellAll ? batches : 1;
        int itemsToRemove = batchesToSell * entry.requiredAmount();
        int emeraldsToGive = batchesToSell * entry.emeraldReward();

        removeCleanItems(player.getInventory(), entry.item(), itemsToRemove);
        giveEmeralds(player, emeraldsToGive);

        player.sendMessage(Text.literal("§aSold " + itemsToRemove + "x " + entry.item().getName().getString()
            + " for " + emeraldsToGive + " Emerald(s)!"), false);
    }

    private int countCleanItems(PlayerInventory inv, Item target) {
        int total = 0;
        for (int i = 0; i < inv.main.size(); i++) {
            ItemStack stack = inv.main.get(i);
            if (stack.isOf(target) && !stack.hasEnchantments() && !stack.hasCustomName()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private void removeCleanItems(PlayerInventory inv, Item target, int amountToRemove) {
        int remaining = amountToRemove;
        for (int i = 0; i < inv.main.size() && remaining > 0; i++) {
            ItemStack stack = inv.main.get(i);
            if (stack.isOf(target) && !stack.hasEnchantments() && !stack.hasCustomName()) {
                int take = Math.min(stack.getCount(), remaining);
                stack.decrement(take);
                remaining -= take;
            }
        }
    }

    private void giveEmeralds(ServerPlayerEntity player, int amount) {
        while (amount > 0) {
            int stackSize = Math.min(amount, 64);
            ItemStack reward = new ItemStack(Items.EMERALD, stackSize);
            if (!player.getInventory().insertStack(reward)) {
                player.dropItem(reward, false);
            }
            amount -= stackSize;
        }
    }
}
