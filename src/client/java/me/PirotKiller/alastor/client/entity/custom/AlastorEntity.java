package me.PirotKiller.alastor.client.entity.custom;

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
import java.util.EnumSet; // Required for the new Goal

public class AlastorEntity extends AnimalEntity {
    // Animation States
    public final AnimationState idleAnimationState = new AnimationState();
    public int idleAnimationTimeout = 0;

    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;

    // Combat Variables
    private int liftAttackCooldown = 0;
    private int slamTimer = 0;
    private LivingEntity slamTarget = null;

    public AlastorEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));

        // --- PRIORITY 1: Ranged Lift Attack ---
        // Attempts to cast the spell if cooldown is ready and target is in range
        this.goalSelector.add(1, new LiftAttackGoal(this));

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
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 18)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 25); // Increased follow range for ranged attacks
    }

    // --- LOGIC: Handle Client Animation Signal ---
    @Override
    public void handleStatus(byte status) {
        if (status == 4) {
            this.attackAnimationTimeout = 20;
            this.attackAnimationState.start(this.age);
        } else {
            super.handleStatus(status);
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 40;
            this.idleAnimationState.start(this.age);
        } else {
            --this.idleAnimationTimeout;
        }

        if (this.getVelocity().horizontalLengthSquared() > 0.001d) {
            this.idleAnimationState.stop();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            this.setupAnimationStates();
        } else {
            // --- SERVER SIDE: Handle Cooldowns & Slam Logic ---
            if (this.liftAttackCooldown > 0) this.liftAttackCooldown--;

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
            if (target == null || !target.isAlive()) return false;
            this.target = target;

            // Start if Cooldown is 0 AND Distance is less than 15 blocks (225 sq blocks)
            return this.mob.liftAttackCooldown <= 0 && this.mob.squaredDistanceTo(target) < 225;
        }

        @Override
        public void start() {
            this.animationTime = 20; // 1 second casting time (matches animation)
            this.mob.liftAttackCooldown = 200; // Reset cooldown (10 seconds)

            // 1. Play Animation
            this.mob.getWorld().sendEntityStatus(this.mob, (byte) 4);

            // 2. Launch the Target (Range Logic)
            this.mob.slamTarget = this.target;
            this.mob.slamTimer = 12; // Time until slam
            this.target.setVelocity(0, 1.5, 0);
            this.target.velocityModified = true;
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
    public boolean isBreedingItem(ItemStack stack) { return false; }
    @Nullable @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) { return null; }
}