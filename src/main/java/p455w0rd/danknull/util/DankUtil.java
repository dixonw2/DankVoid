package p455w0rd.danknull.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import p455w0rd.danknull.items.ItemDankNullReplica;

public final class DankUtil {

    public static List<Integer> findDankSlots(EntityPlayer player) {
        final InventoryPlayer inventory = player.inventory;
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (inventory.getStackInSlot(i).getItem() instanceof ItemDankNullReplica) {
                slots.add(i);
            }
        }

        return slots;
    }
}
