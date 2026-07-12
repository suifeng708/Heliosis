package myau.module.modules.combat;

import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.event.types.Priority;
import myau.events.AttackEvent;
import myau.events.LoadWorldEvent;
import myau.events.PacketEvent;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import myau.mixin.IAccessorRenderManager;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.modules.player.Scaffold;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings.GameType;

import java.awt.Color;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@ModuleInfo(name = "Backtrack", enabled = "false", hidden = "false", description = "", category = Category.COMBAT)
public class BackTrack extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final long COMBAT_LOCK_MS = 3000L;
    private static final long RESTART_DELAY_MS = 50L;
    private static final int MAX_QUEUED_PACKETS = 512;
    private static final double POSITION_EPSILON_SQ = 1.0E-4;
    private static BackTrack instance;

    public final FloatProperty range;
    public final BooleanProperty adaptive;
    public final IntProperty normalDelay;
    public final IntProperty adaptiveDelay;
    public final BooleanProperty releaseOnHit;
    public final BooleanProperty interruptLagRange;
    public final BooleanProperty players;
    public final BooleanProperty teams;
    public final BooleanProperty botCheck;
    public final BooleanProperty esp;

    private final ConcurrentLinkedQueue<QueuedPacket> incomingQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean releaseScheduled = new AtomicBoolean(false);

    private volatile Vec3 trackedPosition;
    private volatile AxisAlignedBB realAABB;
    private volatile EntityLivingBase target;
    private EntityLivingBase lastAttacked;
    private long lastAttackTime;
    private long backtrackStartTime;
    private long nextBacktrackTime;
    private boolean lagRangeInterrupted;

    public BackTrack() {
        instance = this;
        this.range = new FloatProperty("range", 3.5F, 1.0F, 8.0F);
        this.adaptive = new BooleanProperty("adaptive", true);
        this.normalDelay = new IntProperty("normal-delay", 100, 50, 1000, () -> !this.adaptive.getValue());
        this.adaptiveDelay = new IntProperty("adaptive-delay", 100, 50, 1000, this.adaptive::getValue);
        this.releaseOnHit = new BooleanProperty("release-on-hit", true);
        this.interruptLagRange = new BooleanProperty("interrupt-lagrange", true);
        this.players = new BooleanProperty("players", true);
        this.teams = new BooleanProperty("teams", true);
        this.botCheck = new BooleanProperty("bot-check", true);
        this.esp = new BooleanProperty("esp", true);
    }

    @Override
    public void onEnabled() {
        discardIncoming();
        clearTargetState();
        lagRangeInterrupted = false;
    }

    @Override
    public void onDisabled() {
        restoreLagRange();
        releaseIncoming();
        clearTargetState();
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        restoreLagRange();
        discardIncoming();
        clearTargetState();
    }

    private int currentDelay() {
        return adaptive.getValue() ? adaptiveDelay.getValue() : normalDelay.getValue();
    }

    private LagRange getLagRange() {
        Module module = Myau.moduleManager.getModule(LagRange.class);
        return module instanceof LagRange ? (LagRange) module : null;
    }

    private void setLagRangeEnabled(boolean enabled) {
        LagRange lagRange = getLagRange();
        if (lagRange == null) return;

        if (enabled && lagRangeInterrupted) {
            lagRangeInterrupted = false;
            lagRange.setEnabled(true);
        } else if (!enabled && interruptLagRange.getValue() && !lagRangeInterrupted && lagRange.isEnabled()) {
            lagRangeInterrupted = true;
            lagRange.setEnabled(false);
        }
    }

    private void restoreLagRange() {
        setLagRangeEnabled(true);
    }

    private boolean isInCombat() {
        return lastAttacked != null
                && lastAttacked == target
                && !lastAttacked.isDead
                && System.currentTimeMillis() - lastAttackTime <= COMBAT_LOCK_MS;
    }

    private boolean hasValidContext() {
        if (mc.thePlayer == null || mc.theWorld == null || mc.playerController == null) return false;
        if (mc.thePlayer.isDead || mc.thePlayer.getHealth() <= 0.0F || mc.thePlayer.ticksExisted <= 20) return false;
        if (mc.playerController.getCurrentGameType() == GameType.SPECTATOR) return false;
        if (target == null || !isValidTarget(target) || !isInCombat()) return false;
        Module scaffold = Myau.moduleManager.getModule(Scaffold.class);
        return scaffold == null || !scaffold.isEnabled();
    }

    private boolean shouldStartBacktracking() {
        if (System.currentTimeMillis() < nextBacktrackTime || !hasValidContext()) return false;
        if (trackedPosition == null || realAABB == null || !hasPositionDifference()) return false;

        double realDistance = distanceToBox(realAABB);
        if (realDistance > range.getValue()) return false;
        if (!adaptive.getValue()) return true;

        double clientDistance = distanceToBox(target.getEntityBoundingBox());
        return realDistance + 0.01 >= clientDistance;
    }

    private boolean shouldContinueBacktracking() {
        if (!hasValidContext() || trackedPosition == null || realAABB == null) return false;
        double realDistance = distanceToBox(realAABB);
        if (realDistance > range.getValue()) return false;
        if (!adaptive.getValue()) return true;
        double clientDistance = distanceToBox(target.getEntityBoundingBox());
        return hasPositionDifference() && realDistance + 0.01 >= clientDistance;
    }

    private boolean hasPositionDifference() {
        if (target == null || trackedPosition == null) return false;
        double dx = trackedPosition.xCoord - target.posX;
        double dy = trackedPosition.yCoord - target.posY;
        double dz = trackedPosition.zCoord - target.posZ;
        return dx * dx + dy * dy + dz * dz > POSITION_EPSILON_SQ;
    }

    private double distanceToBox(AxisAlignedBB box) {
        float border = target == null ? 0.0F : target.getCollisionBorderSize();
        return RotationUtil.distanceToBox(box.expand(border, border, border));
    }

    private AxisAlignedBB createTrackedBox(Vec3 position) {
        if (target == null || position == null) return null;
        double halfWidth = target.width / 2.0;
        return new AxisAlignedBB(
                position.xCoord - halfWidth,
                position.yCoord,
                position.zCoord - halfWidth,
                position.xCoord + halfWidth,
                position.yCoord + target.height,
                position.zCoord + halfWidth
        );
    }

    private boolean updateRealPosition(Packet<?> packet) {
        if (target == null) return false;

        if (packet instanceof S14PacketEntity) {
            S14PacketEntity movement = (S14PacketEntity) packet;
            if (movement.getEntity(mc.theWorld) != target) return false;
            Vec3 base = trackedPosition != null
                    ? trackedPosition
                    : new Vec3(target.posX, target.posY, target.posZ);
            trackedPosition = base.addVector(
                    movement.func_149062_c() / 32.0,
                    movement.func_149061_d() / 32.0,
                    movement.func_149064_e() / 32.0
            );
        } else if (packet instanceof S18PacketEntityTeleport) {
            S18PacketEntityTeleport teleport = (S18PacketEntityTeleport) packet;
            if (teleport.getEntityId() != target.getEntityId()) return false;
            trackedPosition = new Vec3(
                    teleport.getX() / 32.0,
                    teleport.getY() / 32.0,
                    teleport.getZ() / 32.0
            );
        } else {
            return false;
        }

        realAABB = createTrackedBox(trackedPosition);
        return true;
    }

    private boolean shouldPassThrough(Packet<?> packet) {
        if (packet instanceof S02PacketChat) return true;
        if (packet instanceof S29PacketSoundEffect) {
            String sound = ((S29PacketSoundEffect) packet).getSoundName();
            return sound != null && (sound.contains("game.player.hurt") || sound.contains("game.player.die"));
        }
        return false;
    }

    private boolean shouldFlushImmediately(Packet<?> packet) {
        if (packet instanceof S01PacketJoinGame
                || packet instanceof S07PacketRespawn
                || packet instanceof S08PacketPlayerPosLook
                || packet instanceof S27PacketExplosion
                || packet instanceof S40PacketDisconnect) {
            return true;
        }
        if (packet instanceof S06PacketUpdateHealth) {
            return ((S06PacketUpdateHealth) packet).getHealth() <= 0.0F;
        }
        if (packet instanceof S12PacketEntityVelocity) {
            return ((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId();
        }
        if (packet instanceof S13PacketDestroyEntities && target != null) {
            for (int entityId : ((S13PacketDestroyEntities) packet).getEntityIDs()) {
                if (entityId == target.getEntityId()) return true;
            }
        }
        if (packet instanceof S19PacketEntityStatus) {
            S19PacketEntityStatus status = (S19PacketEntityStatus) packet;
            Entity entity = status.getEntity(mc.theWorld);
            if (status.getOpCode() == 2 && entity == mc.thePlayer) return true;
            return releaseOnHit.getValue() && status.getOpCode() == 2 && entity == target;
        }
        if (packet instanceof S1CPacketEntityMetadata && target != null) {
            S1CPacketEntityMetadata metadata = (S1CPacketEntityMetadata) packet;
            if (metadata.getEntityId() == target.getEntityId() && metadata.func_149376_c() != null) {
                for (WatchableObject value : metadata.func_149376_c()) {
                    if (value.getDataValueId() == 6 && value.getObject() instanceof Number
                            && ((Number) value.getObject()).doubleValue() <= 0.0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void enqueue(Packet<?> packet) {
        long now = System.currentTimeMillis();
        if (incomingQueue.isEmpty()) backtrackStartTime = now;
        incomingQueue.offer(new QueuedPacket(packet, now));
    }

    private void scheduleRelease() {
        if (!releaseScheduled.compareAndSet(false, true)) return;
        mc.addScheduledTask(() -> {
            releaseScheduled.set(false);
            releaseIncoming();
        });
    }

    private void releaseIncoming() {
        if (!mc.isCallingFromMinecraftThread()) {
            scheduleRelease();
            return;
        }
        releaseScheduled.set(false);
        if (mc.getNetHandler() == null) {
            discardIncoming();
            return;
        }
        QueuedPacket queued;
        while ((queued = incomingQueue.poll()) != null) {
            processPacketUnchecked(queued.packet);
        }
        finishRelease();
    }

    private void releaseExpired(long cutoff) {
        QueuedPacket queued;
        while ((queued = incomingQueue.peek()) != null && queued.timestamp <= cutoff) {
            incomingQueue.poll();
            processPacketUnchecked(queued.packet);
        }
        updateQueueStart();
    }

    private void releaseUntilInRange() {
        while (!incomingQueue.isEmpty()
                && target != null
                && distanceToBox(target.getEntityBoundingBox()) > range.getValue()) {
            QueuedPacket queued = incomingQueue.poll();
            if (queued != null) processPacketUnchecked(queued.packet);
        }
        updateQueueStart();
    }

    private void updateQueueStart() {
        QueuedPacket first = incomingQueue.peek();
        if (first == null) {
            finishRelease();
        } else {
            backtrackStartTime = first.timestamp;
        }
    }

    private void finishRelease() {
        backtrackStartTime = 0L;
        nextBacktrackTime = System.currentTimeMillis() + RESTART_DELAY_MS;
        restoreLagRange();
    }

    private void discardIncoming() {
        incomingQueue.clear();
        releaseScheduled.set(false);
        backtrackStartTime = 0L;
        nextBacktrackTime = 0L;
    }

    @SuppressWarnings("unchecked")
    private static <T extends INetHandler> void processPacketUnchecked(Packet<T> packet) {
        packet.processPacket((T) Minecraft.getMinecraft().getNetHandler());
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!isEnabled() || mc.thePlayer == null || !(event.getTarget() instanceof EntityLivingBase)) return;
        trackAttack((EntityLivingBase) event.getTarget());
    }

    private void trackAttack(EntityLivingBase attacked) {
        if (!isValidTarget(attacked)) return;
        lastAttacked = attacked;
        lastAttackTime = System.currentTimeMillis();
        if (target != attacked) switchTarget(attacked);
    }

    private void trackOutgoingAttack(PacketEvent event) {
        if (event.isCancelled() || !(event.getPacket() instanceof C02PacketUseEntity)) return;
        C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
        if (packet.getAction() != C02PacketUseEntity.Action.ATTACK) return;
        Entity entity = packet.getEntityFromWorld(mc.theWorld);
        if (entity instanceof EntityLivingBase) trackAttack((EntityLivingBase) entity);
    }

    @EventTarget(Priority.LOWEST)
    public void onPacket(PacketEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;
        if (event.getType() == EventType.SEND) {
            trackOutgoingAttack(event);
            return;
        }
        if (event.getType() != EventType.RECEIVE || event.isCancelled()) return;

        Module scaffold = Myau.moduleManager.getModule(Scaffold.class);
        if (scaffold != null && scaffold.isEnabled()) {
            if (!incomingQueue.isEmpty()) scheduleRelease();
            return;
        }
        handleIncoming(event);
    }

    private void handleIncoming(PacketEvent event) {
        Packet<?> packet = event.getPacket();
        boolean targetMovement = updateRealPosition(packet);

        if (incomingQueue.isEmpty()) {
            if (!targetMovement || !shouldStartBacktracking()) return;
        } else if (shouldPassThrough(packet)) {
            return;
        }

        enqueue(packet);
        event.setCancelled(true);

        if (shouldFlushImmediately(packet)
                || incomingQueue.size() >= MAX_QUEUED_PACKETS
                || !shouldContinueBacktracking()) {
            scheduleRelease();
        }
    }

    @EventTarget(Priority.LOW)
    public void onTick(TickEvent event) {
        if (!isEnabled() || event.getType() != EventType.PRE) return;
        if (mc.thePlayer == null || mc.theWorld == null) {
            discardIncoming();
            return;
        }
        tickPre();
    }

    private void tickPre() {
        EntityLivingBase newTarget = resolveTarget();
        if (newTarget != target) switchTarget(newTarget);

        if (target == null || !hasValidContext()) {
            if (!incomingQueue.isEmpty()) releaseIncoming();
            restoreLagRange();
            return;
        }

        if (trackedPosition == null) initializeTracking();
        if (incomingQueue.isEmpty()) {
            restoreLagRange();
            return;
        }

        if (mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime && mc.thePlayer.maxHurtTime > 0) {
            releaseIncoming();
            return;
        }
        if (!shouldContinueBacktracking()) {
            releaseIncoming();
            return;
        }

        if (distanceToBox(target.getEntityBoundingBox()) > range.getValue()) {
            releaseUntilInRange();
        }
        if (incomingQueue.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (adaptive.getValue()) {
            releaseExpired(now - adaptiveDelay.getValue());
        } else if (now - backtrackStartTime >= normalDelay.getValue()) {
            releaseIncoming();
        }

        if (!incomingQueue.isEmpty()) {
            setLagRangeEnabled(false);
        }
    }

    private void switchTarget(EntityLivingBase newTarget) {
        restoreLagRange();
        if (!incomingQueue.isEmpty()) releaseIncoming();
        target = newTarget;
        trackedPosition = null;
        realAABB = null;
        backtrackStartTime = 0L;
        if (target != null) initializeTracking();
    }

    private void initializeTracking() {
        if (target == null) return;
        trackedPosition = new Vec3(
                MathHelper.floor_double(target.posX * 32.0) / 32.0,
                MathHelper.floor_double(target.posY * 32.0) / 32.0,
                MathHelper.floor_double(target.posZ * 32.0) / 32.0
        );
        realAABB = createTrackedBox(trackedPosition);
    }

    private void clearTargetState() {
        target = null;
        trackedPosition = null;
        realAABB = null;
        lastAttacked = null;
        lastAttackTime = 0L;
        backtrackStartTime = 0L;
        nextBacktrackTime = 0L;
    }

    private EntityLivingBase resolveTarget() {
        KillAura killAura = (KillAura) Myau.moduleManager.modules.get(KillAura.class);
        if (killAura != null && killAura.isEnabled()) {
            EntityLivingBase auraTarget = killAura.getTarget();
            if (auraTarget != null && isValidTarget(auraTarget)) return auraTarget;
        }
        if (lastAttacked != null
                && isValidTarget(lastAttacked)
                && System.currentTimeMillis() - lastAttackTime <= COMBAT_LOCK_MS
                && distanceToBox(lastAttacked.getEntityBoundingBox()) <= range.getValue() * 2.0F) {
            return lastAttacked;
        }
        return null;
    }

    private boolean isValidTarget(EntityLivingBase entity) {
        if (entity == null || mc.theWorld == null || !mc.theWorld.loadedEntityList.contains(entity)) return false;
        if (entity == mc.thePlayer || entity == mc.thePlayer.ridingEntity) return false;
        if (entity == mc.getRenderViewEntity() || entity == mc.getRenderViewEntity().ridingEntity) return false;
        if (entity.isDead || entity.deathTime > 0 || entity.getHealth() <= 0.0F) return false;
        if (!(entity instanceof EntityPlayer) || !players.getValue()) return false;

        EntityPlayer player = (EntityPlayer) entity;
        if (TeamUtil.isFriend(player)) return false;
        if (teams.getValue() && TeamUtil.isSameTeam(player)) return false;
        return !botCheck.getValue() || !TeamUtil.isBot(player);
    }

    public static boolean runWithNearestTrackedDistance(Entity entity, Supplier<Boolean> action) {
        BackTrack backTrack = instance;
        if (backTrack == null
                || !backTrack.isEnabled()
                || backTrack.target != entity
                || backTrack.incomingQueue.isEmpty()
                || backTrack.trackedPosition == null
                || backTrack.realAABB == null) {
            return action.get();
        }

        float border = entity.getCollisionBorderSize();
        double currentDistance = RotationUtil.distanceToBox(
                entity.getEntityBoundingBox().expand(border, border, border)
        );
        double trackedDistance = RotationUtil.distanceToBox(
                backTrack.realAABB.expand(border, border, border)
        );
        if (trackedDistance >= currentDistance) return action.get();

        double posX = entity.posX;
        double posY = entity.posY;
        double posZ = entity.posZ;
        double prevPosX = entity.prevPosX;
        double prevPosY = entity.prevPosY;
        double prevPosZ = entity.prevPosZ;
        double lastTickPosX = entity.lastTickPosX;
        double lastTickPosY = entity.lastTickPosY;
        double lastTickPosZ = entity.lastTickPosZ;

        try {
            entity.setPosition(
                    backTrack.trackedPosition.xCoord,
                    backTrack.trackedPosition.yCoord,
                    backTrack.trackedPosition.zCoord
            );
            entity.prevPosX = entity.posX;
            entity.prevPosY = entity.posY;
            entity.prevPosZ = entity.posZ;
            entity.lastTickPosX = entity.posX;
            entity.lastTickPosY = entity.posY;
            entity.lastTickPosZ = entity.posZ;
            return action.get();
        } finally {
            entity.setPosition(posX, posY, posZ);
            entity.prevPosX = prevPosX;
            entity.prevPosY = prevPosY;
            entity.prevPosZ = prevPosZ;
            entity.lastTickPosX = lastTickPosX;
            entity.lastTickPosY = lastTickPosY;
            entity.lastTickPosZ = lastTickPosZ;
        }
    }

    @EventTarget(Priority.HIGH)
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !esp.getValue() || incomingQueue.isEmpty()) return;
        if (target == null || realAABB == null) return;

        AxisAlignedBB visual = target.getEntityBoundingBox();
        double dx = realAABB.minX - visual.minX;
        double dy = realAABB.minY - visual.minY;
        double dz = realAABB.minZ - visual.minZ;
        if (dx * dx + dy * dy + dz * dz <= POSITION_EPSILON_SQ) return;

        Color color = target instanceof EntityPlayer
                ? TeamUtil.getTeamColor((EntityPlayer) target, 1.0F)
                : new Color(255, 60, 60);
        IAccessorRenderManager renderManager = (IAccessorRenderManager) mc.getRenderManager();
        AxisAlignedBB aabb = realAABB.offset(
                -renderManager.getRenderPosX(),
                -renderManager.getRenderPosY(),
                -renderManager.getRenderPosZ()
        );
        RenderUtil.enableRenderState();
        RenderUtil.drawFilledBox(aabb, color.getRed(), color.getGreen(), color.getBlue());
        RenderUtil.disableRenderState();
    }

    @Override
    public String[] getSuffix() {
        return new String[]{currentDelay() + "ms"};
    }

    private static final class QueuedPacket {
        private final Packet<?> packet;
        private final long timestamp;

        private QueuedPacket(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }
}
