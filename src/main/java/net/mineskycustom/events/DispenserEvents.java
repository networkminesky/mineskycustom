package net.mineskycustom.events;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import net.mineskycustom.MineSkyCustom;
import net.mineskycustom.custom.CustomObject;
import net.mineskycustom.custom.blocks.CustomBlock;
import net.mineskycustom.custom.plants.CustomPlant;
import net.mineskycustom.handler.BlockHandler;
import net.mineskycustom.handler.InstrumentConverter;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.position.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DispenserEvents implements Listener {

    private static final Set<org.bukkit.Location> processing = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreDispense(BlockPreDispenseEvent e) {
        final Block b = e.getBlock();
        final ItemStack stack = e.getItemStack();

        if (b.getType() != Material.DISPENSER)
            return;

        // 1. Trava anti-loop de redstone no mesmo tick
        if (!processing.add(b.getLocation())) {
            e.setCancelled(true);
            return;
        }

        try {
            CustomObject object = BlockHandler.getCustomObjectFromItemStack(stack);
            if (!stack.getType().isBlock()) {
                if (object == null)
                    return; // Deixa flechas, poções e itens vanilla agirem normalmente
                else if (object.isCustomBlock() && (object.getObject() instanceof CustomBlock cb && cb.getOrientedValues() != null))
                    return;
            }

            org.bukkit.block.data.type.Dispenser dispenser = (org.bukkit.block.data.type.Dispenser) b.getBlockData();
            Block relative = b.getRelative(dispenser.getFacing());

            if (!relative.getType().isAir()) {
                e.setCancelled(true);
                return;
            }

            // Checagem de claims
            Position originalPos = BukkitHuskClaimsAPI.getInstance().getPosition(b.getLocation());
            Position relativePos = BukkitHuskClaimsAPI.getInstance().getPosition(relative.getLocation());
            Claim originalClaim = BukkitHuskClaimsAPI.getInstance().getClaimAt(originalPos).orElse(null);
            Claim relativeClaim = BukkitHuskClaimsAPI.getInstance().getClaimAt(relativePos).orElse(null);

            if (originalClaim != relativeClaim) {
                e.setCancelled(true);
                return;
            }

            // Cancela o drop do item vanilla do dispenser
            e.setCancelled(true);

            // 2. Desconta o item com segurança
            BlockState liveState = b.getState(false);
            if (liveState instanceof org.bukkit.block.Dispenser disp) {
                ItemStack inSlot = disp.getInventory().getItem(e.getSlot());
                if (inSlot == null || inSlot.getType() == Material.AIR) {
                    return;
                }

                ItemStack copy = inSlot.clone();
                copy.setAmount(copy.getAmount() - 1);

                if (copy.getAmount() > 0) {
                    disp.getInventory().setItem(e.getSlot(), copy);
                } else {
                    disp.getInventory().setItem(e.getSlot(), null);
                }

                disp.update(true, false);
            } else {
                return;
            }

            // 3. Colocação do bloco com GRAVIDADE REAL sem dupes
            Material base = (object == null) ? stack.getType() :
                    (object.isCustomBlock() ? Material.NOTE_BLOCK : Material.TRIPWIRE);

            if (object != null) {
                // Blocos ou plantas customizadas: SEM FÍSICA
                relative.setType(base, false);
                BlockData baseData = base.createBlockData();

                if (object.isCustomBlock()) {
                    CustomBlock cb = (CustomBlock) object.getObject();
                    NoteBlock nb = (NoteBlock) baseData;
                    nb.setInstrument(InstrumentConverter.fromMinecraft(cb.getInstrument()));
                    nb.setNote(new Note(cb.getNote()));
                    relative.setBlockData(nb, false);
                    relative.getWorld().playSound(relative.getLocation(), cb.getProperties().getSound() + ".place", 1, 0.8F);
                } else {
                    CustomPlant cb = (CustomPlant) object.getObject();
                    Tripwire tr = (Tripwire) baseData;
                    BlockHandler.modifyTripwire(tr, cb);
                    relative.setBlockData(tr, false);
                    relative.getWorld().playSound(relative.getLocation(), cb.getPlantProperties().getSound() + ".place", 1, cb.getPlantProperties().getSoundPitch());
                }
            } else {
                // Blocos Vanilla:
                if (base.hasGravity()) {
                    Block below = relative.getRelative(BlockFace.DOWN);

                    // Se NÃO tiver chão sólido embaixo, ele DEVE CAIR:
                    if (!below.getType().isSolid()) {
                        FallingBlock fb = relative.getWorld().spawnFallingBlock(
                                relative.getLocation().add(0.5, 0, 0.5),
                                base.createBlockData()
                        );
                        fb.setDropItem(true);
                    } else {
                        // Se JÁ estiver apoiado no chão firme, coloca como bloco estático seguro
                        // Isso impede que a entidade gere drop duplo com a pá de Eficiência 5!
                        relative.setType(base, false);
                    }
                } else {
                    // Outros blocos vanilla normais (Madeira, Pedra, etc.)
                    relative.setType(base, true);
                }

                relative.getWorld().playSound(b.getLocation(), relative.getBlockSoundGroup().getPlaceSound(), 1, 0.8f);
            }

        } finally {
            processing.remove(b.getLocation());
        }
    }
}
