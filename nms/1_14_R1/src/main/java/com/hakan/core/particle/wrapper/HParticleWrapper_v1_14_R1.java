package com.hakan.core.particle.wrapper;

import com.hakan.core.HCore;
import com.hakan.core.particle.HParticle;
import net.minecraft.server.v1_14_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_14_R1.ParticleParam;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_14_R1.CraftParticle;
import org.bukkit.entity.Player;

/**
 * {@inheritDoc}
 */
public final class HParticleWrapper_v1_14_R1 implements HParticleWrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public void play(Player player, Location location, HParticle hParticle) {
        ParticleParam particleParam = null;
        try {
            particleParam = CraftParticle.toNMS(Particle.valueOf(hParticle.getParticleName()));
        } catch (Exception e) {
            for (Particle particle : Particle.values()) {
                if (particle.name().toLowerCase().contains(hParticle.getParticleName().toLowerCase())) {
                    particleParam = CraftParticle.toNMS(particle);
                    break;
                }
            }
        }

        Validate.notNull(particleParam, "Particle not found!");
        HCore.sendPacket(player, new PacketPlayOutWorldParticles(particleParam, false, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) hParticle.getOffset().getX(), (float) hParticle.getOffset().getY(), (float) hParticle.getOffset().getZ(), (float) hParticle.getSpeed(), hParticle.getAmount()));
    }
}