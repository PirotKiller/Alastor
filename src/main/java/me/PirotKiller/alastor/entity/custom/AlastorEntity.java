package me.PirotKiller.alastor.entity.custom;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public class AlastorEntity extends AnimalEntity {
    // Animation States
    public final AnimationState idleAnimationState = new AnimationState();
    public int idleAnimationTimeout = 0;

    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;

    public final AnimationState rayAttackAnimationState = new AnimationState();
    public int rayAttackAnimationTimeout = 0;

    public final AnimationState transformationAnimationState = new AnimationState();
    public int transformationAnimationTimeout = 0;

    public final AnimationState throwAnimationState = new AnimationState();
    public int throwAnimationTimeout = 0;

    private static final TrackedData<Boolean> IS_DEMON = DataTracker.registerData(AlastorEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    // Combat Variables
    private int liftAttackCooldown = 0;
    private int rayAttackCooldown = 0;
    private int throwAttackCooldown = 0;
    private int teleportCooldown = 0;
    private int minionSpawnCooldown = 0;
    private int shockwaveCooldown = 0;
    private int slamTimer = 0;
    private LivingEntity slamTarget = null;

    private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.RED,
            BossBar.Style.PROGRESS);

    public AlastorEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_DEMON, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));

        // --- PRIORITY 1: Ranged Lift Attack ---
        // Attempts to cast the spell if cooldown is ready and target is in range
        this.goalSelector.add(1, new LiftAttackGoal(this));
        this.goalSelector.add(1, new RayAttackGoal(this));
        this.goalSelector.add(1, new ThrowBlockGoal(this));

        // --- PRIORITY 2: Melee Attack ---
        // Only runs if the Lift Attack is on cooldown
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, false));

        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F)); // Increased look range
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40); // Increased follow range for ranged attacks
    }

    // --- LOGIC: Handle Client Animation Signal ---
    @Override
    public void handleStatus(byte status) {
        if (status == 4) {
            this.attackAnimationTimeout = 40; // FIXED: Changed from 20 to 40
            this.attackAnimationState.start(this.age);
        } else if (status == 5) {
            this.rayAttackAnimationTimeout = 60; // 3 seconds
            this.rayAttackAnimationState.start(this.age);
        } else if (status == 6) {
            this.transformationAnimationTimeout = 60; // 3 seconds
            this.transformationAnimationState.start(this.age);
        } else if (status == 7) {
            this.throwAnimationTimeout = 30; // 1.5 seconds
            this.throwAnimationState.start(this.age);
        } else {
            super.handleStatus(status);
        }
    }

    private void setupAnimationStates() {
        // 1. Handle Attack Timer (Client Side)
        if (this.attackAnimationTimeout > 0) {
            this.attackAnimationTimeout--;
        }
        if (this.rayAttackAnimationTimeout > 0) {
            this.rayAttackAnimationTimeout--;
        }
        if (this.transformationAnimationTimeout > 0) {
            this.transformationAnimationTimeout--;
        }
        if (this.throwAnimationTimeout > 0) {
            this.throwAnimationTimeout--;
        }

        // 2. PRIORITY: If attacking, STOP the idle animation and RETURN.
        // This ensures they never overlap.
        // 2. PRIORITY: If attacking, STOP the idle animation and RETURN.
        // This ensures they never overlap.
        // if (this.attackAnimationTimeout > 0) {
        // this.idleAnimationState.stop();
        // return;
        // }

        // 3. Normal Idle Logic (Only runs if NOT attacking)
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 40;
            this.idleAnimationState.start(this.age);
        } else {
            --this.idleAnimationTimeout;
        }

        // 4. Stop Idle if Walking
        if (this.getVelocity().horizontalLengthSquared() > 0.001d) {
            this.idleAnimationState.stop();
        }
    }

    @Override
    public void mobTick() {
        super.mobTick();
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            this.setupAnimationStates();
        } else {
            // --- SERVER SIDE: Handle Cooldowns & Slam Logic ---
            if (this.liftAttackCooldown > 0)
                this.liftAttackCooldown--;
            if (this.rayAttackCooldown > 0)
                this.rayAttackCooldown--;
            if (this.throwAttackCooldown > 0)
                this.throwAttackCooldown--;
            if (this.teleportCooldown > 0)
                this.teleportCooldown--;
            if (this.minionSpawnCooldown > 0)
                this.minionSpawnCooldown--;
            if (this.shockwaveCooldown > 0)
                this.shockwaveCooldown--;

            // --- ABILITY LOGIC ---
            this.tickTeleport();
            this.tickMinionSummon();
            this.tickShockwave();

            if (this.slamTimer > 0) {
                this.slamTimer--;
                if (this.slamTimer == 0 && this.slamTarget != null && this.slamTarget.isAlive()) {
                    // SLAM DOWN
                    this.slamTarget.setVelocity(0, -5.0, 0);
                    this.slamTarget.velocityModified = true;
                }
            }
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean success = super.tryAttack(target);

        if (success && target instanceof LivingEntity livingTarget && this.liftAttackCooldown <= 0) {
            // ... existing launch logic ...

            // Stop the mob from sliding while attacking
            this.getNavigation().stop();

            // Apply Weakness & Nausea for 5 seconds on Melee Hit
            if (target instanceof LivingEntity livingtarget) {
                livingtarget.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
                livingtarget.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
            }
        }
        return success;
    }

    public boolean isDemon() {
        return this.dataTracker.get(IS_DEMON);
    }

    public void setDemon(boolean demon) {
        this.dataTracker.set(IS_DEMON, demon);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.isDemon() && this.getHealth() - amount <= 0) {
            this.transform();
            return false; // Cancel the damage/death
        }
        return super.damage(source, amount);
    }

    private void transform() {
        this.setDemon(true);
        this.setHealth(this.getMaxHealth());
        this.bossBar.setName(Text.of("Demon Alastor"));
        this.getWorld().sendEntityStatus(this, (byte) 6); // 6 for transformation

        // Transformation Effects
        this.playSound(SoundEvents.ENTITY_WITHER_SPAWN, 1.0F, 1.0F);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0, 0,
                    0, 0);
        }
    }

    @Override
    public int getXpToDrop() {
        return 8670; // XP required for Level 0 -> 60
    }

    // --- NEW ABILITIES ---

    private void tickTeleport() {
        LivingEntity target = this.getTarget();
        if (target != null && this.teleportCooldown <= 0) {
            double distanceSq = this.squaredDistanceTo(target);
            // Teleport if far away (> 20 blocks) OR random chance when close
            if (distanceSq > 400 || (this.getRandom().nextFloat() < 0.005F && distanceSq > 25)) {
                this.teleportToTarget(target);
            }
        }
    }

    private void teleportToTarget(LivingEntity target) {
        double x = target.getX() + (this.getRandom().nextDouble() - 0.5D) * 8.0D;
        double y = target.getY();
        double z = target.getZ() + (this.getRandom().nextDouble() - 0.5D) * 8.0D;

        this.teleport(x, y, z, true);
        this.teleportCooldown = 300; // 15 seconds
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                this.getSoundCategory(), 1.0F, 1.0F);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1, this.getZ(), 20, 0.5, 1.0,
                    0.5, 0.1);
        }
    }

    private void tickMinionSummon() {
        if (this.isDemon() && this.minionSpawnCooldown <= 0 && this.getTarget() != null) {
            // Summon 3 Vexes
            for (int i = 0; i < 3; i++) {
                // Use EntityType.VEX directly or create a new VexEntity
                net.minecraft.entity.mob.VexEntity vex = net.minecraft.entity.EntityType.VEX.create(this.getWorld());
                if (vex != null) {
                    vex.refreshPositionAndAngles(this.getX(), this.getY() + 1, this.getZ(), 0, 0);
                    vex.setOwner(this);
                    vex.setTarget(this.getTarget());
                    // vex.setBounds(this.getBlockPos()); // Optional: limit wander range
                    // vex.setLifeTicks(1200); // Optional: limited life
                    this.getWorld().spawnEntity(vex);
                }
            }
            this.minionSpawnCooldown = 900; // 45 seconds
            this.playSound(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.EFFECT, this.getX(), this.getY() + 2, this.getZ(), 10, 0.5,
                        0.5,
                        0.5, 0.1);
            }
        }
    }

    private void tickShockwave() {
        LivingEntity target = this.getTarget();
        if (target != null && this.shockwaveCooldown <= 0 && this.squaredDistanceTo(target) < 16) { // < 4 blocks
            // Only trigger if hurt recently? Or just proximity? Let's do proximity + random
            // chance or just proximity check
            // For "Get Off Me", let's check if we took damage recently?
            // Actually, let's just do it if player is too close for too long.
            // For simplicity, just check distance and cooldown.

            this.shockwaveCooldown = 200; // 10 seconds

            // Effect
            this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0F, 1.0F);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0,
                        0, 0, 0);
            }

            // Damage and Knockback
            for (Entity entity : this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(5.0D))) {
                if (entity instanceof LivingEntity livingEntity && livingEntity != this) {
                    livingEntity.damage(this.getDamageSources().explosion(this, null), 6.0F);
                    double d0 = livingEntity.getX() - this.getX();
                    double d1 = livingEntity.getZ() - this.getZ();
                    livingEntity.takeKnockback(1.5F, -d0, -d1);
                }
            }
        }
    }

    // --- CUSTOM GOAL CLASS ---
    // This handles the "Ranged" behavior
    class LiftAttackGoal extends Goal {
        private final AlastorEntity mob;
        private LivingEntity target;
        private int animationTime = 0;

        public LiftAttackGoal(AlastorEntity mob) {
            this.mob = mob;
            // Mutex controls: Stop moving (MOVE) and override looking (LOOK) while casting
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive())
                return false;
            this.target = target;

            // Start if Cooldown is 0 AND Distance is less than 15 blocks (225 sq blocks)
            return this.mob.liftAttackCooldown <= 0 && this.mob.squaredDistanceTo(target) < 225;
        }

        @Override
        public void start() {
            this.animationTime = 40; // FIXED: Changed from 20 to 40
            this.mob.liftAttackCooldown = 200; // Reset cooldown (10 seconds)

            // 1. Play Animation
            this.mob.getWorld().sendEntityStatus(this.mob, (byte) 4);

            // Sound Effect
            this.mob.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.0F);

            // 2. Launch the Target (Range Logic)
            this.mob.slamTarget = this.target;
            this.mob.slamTimer = 12; // Time until slam
            this.target.setVelocity(0, 2.5, 0);
            this.target.velocityModified = true;

            // Apply Slowness for 5 seconds
            this.target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));

            // Particles on Target
            if (this.mob.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.EFFECT, this.target.getX(), this.target.getY(),
                        this.target.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }

        @Override
        public boolean shouldContinue() {
            return this.animationTime > 0;
        }

        @Override
        public void tick() {
            this.animationTime--;

            // Stop moving and face the target while casting
            this.mob.getNavigation().stop();
            this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);
        }
    }

    // Standard Breeding Methods
    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    // --- RAY ATTACK GOAL ---
    // --- RAY ATTACK GOAL ---
    class RayAttackGoal extends Goal {
        private final AlastorEntity mob;
        private LivingEntity target;
        private int animationTime = 0;

        public RayAttackGoal(AlastorEntity mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive())
                return false;
            this.target = target;

            // Use the entity's main cooldown variable
            return this.mob.rayAttackCooldown <= 0 && this.mob.getRandom().nextInt(40) == 0
                    && this.mob.squaredDistanceTo(target) < 400;
        }

        @Override
        public void start() {
            this.animationTime = 60; // 3 seconds
            this.mob.rayAttackCooldown = 100; // 5 seconds cooldown (Reduced for testing)
            this.mob.getWorld().sendEntityStatus(this.mob, (byte) 5);

            // Charge Sound
            this.mob.playSound(SoundEvents.BLOCK_BEACON_AMBIENT, 2.0F, 2.0F);
        }

        @Override
        public boolean shouldContinue() {
            return this.animationTime > 0;
        }

        @Override
        public void tick() {
            this.animationTime--;
            this.mob.getNavigation().stop();
            this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);

            if (this.animationTime == 30) { // Fire halfway through
                double d0 = this.mob.getX();
                double d1 = this.mob.getEyeY();
                double d2 = this.mob.getZ();
                double d3 = this.target.getX() - d0;
                double d4 = this.target.getEyeY() - d1;
                double d5 = this.target.getZ() - d2;
                double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);

                d3 /= d6;
                d4 /= d6;
                d5 /= d6;

                double step = 0.5;
                for (int i = 0; i < d6 / step; ++i) {
                    double x = d0 + d3 * i * step;
                    double y = d1 + d4 * i * step;
                    double z = d2 + d5 * i * step;
                    // Redstone Dust Particle: Color (Red), Scale (1.0)
                    if (this.mob.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(new DustParticleEffect(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f), x, y,
                                z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }

                this.target.damage(this.mob.getDamageSources().magic(), 10.0F);

                // Apply Blindness & Wither for 5 seconds
                this.target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
                this.target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));

                // Impact Sound & Particles
                this.mob.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0F, 1.0F);
                if (this.mob.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.target.getX(), this.target.getY(),
                            this.target.getZ(), 1, 0, 0, 0, 0);
                }
            }
        }
    }

    // --- THROW BLOCK GOAL ---
    class ThrowBlockGoal extends Goal {
        private final AlastorEntity mob;
        private LivingEntity target;
        private int animationTime = 0;

        public ThrowBlockGoal(AlastorEntity mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive())
                return false;
            this.target = target;

            return this.mob.isDemon() && this.mob.throwAttackCooldown <= 0 && this.mob.squaredDistanceTo(target) < 256
                    && this.mob.squaredDistanceTo(target) > 25;
        }

        @Override
        public void start() {
            this.animationTime = 30; // 1.5 seconds
            this.mob.throwAttackCooldown = 100; // 5 seconds
            this.mob.getWorld().sendEntityStatus(this.mob, (byte) 7);

            // Throw Sound
            this.mob.playSound(SoundEvents.ENTITY_GHAST_SHOOT, 1.0F, 0.5F);
        }

        @Override
        public boolean shouldContinue() {
            return this.animationTime > 0;
        }

        @Override
        public void tick() {
            this.animationTime--;
            this.mob.getNavigation().stop();
            this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);

            if (this.animationTime == 10) { // Throw point
                if (this.mob.getWorld() instanceof ServerWorld serverWorld) {
                    for (int i = 0; i < 5; i++) { // Throw 5 blocks
                        FallingBlockEntity block = FallingBlockEntity.spawnFromBlock(serverWorld,
                                this.mob.getBlockPos().up(2), Blocks.COBBLESTONE.getDefaultState());
                        double d0 = this.target.getX() - this.mob.getX();
                        double d1 = this.target.getBodyY(0.3333333333333333D) - block.getY();
                        double d2 = this.target.getZ() - this.mob.getZ();
                        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

                        // Add random spread
                        double spreadX = (this.mob.getRandom().nextDouble() - 0.5) * 0.5;
                        double spreadY = (this.mob.getRandom().nextDouble() - 0.5) * 0.5;
                        double spreadZ = (this.mob.getRandom().nextDouble() - 0.5) * 0.5;

                        // Normalized Velocity Calculation
                        double speed = 1.5D;
                        double arc = 0.2D; // Add 20% of distance to height

                        // Calculate base vector
                        double vx = d0;
                        double vy = d1 + d3 * arc;
                        double vz = d2;

                        // Normalize
                        double magnitude = Math.sqrt(vx * vx + vy * vy + vz * vz);
                        vx /= magnitude;
                        vy /= magnitude;
                        vz /= magnitude;

                        // Apply Speed
                        vx *= speed;
                        vy *= speed;
                        vz *= speed;

                        // Apply Spread
                        vx += spreadX;
                        vy += spreadY;
                        vz += spreadZ;

                        block.setVelocity(vx, vy, vz);
                        block.setHurtEntities(7.0F, 10); // Damage, Max Damage
                    }
                }
            }
        }
    }
}
