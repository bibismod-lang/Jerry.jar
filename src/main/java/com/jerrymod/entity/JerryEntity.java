package com.jerrymod.entity;

import com.jerrymod.JerryMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Jerry: copia del lobo/perro de Minecraft + felicidad, ojos, trucos y transformación. */
public class JerryEntity extends WolfEntity {
    public static final int TRICK_NONE = 0, TRICK_PAW = 1, TRICK_SPIN = 2;

    private static final int DECAY_TICKS = 200;     // cada 10 s pierde 1 de felicidad
    private static final int MONSTER_AT = 5;        // felicidad <= 5  ->  monstruo
    private static final int WARN_AT = 15;

    private static final TrackedData<Integer> HAPPINESS = DataTracker.registerData(JerryEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> EYE_TYPE = DataTracker.registerData(JerryEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TRICK = DataTracker.registerData(JerryEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private int trickTicks;
    private int lastPet;
    private boolean warned;

    public JerryEntity(EntityType<JerryEntity> type, World world) {
        super(type, world);
        this.dataTracker.set(EYE_TYPE, this.random.nextInt(4));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HAPPINESS, 80);
        this.dataTracker.startTracking(EYE_TYPE, 0);
        this.dataTracker.startTracking(TRICK, TRICK_NONE);
    }

    public int getHappiness() { return this.dataTracker.get(HAPPINESS); }
    public int getEyeType() { return this.dataTracker.get(EYE_TYPE); }
    public int getTrick() { return this.dataTracker.get(TRICK); }

    public void addHappiness(int n) {
        int v = Math.max(0, Math.min(100, getHappiness() + n));
        this.dataTracker.set(HAPPINESS, v);
        if (v > WARN_AT + 10) warned = false;
    }

    public void doTrick(int trick) {
        this.dataTracker.set(TRICK, trick);
        this.trickTicks = trick == TRICK_PAW ? 60 : 12; // 12 ticks x 30° = 360°
    }

    public void hearts(int count) {
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.HEART, getX(), getBodyY(0.9), getZ(), count, 0.4, 0.3, 0.4, 0.02);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;

        if (age % DECAY_TICKS == 0) addHappiness(-1);

        int h = getHappiness();
        if (h <= WARN_AT && !warned) {
            warned = true;
            playSound(SoundEvents.ENTITY_WOLF_GROWL, 1.5f, 0.5f);
            PlayerEntity near = getWorld().getClosestPlayer(this, 16);
            if (near != null) near.sendMessage(Text.literal("§cJerry te mira fijamente... algo anda mal."), true);
        }

        if (getTrick() != TRICK_NONE) {
            getNavigation().stop();
            if (getTrick() == TRICK_SPIN) {
                float y = getYaw() + 30f;
                setYaw(y); setBodyYaw(y); setHeadYaw(y);
            }
            if (--trickTicks <= 0) this.dataTracker.set(TRICK, TRICK_NONE);
        }

        if (h <= MONSTER_AT) transform();
    }

    private void transform() {
        JerryMonsterEntity m = JerryMod.MONSTER.create(getWorld());
        if (m == null) return;
        m.refreshPositionAndAngles(getX(), getY(), getZ(), getYaw(), 0f);
        getWorld().spawnEntity(m);
        getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2f, 0.6f);
        discard();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        boolean client = getWorld().isClient;

        if (!client && !isTamed()) setOwner(player); // el primero que lo toca es su dueño

        // Ojo de araña: cambia el tipo de ojos (0..3)
        if (stack.isOf(Items.SPIDER_EYE)) {
            if (!client) this.dataTracker.set(EYE_TYPE, (getEyeType() + 1) % 4);
            return ActionResult.success(client);
        }
        // Hueso: alimentar
        if (stack.isOf(Items.BONE)) {
            if (!client) {
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                addHappiness(15);
                hearts(4);
                playSound(SoundEvents.ENTITY_WOLF_PANT, 1f, 1f);
            }
            return ActionResult.success(client);
        }
        // Acariciar: agacharse + click derecho con la mano vacía
        if (player.isSneaking() && stack.isEmpty()) {
            if (!client && age - lastPet > 10) {
                lastPet = age;
                addHappiness(6);
                hearts(7);
            }
            return ActionResult.success(client);
        }
        return super.interactMob(player, hand); // click derecho normal = sentarse/levantarse como un perro
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean r = super.damage(source, amount);
        if (r && !getWorld().isClient && source.getAttacker() instanceof PlayerEntity) addHappiness(-25);
        return r;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Happiness", getHappiness());
        nbt.putInt("EyeType", getEyeType());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Happiness")) this.dataTracker.set(HAPPINESS, nbt.getInt("Happiness"));
        if (nbt.contains("EyeType")) this.dataTracker.set(EYE_TYPE, nbt.getInt("EyeType"));
    }
}
