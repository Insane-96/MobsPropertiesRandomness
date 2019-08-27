package insane96mcp.mpr.json.mobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import com.google.gson.annotations.SerializedName;

import insane96mcp.mpr.MobsPropertiesRandomness;
import insane96mcp.mpr.exceptions.InvalidJsonException;
import insane96mcp.mpr.json.IJsonObject;
import insane96mcp.mpr.json.JsonMob;
import insane96mcp.mpr.json.utils.JsonChance;
import insane96mcp.mpr.json.utils.JsonRangeMinMax;
import insane96mcp.mpr.lib.Reflection;
import insane96mcp.mpr.network.CreeperFuse;
import insane96mcp.mpr.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class JsonCreeper implements IJsonObject {
	public JsonRangeMinMax fuse;
	@SerializedName("explosion_radius")
	public JsonRangeMinMax explosionRadius;
	@SerializedName("powered_chance")
	public JsonChance poweredChance;
	
	@Override
	public String toString() {
		return String.format("Creeper{fuse: %s, explosionRadius: %s, poweredChance: %s}", fuse, explosionRadius, poweredChance);
	}
	
	public void Validate(final File file) throws InvalidJsonException{
		if (poweredChance != null)
			poweredChance.Validate(file);
	}
	
	public static void Apply(EntityLiving entity, World world, Random random) {
		
		if (!(entity instanceof EntityCreeper)) 
			return;
		
		EntityCreeper entityCreeper = (EntityCreeper)entity;
		
		if (world.isRemote) {
			//Fix creeper fuse animation clientside
			PacketHandler.SendToServer(new CreeperFuse(entityCreeper.getEntityId()));
			
			return;
		}
		
		for (JsonMob mob : JsonMob.mobs) {
			if (mob.creeper == null)
				continue;
			
			if (EntityList.isMatchingName(entityCreeper, new ResourceLocation(mob.mobId))) {
				JsonCreeper creeper = mob.creeper;
				
				NBTTagCompound compound = new NBTTagCompound();
				entityCreeper.writeEntityToNBT(compound);
				
				//Fuse
				if (mob.creeper.fuse != null && compound.getShort("Fuse") == 30) {
					int minFuse = (int) mob.creeper.fuse.GetMin();
					int maxFuse = (int) mob.creeper.fuse.GetMax();
					int fuse = MathHelper.getInt(random, minFuse, maxFuse);
					compound.setShort("Fuse", (short)fuse);
				}
				
				//Explosion Radius
				if (mob.creeper.explosionRadius != null && compound.getByte("ExplosionRadius") == 30) {
					int minExplosionRadius = (int) mob.creeper.explosionRadius.GetMin();
					int maxExplosionRadius = (int) mob.creeper.explosionRadius.GetMax();
					int explosionRadius = MathHelper.getInt(random, minExplosionRadius, maxExplosionRadius);
					compound.setByte("ExplosionRadius", (byte) explosionRadius);
				}
				
				//Power It
				if(creeper.poweredChance.ChanceMatches(entity, world, random))
					compound.setBoolean("powered", true);
				
				entityCreeper.readEntityFromNBT(compound);
			}
		}
	}
	
	/**
	 * Fixes area effect clouds (not spawned by the player) that spawn with duration over 8 minutes setting them to 30 seconds
	 */
	public static void FixAreaEffectClouds(Entity entity) {
		if (!(entity instanceof EntityAreaEffectCloud))
			return;
		
		NBTTagCompound tags = entity.getEntityData();
		boolean isAlreadyChecked = tags.getBoolean(MobsPropertiesRandomness.RESOURCE_PREFIX + "checked");
		
		if (isAlreadyChecked)
			return;
		
		EntityAreaEffectCloud areaEffectCloud = (EntityAreaEffectCloud) entity;
		if (areaEffectCloud.getOwner() instanceof EntityPlayer) {
			tags.setBoolean(MobsPropertiesRandomness.RESOURCE_PREFIX + "checked", true);
			return;
		}
		
		ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
		effects = (ArrayList<PotionEffect>) Reflection.Get(Reflection.EntityAreaEffectCloud_effects, areaEffectCloud);
		ArrayList<PotionEffect> newEffects = new ArrayList<PotionEffect>();
		for (PotionEffect potionEffect : effects) {
			if (potionEffect.getDuration() > 9600) {
				PotionEffect newPotionEffect = new PotionEffect(potionEffect.getPotion(), 600, potionEffect.getAmplifier());
				newEffects.add(newPotionEffect);
				continue;
			}
			newEffects.add(potionEffect);
		}
		Reflection.Set(Reflection.EntityAreaEffectCloud_effects, areaEffectCloud, newEffects);
		tags.setBoolean(MobsPropertiesRandomness.RESOURCE_PREFIX + "checked", true);
	}
}