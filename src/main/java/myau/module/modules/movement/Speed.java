package myau.module.modules.movement;

import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.event.types.Priority;
import myau.events.JumpEvent;
import myau.events.LivingUpdateEvent;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.events.StrafeEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.mixin.IAccessorC03PacketPlayer;
import myau.mixin.IAccessorEntity;
import myau.mixin.IAccessorEntityPlayer;
import myau.mixin.IAccessorMinecraft;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.modules.combat.KillAura;
import myau.module.modules.player.Scaffold;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.MoveUtil;
import myau.util.RotationUtil;
import net.minecraft.block.BlockCarpet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "Speed", enabled = "false", hidden = "false", description = "Heliosis and LiquidBounce speed modes", category = Category.MOVEMENT)
public class Speed extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final int DEFAULT = 0;
    private static final int LEGIT = 1;
    private static final int TIMER = 2;
    private static final int NCP_BHOP = 3;
    private static final int NCP_FHOP = 4;
    private static final int SNCP_BHOP = 5;
    private static final int NCP_HOP = 6;
    private static final int NCP_YPORT = 7;
    private static final int UNCP_HOP = 8;
    private static final int UNCP_HOP_NEW = 9;
    private static final int AAC_HOP_3313 = 10;
    private static final int AAC_HOP_350 = 11;
    private static final int AAC_HOP_4 = 12;
    private static final int AAC_HOP_5 = 13;
    private static final int SPARTAN_YPORT = 14;
    private static final int SPECTRE_LOW_HOP = 15;
    private static final int SPECTRE_BHOP = 16;
    private static final int SPECTRE_ON_GROUND = 17;
    private static final int VERUS_HOP = 18;
    private static final int VERUS_FHOP = 19;
    private static final int VERUS_LOW_HOP = 20;
    private static final int VERUS_LOW_HOP_NEW = 21;
    private static final int VULCAN_HOP = 22;
    private static final int VULCAN_LOW_HOP = 23;
    private static final int VULCAN_GROUND_288 = 24;
    private static final int OLD_MATRIX_HOP = 25;
    private static final int MATRIX_HOP = 26;
    private static final int MATRIX_SLOW_HOP = 27;
    private static final int INTAVE_HOP_14 = 28;
    private static final int TELEPORT_CUBECRAFT = 29;
    private static final int HYPIXEL_HOP = 30;
    private static final int HYPIXEL_LOW_HOP = 31;
    private static final int BLOCKS_MC_HOP = 32;
    private static final int BOOST = 33;
    private static final int FRAME = 34;
    private static final int MI_JUMP = 35;
    private static final int ON_GROUND = 36;
    private static final int SLOW_HOP = 37;
    private static final int CUSTOM = 38;

    private final net.minecraft.util.Timer timer = ((IAccessorMinecraft) mc).getTimer();

    public final ModeProperty mode = new ModeProperty("mode", DEFAULT, new String[]{
            "default", "legit", "Timer",
            "NCPBHop", "NCPFHop", "SNCPBHop", "NCPHop", "NCPYPort", "UNCPHop", "UNCPHopNew",
            "AACHop3.3.13", "AACHop3.5.0", "AACHop4", "AACHop5",
            "SpartanYPort", "SpectreLowHop", "SpectreBHop", "SpectreOnGround",
            "VerusHop", "VerusFHop", "VerusLowHop", "VerusLowHopNew",
            "VulcanHop", "VulcanLowHop", "VulcanGround2.8.8",
            "OldMatrixHop", "MatrixHop", "MatrixSlowHop", "IntaveHop14",
            "TeleportCubeCraft", "HypixelHop", "HypixelLowHop", "BlocksMCHop",
            "Boost", "Frame", "MiJump", "OnGround", "SlowHop", "Custom"
    });

    // Original Heliosis settings.
    public final FloatProperty timerspeed = new FloatProperty("TimerSpeed", 2.5F, 1.0F, 100.0F,
            () -> isMode(TIMER));
    public final FloatProperty multiplier = new FloatProperty("multiplier", 1.0F, 0.0F, 10.0F);
    public final FloatProperty friction = new FloatProperty("friction", 1.0F, 0.0F, 10.0F);
    public final PercentProperty strafe = new PercentProperty("strafe", 0);
    public final BooleanProperty onlyJumping = new BooleanProperty("only-jumping", true);
    public final ModeProperty blockPlacements = new ModeProperty("block-placements", 1,
            new String[]{"LEGIT", "BLATANT"});

    // LiquidBounce Custom mode settings.
    public final FloatProperty customY = new FloatProperty("CustomY", 0.42F, 0.0F, 4.0F, () -> isMode(CUSTOM));
    public final FloatProperty customGroundStrafe = new FloatProperty("CustomGroundStrafe", 1.6F, 0.0F, 2.0F,
            () -> isMode(CUSTOM));
    public final FloatProperty customAirStrafe = new FloatProperty("CustomAirStrafe", 0.0F, 0.0F, 2.0F,
            () -> isMode(CUSTOM));
    public final FloatProperty customGroundTimer = new FloatProperty("CustomGroundTimer", 1.0F, 0.1F, 2.0F,
            () -> isMode(CUSTOM));
    public final IntProperty customAirTimerTick = new IntProperty("CustomAirTimerTick", 5, 1, 20,
            () -> isMode(CUSTOM));
    public final FloatProperty customAirTimer = new FloatProperty("CustomAirTimer", 1.0F, 0.1F, 2.0F,
            () -> isMode(CUSTOM));
    public final BooleanProperty resetXZ = new BooleanProperty("ResetXZ", false, () -> isMode(CUSTOM));
    public final BooleanProperty resetY = new BooleanProperty("ResetY", false, () -> isMode(CUSTOM));
    public final BooleanProperty notOnConsuming = new BooleanProperty("NotOnConsuming", false, () -> isMode(CUSTOM));
    public final BooleanProperty notOnFalling = new BooleanProperty("NotOnFalling", false, () -> isMode(CUSTOM));
    public final BooleanProperty notOnVoid = new BooleanProperty("NotOnVoid", true, () -> isMode(CUSTOM));

    public final FloatProperty cubecraftPortLength = new FloatProperty("CubeCraft-PortLength", 1.0F, 0.1F, 2.0F,
            () -> isMode(TELEPORT_CUBECRAFT));

    public final BooleanProperty intaveBoost = new BooleanProperty("Intave-Boost", true, () -> isMode(INTAVE_HOP_14));
    public final FloatProperty initialBoostMultiplier = new FloatProperty("Intave-InitialBoostMultiplier", 1.0F, 0.01F, 10.0F,
            () -> isMode(INTAVE_HOP_14) && intaveBoost.getValue());
    public final BooleanProperty intaveLowHop = new BooleanProperty("Intave-LowHop", true, () -> isMode(INTAVE_HOP_14));
    public final FloatProperty strafeStrength = new FloatProperty("Intave-StrafeStrength", 0.29F, 0.1F, 0.29F,
            () -> isMode(INTAVE_HOP_14));
    public final FloatProperty groundTimer = new FloatProperty("Intave-GroundTimer", 0.5F, 0.1F, 5.0F,
            () -> isMode(INTAVE_HOP_14));
    public final FloatProperty airTimer = new FloatProperty("Intave-AirTimer", 1.09F, 0.1F, 5.0F,
            () -> isMode(INTAVE_HOP_14));

    public final BooleanProperty pullDown = new BooleanProperty("UNCP-PullDown", true, () -> isMode(UNCP_HOP_NEW));
    public final IntProperty pullDownTick = new IntProperty("UNCP-OnTick", 5, 5, 9,
            () -> isMode(UNCP_HOP_NEW) && pullDown.getValue());
    public final BooleanProperty pullDownOnHurt = new BooleanProperty("UNCP-OnHurt", true,
            () -> isMode(UNCP_HOP_NEW) && pullDown.getValue());
    public final BooleanProperty uncpBoost = new BooleanProperty("UNCP-ShouldBoost", true, () -> isMode(UNCP_HOP_NEW));
    public final BooleanProperty uncpTimerBoost = new BooleanProperty("UNCP-TimerBoost", true, () -> isMode(UNCP_HOP_NEW));
    public final BooleanProperty uncpDamageBoost = new BooleanProperty("UNCP-DamageBoost", true, () -> isMode(UNCP_HOP_NEW));
    public final BooleanProperty uncpLowHop = new BooleanProperty("UNCP-LowHop", true, () -> isMode(UNCP_HOP_NEW));
    public final BooleanProperty uncpAirStrafe = new BooleanProperty("UNCP-AirStrafe", true, () -> isMode(UNCP_HOP_NEW));

    public final BooleanProperty matrixLowHop = new BooleanProperty("Matrix-LowHop", true,
            () -> isMode(MATRIX_HOP, MATRIX_SLOW_HOP));
    public final FloatProperty extraGroundBoost = new FloatProperty("Matrix-ExtraGroundBoost", 0.2F, 0.0F, 0.5F,
            () -> isMode(MATRIX_HOP, MATRIX_SLOW_HOP));

    public final BooleanProperty glide = new BooleanProperty("Hypixel-Glide", true, () -> isMode(HYPIXEL_LOW_HOP));

    public final BooleanProperty fullStrafe = new BooleanProperty("BlocksMC-FullStrafe", true,
            () -> isMode(BLOCKS_MC_HOP));
    public final BooleanProperty bmcLowHop = new BooleanProperty("BlocksMC-LowHop", true,
            () -> isMode(BLOCKS_MC_HOP));
    public final BooleanProperty bmcDamageBoost = new BooleanProperty("BlocksMC-DamageBoost", true,
            () -> isMode(BLOCKS_MC_HOP));
    public final BooleanProperty damageLowHop = new BooleanProperty("BlocksMC-DamageLowHop", false,
            () -> isMode(BLOCKS_MC_HOP));
    public final BooleanProperty safeY = new BooleanProperty("BlocksMC-SafeY", true,
            () -> isMode(BLOCKS_MC_HOP));

    private int activeMode = -1;
    private int airTicks;
    private boolean wasOnGround;
    private boolean boosting;
    private float cachedSilentYaw = Float.NaN;

    private int ncpLevel;
    private double ncpMoveSpeed;
    private double ncpLastDist;
    private int ncpTimerDelay;
    private int sncpLevel;
    private double sncpMoveSpeed;
    private double sncpLastDist;
    private int sncpTimerDelay;
    private int yPortJumps;
    private int uncpTick;
    private int uncpNewAirTick;
    private float modeSpeed;
    private int spartanAirMoves;
    private int spectreSpeedUp;
    private int boostMotionDelay;
    private float boostGround;
    private int frameMotionTicks;
    private int frameTimerTicks;
    private boolean frameMoved;
    private long lastCubePort;

    private boolean isMode(int... modes) {
        int current = mode.getValue();
        for (int candidate : modes) {
            if (current == candidate) return true;
        }
        return false;
    }

    private boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0.0F
                || mc.thePlayer.movementInput.moveStrafe != 0.0F);
    }

    private boolean isInLiquidOrClimbable(EntityPlayerSP player) {
        return player.isInWater() || player.isInLava() || player.isOnLadder()
                || ((IAccessorEntity) player).getIsInWeb();
    }

    private boolean canBoost() {
        if (mc.thePlayer == null) return false;
        Scaffold scaffold = (Scaffold) Myau.moduleManager.modules.get(Scaffold.class);
        return !scaffold.isEnabled() && MoveUtil.isForwardPressed()
                && mc.thePlayer.getFoodStats().getFoodLevel() > 6
                && !mc.thePlayer.isSneaking()
                && !mc.thePlayer.isInWater()
                && !mc.thePlayer.isInLava()
                && !((IAccessorEntity) mc.thePlayer).getIsInWeb();
    }

    private boolean canBoostLegit() {
        if (!canBoost()) return false;
        KillAura killAura = (KillAura) Myau.moduleManager.modules.get(KillAura.class);
        if (killAura.isEnabled() && killAura.target != null) return false;
        return !onlyJumping.getValue() || mc.gameSettings.keyBindJump.isKeyDown();
    }

    private boolean isOnlyForward() {
        return mc.gameSettings.keyBindForward.isKeyDown()
                && !mc.gameSettings.keyBindLeft.isKeyDown()
                && !mc.gameSettings.keyBindRight.isKeyDown()
                && !mc.gameSettings.keyBindBack.isKeyDown();
    }

    public boolean isBoosting() {
        return boosting && isMode(LEGIT) && blockPlacements.getValue() == 0;
    }

    public float getSilentYaw() {
        return cachedSilentYaw;
    }

    @Override
    public void onEnabled() {
        if (mc.thePlayer == null) return;
        activeMode = mode.getValue();
        resetModeState();
        timer.timerSpeed = 1.0F;
        enableMode(activeMode);
    }

    @Override
    public void onDisabled() {
        disableMode(activeMode);
        activeMode = -1;
        timer.timerSpeed = 1.0F;
        boosting = false;
        wasOnGround = false;
        cachedSilentYaw = Float.NaN;
        if (mc.thePlayer != null) {
            mc.thePlayer.jumpMovementFactor = 0.02F;
            ((IAccessorEntityPlayer) mc.thePlayer).setSpeedInAir(0.02F);
            mc.thePlayer.stepHeight = 0.6F;
        }
    }

    private void resetModeState() {
        airTicks = 0;
        ncpLevel = 1;
        ncpMoveSpeed = 0.2873;
        ncpLastDist = 0.0;
        ncpTimerDelay = 0;
        sncpLevel = 4;
        sncpMoveSpeed = 0.0;
        sncpLastDist = 0.0;
        sncpTimerDelay = 0;
        yPortJumps = 0;
        uncpTick = 0;
        uncpNewAirTick = 0;
        modeSpeed = 0.0F;
        spartanAirMoves = 0;
        spectreSpeedUp = 0;
        boostMotionDelay = 0;
        boostGround = 0.0F;
        frameMotionTicks = 0;
        frameTimerTicks = 0;
        frameMoved = false;
        lastCubePort = 0L;
    }

    private void enableMode(int selectedMode) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        if (selectedMode == NCP_BHOP) {
            ncpLevel = mc.theWorld.getCollidingBoundingBoxes(player,
                    player.getEntityBoundingBox().offset(0.0, player.motionY, 0.0)).isEmpty()
                    && !player.isCollidedVertically ? 4 : 1;
        } else if (selectedMode == NCP_FHOP) {
            timer.timerSpeed = 1.0866F;
        } else if (selectedMode == NCP_HOP) {
            timer.timerSpeed = 1.0865F;
        } else if (selectedMode == AAC_HOP_350 && player.onGround) {
            player.motionX = 0.0;
            player.motionZ = 0.0;
        } else if (selectedMode == CUSTOM) {
            if (resetXZ.getValue()) {
                player.motionX = 0.0;
                player.motionZ = 0.0;
            }
            if (resetY.getValue()) player.motionY = 0.0;
        }
    }

    private void disableMode(int selectedMode) {
        timer.timerSpeed = 1.0F;
        if (mc.thePlayer == null) return;
        if (selectedMode == NCP_FHOP || selectedMode == NCP_HOP || selectedMode == AAC_HOP_3313
                || selectedMode == AAC_HOP_350 || selectedMode == MATRIX_HOP || selectedMode == MATRIX_SLOW_HOP
                || selectedMode == OLD_MATRIX_HOP) {
            mc.thePlayer.jumpMovementFactor = 0.02F;
            ((IAccessorEntityPlayer) mc.thePlayer).setSpeedInAir(0.02F);
        }
    }

    @EventTarget(Priority.LOW)
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;

        if (event.getType() == EventType.POST) {
            if (isMode(AAC_HOP_350)) runAacHop350Post();
            return;
        }

        if (activeMode != mode.getValue()) {
            disableMode(activeMode);
            activeMode = mode.getValue();
            resetModeState();
            enableMode(activeMode);
        }

        if (isMode(LEGIT)) {
            runOriginalLegit(event);
            return;
        }
        if (isMode(TIMER)) {
            timer.timerSpeed = timerspeed.getValue();
            return;
        }
        if (isMode(DEFAULT)) {
            timer.timerSpeed = 1.0F;
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        if (player.isSneaking()) return;
        if (isMoving()) player.setSprinting(true);
        airTicks = player.onGround ? 0 : airTicks + 1;
        runLiquidBounceMode(activeMode);
    }

    private void runOriginalLegit(UpdateEvent event) {
        if (!canBoostLegit()) {
            boosting = false;
            cachedSilentYaw = Float.NaN;
            wasOnGround = mc.thePlayer.onGround;
            return;
        }

        boolean onGround = mc.thePlayer.onGround;
        if (!onGround && wasOnGround && isOnlyForward()) boosting = true;
        if (onGround) {
            boosting = false;
            cachedSilentYaw = Float.NaN;
        }

        if (boosting && isOnlyForward()) {
            float quantized = RotationUtil.quantizeAngle(event.getNewYaw() + 45.0F);
            cachedSilentYaw = quantized;
            event.setRotation(quantized, event.getNewPitch(), 1);
            event.setPervRotation(quantized, 1);
        } else if (boosting) {
            boosting = false;
            cachedSilentYaw = Float.NaN;
        }
        wasOnGround = onGround;
    }

    private void runLiquidBounceMode(int selectedMode) {
        switch (selectedMode) {
            case NCP_BHOP:
                runNcpBHop();
                break;
            case NCP_FHOP:
                runNcpFHop();
                break;
            case SNCP_BHOP:
                runSncpBHop();
                break;
            case NCP_HOP:
                runNcpHop();
                break;
            case NCP_YPORT:
                runNcpYPort();
                break;
            case UNCP_HOP:
                runUncpHop();
                break;
            case UNCP_HOP_NEW:
                runUncpHopNew();
                break;
            case AAC_HOP_3313:
                runAacHop3313();
                break;
            case AAC_HOP_4:
                runAacHop4();
                break;
            case AAC_HOP_5:
                runAacHop5();
                break;
            case SPARTAN_YPORT:
                runSpartanYPort();
                break;
            case SPECTRE_LOW_HOP:
                runSpectreHop(0.15);
                break;
            case SPECTRE_BHOP:
                runSpectreHop(0.44);
                break;
            case SPECTRE_ON_GROUND:
                runSpectreOnGround();
                break;
            case VERUS_HOP:
                runVerusHop();
                break;
            case VERUS_FHOP:
                runVerusFHop();
                break;
            case VERUS_LOW_HOP:
                runVerusLowHop(false);
                break;
            case VERUS_LOW_HOP_NEW:
                runVerusLowHop(true);
                break;
            case VULCAN_HOP:
                runVulcanHop();
                break;
            case VULCAN_LOW_HOP:
                runVulcanLowHop();
                break;
            case VULCAN_GROUND_288:
                runVulcanGround();
                break;
            case OLD_MATRIX_HOP:
                runOldMatrixHop();
                break;
            case MATRIX_HOP:
                runMatrixHop(false);
                break;
            case MATRIX_SLOW_HOP:
                runMatrixHop(true);
                break;
            case INTAVE_HOP_14:
                runIntaveHop();
                break;
            case TELEPORT_CUBECRAFT:
                runTeleportCubeCraft();
                break;
            case HYPIXEL_LOW_HOP:
                runHypixelLowHop();
                break;
            case BLOCKS_MC_HOP:
                runBlocksMcHop();
                break;
            case BOOST:
                runBoost();
                break;
            case FRAME:
                runFrame();
                break;
            case MI_JUMP:
                runMiJump();
                break;
            case ON_GROUND:
                runOnGround();
                break;
            case SLOW_HOP:
                runSlowHop();
                break;
            case CUSTOM:
                runCustom();
                break;
            default:
                break;
        }
    }

    private void runNcpBHop() {
        EntityPlayerSP player = mc.thePlayer;
        ncpLastDist = Math.hypot(player.posX - player.prevPosX, player.posZ - player.prevPosZ);
        ncpTimerDelay = (ncpTimerDelay + 1) % 5;
        timer.timerSpeed = ncpTimerDelay == 0 && isMoving() ? 1.3F : 1.0F;
        if (ncpTimerDelay == 0 && isMoving()) {
            player.motionX *= 1.0199999809265137;
            player.motionZ *= 1.0199999809265137;
        }
        if (player.onGround && isMoving()) ncpLevel = 2;
        if (round3(player.posY - (int) player.posY) == round3(0.138)) {
            player.motionY -= 0.08;
            player.setPosition(player.posX, player.posY - 0.09316090325960147, player.posZ);
        }
        if (ncpLevel == 1 && isMoving()) {
            ncpLevel = 2;
            ncpMoveSpeed = 1.35 * baseMoveSpeed() - 0.01;
        } else if (ncpLevel == 2) {
            ncpLevel = 3;
            player.motionY = 0.399399995803833;
            ncpMoveSpeed *= 2.149;
        } else if (ncpLevel == 3) {
            ncpLevel = 4;
            ncpMoveSpeed = ncpLastDist - 0.66 * (ncpLastDist - baseMoveSpeed());
        } else {
            if (collidesVertically(player)) ncpLevel = 1;
            ncpMoveSpeed = ncpLastDist - ncpLastDist / 159.0;
        }
        ncpMoveSpeed = Math.max(ncpMoveSpeed, baseMoveSpeed());
        strafe(ncpMoveSpeed, 1.0, true);
        player.stepHeight = 0.6F;
    }

    private void runSncpBHop() {
        EntityPlayerSP player = mc.thePlayer;
        sncpLastDist = Math.hypot(player.posX - player.prevPosX, player.posZ - player.prevPosZ);
        sncpTimerDelay = (sncpTimerDelay + 1) % 5;
        timer.timerSpeed = sncpTimerDelay == 0 && isMoving() ? 1.3F : 1.0F;
        if (sncpTimerDelay == 0 && isMoving()) {
            player.motionX *= 1.0199999809265137;
            player.motionZ *= 1.0199999809265137;
        }
        if (player.onGround && isMoving()) sncpLevel = 2;
        if (round3(player.posY - (int) player.posY) == round3(0.138)) {
            player.motionY -= 0.08;
            player.setPosition(player.posX, player.posY - 0.09316090325960147, player.posZ);
        }
        if (sncpLevel == 1 && isMoving()) {
            sncpLevel = 2;
            sncpMoveSpeed = 1.35 * baseMoveSpeed() - 0.01;
        } else if (sncpLevel == 2) {
            sncpLevel = 3;
            player.motionY = 0.399399995803833;
            sncpMoveSpeed *= 2.149;
        } else if (sncpLevel == 3) {
            sncpLevel = 4;
            sncpMoveSpeed = sncpLastDist - 0.66 * (sncpLastDist - baseMoveSpeed());
        } else if (sncpLevel == 88) {
            sncpMoveSpeed = baseMoveSpeed();
            sncpLastDist = 0.0;
            sncpLevel = 89;
        } else if (sncpLevel == 89) {
            if (collidesVertically(player)) sncpLevel = 1;
            sncpLastDist = 0.0;
            sncpMoveSpeed = baseMoveSpeed();
            return;
        } else {
            if (collidesVertically(player)) {
                sncpMoveSpeed = baseMoveSpeed();
                sncpLastDist = 0.0;
                sncpLevel = 88;
                return;
            }
            sncpMoveSpeed = sncpLastDist - sncpLastDist / 159.0;
        }
        sncpMoveSpeed = Math.max(sncpMoveSpeed, baseMoveSpeed());
        strafe(sncpMoveSpeed, 1.0, true);
        player.stepHeight = 0.6F;
    }

    private void runNcpFHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isMoving()) {
            if (player.onGround) {
                player.jump();
                player.motionX *= 1.01;
                player.motionZ *= 1.01;
                ((IAccessorEntityPlayer) player).setSpeedInAir(0.0223F);
            }
            player.motionY -= 0.00099999;
            strafe(MoveUtil.getSpeed(), 1.0, false);
        } else {
            stopXZ();
        }
    }

    private void runNcpHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isMoving()) {
            if (player.onGround) {
                player.jump();
                ((IAccessorEntityPlayer) player).setSpeedInAir(0.0223F);
            }
            strafe(MoveUtil.getSpeed(), 1.0, false);
        } else {
            stopXZ();
        }
    }

    private void runNcpYPort() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player) || !isMoving()) return;
        if (yPortJumps >= 4 && player.onGround) yPortJumps = 0;
        if (player.onGround) {
            player.motionY = yPortJumps <= 1 ? 0.42 : 0.4;
            double yaw = Math.toRadians(player.rotationYaw);
            player.motionX -= Math.sin(yaw) * 0.2;
            player.motionZ += Math.cos(yaw) * 0.2;
            yPortJumps++;
        } else if (yPortJumps <= 1) {
            player.motionY = -5.0;
        }
        strafe(MoveUtil.getSpeed(), 1.0, false);
    }

    private void runUncpHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (isMoving()) {
            if (player.onGround) {
                modeSpeed = hasSpeedAmplifierAtLeast(1) ? 0.4563F : 0.3385F;
                player.jump();
            } else {
                modeSpeed *= 0.98F;
            }
            if (!player.onGround && player.fallDistance > 2.0F) {
                timer.timerSpeed = 1.0F;
                return;
            }
            strafe(modeSpeed, 1.0, false);
            if (!player.onGround && ++uncpTick % 3 == 0) {
                timer.timerSpeed = 1.0815F;
                uncpTick = 0;
            } else {
                timer.timerSpeed = 0.9598F;
            }
        } else {
            timer.timerSpeed = 1.0F;
        }
    }

    private void runUncpHopNew() {
        EntityPlayerSP player = mc.thePlayer;
        if (player.fallDistance > 2.0F) {
            if (timer.timerSpeed > 1.0F) timer.timerSpeed = 1.0F;
            return;
        }
        if (!isMoving() || isInLiquidOrClimbable(player)) return;

        if (player.onGround) {
            if (uncpLowHop.getValue()) player.motionY = 0.4;
            else player.jump();
            uncpNewAirTick = 0;
            return;
        }

        if (player.hurtTime <= 1 && pullDown.getValue()) {
            uncpNewAirTick++;
            if (uncpNewAirTick == pullDownTick.getValue()) {
                strafe(MoveUtil.getSpeed(), 1.0, false);
                player.motionY = -0.1523351824467155;
            }
        }
        if (pullDownOnHurt.getValue() && player.hurtTime >= 2 && player.hurtTime <= 4 && player.motionY >= 0.0) {
            player.motionY -= 0.1;
        }
        if (uncpAirStrafe.getValue()) {
            strafe(Math.max(MoveUtil.getSpeed(), calculatePotionSpeed(0.2)), 0.7, false);
        }
        if (uncpTimerBoost.getValue()) {
            if (player.hurtTime <= 1) {
                switch (player.ticksExisted % 5) {
                    case 0:
                        timer.timerSpeed = 1.025F;
                        break;
                    case 2:
                        timer.timerSpeed = 1.08F;
                        break;
                    case 4:
                        timer.timerSpeed = 1.0F;
                        break;
                    default:
                        break;
                }
            } else {
                timer.timerSpeed = 1.0F;
            }
        }
        if (uncpBoost.getValue()) {
            player.motionX *= 1.00718;
            player.motionZ *= 1.00718;
        }
        if (uncpDamageBoost.getValue() && player.hurtTime >= 1) {
            strafe(Math.max(MoveUtil.getSpeed(), 0.5), 1.0, false);
        }
    }

    private void runAacHop3313() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || player.isInWater() || player.isInLava() || player.isOnLadder()
                || player.isRiding() || player.hurtTime > 0) return;
        if (player.onGround && player.isCollidedVertically) {
            double yaw = Math.toRadians(player.rotationYaw);
            player.motionX -= Math.sin(yaw) * 0.202;
            player.motionZ += Math.cos(yaw) * 0.202;
            player.motionY = 0.405;
            strafe(MoveUtil.getSpeed(), 1.0, false);
        } else if (player.fallDistance < 0.31F) {
            if (mc.theWorld.getBlockState(player.getPosition()).getBlock() instanceof BlockCarpet) return;
            player.jumpMovementFactor = player.moveStrafing == 0.0F ? 0.027F : 0.021F;
            player.motionX *= 1.001;
            player.motionZ *= 1.001;
            if (!player.isCollidedHorizontally) player.motionY -= 0.014999993;
        } else {
            player.jumpMovementFactor = 0.02F;
        }
    }

    private void runAacHop350Post() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || player.isInWater() || player.isInLava() || player.isSneaking()) return;
        player.jumpMovementFactor += 0.00208F;
        if (player.fallDistance <= 1.0F) {
            if (player.onGround) {
                player.jump();
                player.motionX *= 1.0118;
                player.motionZ *= 1.0118;
            } else {
                player.motionY -= 0.0147;
                player.motionX *= 1.00138;
                player.motionZ *= 1.00138;
            }
        }
    }

    private void runAacHop4() {
        EntityPlayerSP player = mc.thePlayer;
        timer.timerSpeed = 1.0F;
        if (!isMoving() || player.isInWater() || player.isInLava() || player.isOnLadder() || player.isRiding()) return;
        if (player.onGround) player.jump();
        else if (player.fallDistance <= 0.1F) timer.timerSpeed = 1.5F;
        else if (player.fallDistance < 1.3F) timer.timerSpeed = 0.7F;
    }

    private void runAacHop5() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || player.isInWater() || player.isInLava() || player.isOnLadder() || player.isRiding()) return;
        if (player.onGround) {
            player.jump();
            timer.timerSpeed = 0.9385F;
            ((IAccessorEntityPlayer) player).setSpeedInAir(0.0201F);
        }
        if (player.fallDistance < 2.5F) {
            if (player.fallDistance > 0.7F) {
                if (player.ticksExisted % 3 == 0) timer.timerSpeed = 1.925F;
                else if (player.fallDistance < 1.25F) timer.timerSpeed = 1.7975F;
            }
            ((IAccessorEntityPlayer) player).setSpeedInAir(0.02F);
        }
    }

    private void runSpartanYPort() {
        EntityPlayerSP player = mc.thePlayer;
        if (!mc.gameSettings.keyBindForward.isKeyDown()) return;
        if (player.onGround) {
            player.jump();
            spartanAirMoves = 0;
        } else {
            timer.timerSpeed = 1.08F;
            if (spartanAirMoves >= 3) player.jumpMovementFactor = 0.0275F;
            if (spartanAirMoves >= 4 && spartanAirMoves % 2 == 0) {
                player.motionY = -0.32 - ThreadLocalRandom.current().nextDouble(0.009);
                player.jumpMovementFactor = 0.0238F;
            }
            spartanAirMoves++;
        }
    }

    private void runSpectreHop(double yMotion) {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || player.movementInput.jump) return;
        if (player.onGround) {
            strafe(1.1, 1.0, false);
            player.motionY = yMotion;
        } else {
            strafe(MoveUtil.getSpeed(), 1.0, false);
        }
    }

    private void runSpectreOnGround() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || player.movementInput.jump) return;
        if (spectreSpeedUp >= 10) {
            if (player.onGround) {
                stopXZ();
                spectreSpeedUp = 0;
            }
            return;
        }
        if (player.onGround && mc.gameSettings.keyBindForward.isKeyDown()) {
            double yaw = Math.toRadians(player.rotationYaw);
            player.motionX -= Math.sin(yaw) * 0.145;
            player.motionZ += Math.cos(yaw) * 0.145;
            player.motionY = 0.005;
            spectreSpeedUp++;
        }
    }

    private void runVerusHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (isMoving()) {
            if (player.onGround) {
                modeSpeed = hasSpeedAmplifierAtLeast(1) ? 0.46F : 0.34F;
                player.jump();
            } else {
                modeSpeed *= 0.98F;
            }
            strafe(modeSpeed, 1.0, false);
        }
    }

    private void runVerusFHop() {
        EntityPlayerSP player = mc.thePlayer;
        boolean diagonal = player.movementInput.moveForward != 0.0F && player.movementInput.moveStrafe != 0.0F;
        if (player.onGround) {
            strafe(diagonal ? 0.4825 : 0.535, 1.0, false);
            player.jump();
        } else {
            strafe(diagonal ? 0.334 : 0.3345, 1.0, false);
        }
    }

    private void runVerusLowHop(boolean newer) {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (!isMoving()) return;
        if (player.onGround) {
            player.jump();
            if (newer) {
                modeSpeed = player.isPotionActive(Potion.moveSlowdown)
                        && player.getActivePotionEffect(Potion.moveSlowdown).getAmplifier() == 1 ? 0.3F : 0.33F;
            } else {
                modeSpeed = hasSpeedAmplifierAtLeast(1) ? 0.5F : 0.36F;
            }
        } else {
            if (airTicks <= 1) player.motionY = -0.09800000190734863;
            modeSpeed *= newer ? 0.99F : 0.98F;
        }
        strafe(modeSpeed, 1.0, false);
    }

    private void runVulcanHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (isMoving()) {
            if (!player.onGround && player.fallDistance > 2.0F) {
                timer.timerSpeed = 1.0F;
                return;
            }
            if (player.onGround) {
                player.jump();
                if (player.motionY > 0.0) timer.timerSpeed = 1.1453F;
                strafe(0.4815, 1.0, false);
            } else if (player.motionY < 0.0) {
                timer.timerSpeed = 0.9185F;
            }
        } else {
            timer.timerSpeed = 1.0F;
        }
    }

    private void runVulcanLowHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (isMoving()) {
            if (!player.onGround && player.fallDistance > 1.1F) {
                timer.timerSpeed = 1.0F;
                player.motionY = -0.25;
                return;
            }
            if (player.onGround) {
                player.jump();
                strafe(0.4815, 1.0, false);
                timer.timerSpeed = 1.263F;
            } else if (player.ticksExisted % 4 == 0) {
                if (player.ticksExisted % 3 == 0 && player.motionY != 0.0) player.motionY = -0.01 / player.motionY;
                else if (player.posY != 0.0) player.motionY = -player.motionY / player.posY;
                timer.timerSpeed = 0.8985F;
            }
        } else {
            timer.timerSpeed = 1.0F;
        }
    }

    private void runVulcanGround() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (isMoving() && collidesBottom()) {
            boolean speedEffect = player.isPotionActive(Potion.moveSpeed)
                    && player.getActivePotionEffect(Potion.moveSpeed).getAmplifier() > 0;
            double speed = speedEffect ? 0.59 : player.movementInput.moveStrafe != 0.0F ? 0.41 : 0.42;
            strafe(speed, 1.0, false);
            player.motionY = 0.005;
        }
    }

    private void runOldMatrixHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (isMoving()) {
            if (player.onGround) {
                player.jump();
                ((IAccessorEntityPlayer) player).setSpeedInAir(0.02098F);
                timer.timerSpeed = 1.055F;
            } else {
                strafe(MoveUtil.getSpeed(), 1.0, false);
            }
        } else {
            timer.timerSpeed = 1.0F;
        }
    }

    private void runMatrixHop(boolean slow) {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (!isMoving()) {
            if (slow) timer.timerSpeed = 1.0F;
            return;
        }
        if (matrixLowHop.getValue()) player.jumpMovementFactor = 0.026F;
        if (slow && !player.onGround && player.fallDistance > 2.0F) {
            timer.timerSpeed = 1.0F;
            return;
        }
        boolean scaffold = ((Scaffold) Myau.moduleManager.modules.get(Scaffold.class)).isEnabled();
        if (player.onGround) {
            double speed = MoveUtil.getSpeed() + (scaffold ? 0.0 : extraGroundBoost.getValue());
            strafe(speed, 1.0, false);
            player.motionY = 0.42 - (matrixLowHop.getValue() ? 0.00348 : 0.0);
            if (slow) timer.timerSpeed = 0.5195F;
        } else {
            if (slow) timer.timerSpeed = 1.0973F;
            else if (!scaffold && MoveUtil.getSpeed() < 0.19) strafe(MoveUtil.getSpeed(), 1.0, false);
        }
        ((IAccessorEntityPlayer) player).setSpeedInAir(
                player.fallDistance <= 0.4F && player.moveStrafing == 0.0F ? 0.02035F : 0.02F);
    }

    private void runIntaveHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || isInLiquidOrClimbable(player)) return;
        if (player.onGround) {
            player.motionY = 0.42 - (intaveLowHop.getValue() ? 1.7E-14 : 0.0);
            if (player.isSprinting()) strafe(MoveUtil.getSpeed(), strafeStrength.getValue(), false);
            timer.timerSpeed = groundTimer.getValue();
        } else {
            timer.timerSpeed = airTimer.getValue();
        }
        if (intaveBoost.getValue() && player.motionY > 0.003 && player.isSprinting()) {
            double boost = 1.0 + 0.003 * initialBoostMultiplier.getValue();
            player.motionX *= boost;
            player.motionZ *= boost;
        }
    }

    private void runTeleportCubeCraft() {
        if (isMoving() && mc.thePlayer.onGround && System.currentTimeMillis() - lastCubePort >= 300L) {
            strafe(cubecraftPortLength.getValue(), 1.0, false);
            lastCubePort = System.currentTimeMillis();
        }
    }

    private void runHypixelLowHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || player.fallDistance > 1.2F) return;
        if (player.onGround) {
            player.jump();
            strafe(MoveUtil.getSpeed(), 1.0, false);
            return;
        }
        switch (airTicks) {
            case 1:
                strafe(MoveUtil.getSpeed(), 1.0, false);
                break;
            case 4:
                player.motionY -= 0.03;
                break;
            case 5:
                player.motionY -= 0.1905189780583944;
                break;
            case 6:
                player.motionY *= 1.01;
                break;
            case 7:
                if (glide.getValue()) player.motionY /= 1.5;
                break;
            default:
                break;
        }
        if (airTicks >= 7 && glide.getValue()) {
            strafe(Math.max(MoveUtil.getSpeed(), 0.281), 0.7, false);
        }
        if (player.hurtTime == 9) strafe(MoveUtil.getSpeed(), 1.0, false);
        if (getPotionAmplifier(Potion.moveSpeed) == 2
                && (airTicks == 1 || airTicks == 2 || airTicks == 5 || airTicks == 6 || airTicks == 8)) {
            player.motionX *= 1.2;
            player.motionZ *= 1.2;
        }
    }

    private void runBlocksMcHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player) || !isMoving()) return;
        if (player.onGround) {
            player.jump();
            return;
        }
        if (fullStrafe.getValue()) strafe(Math.max(0.0, MoveUtil.getSpeed() - 0.004), 1.0, false);
        else if (airTicks >= 6) strafe(MoveUtil.getSpeed(), 1.0, false);

        if (getPotionAmplifier(Potion.moveSpeed) > 0 && airTicks == 3) {
            player.motionX *= 1.12;
            player.motionZ *= 1.12;
        }
        if (bmcLowHop.getValue() && airTicks == 4
                && (!safeY.getValue() || Math.abs(player.posY % 1.0 - 0.16610926093821377) < 1.0E-9)) {
            player.motionY = -0.09800000190734863;
        }
        if (player.hurtTime == 9 && bmcDamageBoost.getValue()) {
            strafe(Math.max(MoveUtil.getSpeed(), 0.7), 1.0, false);
        }
        if (damageLowHop.getValue() && player.hurtTime >= 1 && player.motionY > 0.0) player.motionY -= 0.15;
    }

    private void runBoost() {
        EntityPlayerSP player = mc.thePlayer;
        double speed = 3.1981;
        double offset = 4.69;
        boolean shouldOffset = mc.theWorld.getCollidingBoundingBoxes(player,
                player.getEntityBoundingBox().offset(player.motionX / offset, 0.0, player.motionZ / offset)).isEmpty();
        if (player.onGround && boostGround < 1.0F) boostGround = Math.min(1.0F, boostGround + 0.2F);
        if (!player.onGround) boostGround = 0.0F;
        if (boostGround == 1.0F && !player.isInLava() && !player.isOnLadder() && !player.isSneaking() && isMoving()) {
            if (!player.isSprinting()) offset += 0.8;
            if (player.moveStrafing != 0.0F) {
                speed -= 0.1;
                offset += 0.5;
            }
            if (player.isInWater()) speed -= 0.1;
            boostMotionDelay++;
            if (boostMotionDelay == 1) {
                player.motionX *= speed;
                player.motionZ *= speed;
            } else if (boostMotionDelay == 2) {
                player.motionX /= 1.458;
                player.motionZ /= 1.458;
            } else if (boostMotionDelay == 4) {
                if (shouldOffset) player.setPosition(player.posX + player.motionX / offset,
                        player.posY, player.posZ + player.motionZ / offset);
                boostMotionDelay = 0;
            }
        }
    }

    private void runFrame() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving()) return;
        if (player.onGround) {
            player.jump();
            if (frameMotionTicks == 1) {
                frameTimerTicks = 0;
                if (frameMoved) {
                    stopXZ();
                    frameMoved = false;
                }
                frameMotionTicks = 0;
            } else {
                frameMotionTicks = 1;
            }
        } else if (!frameMoved && frameMotionTicks == 1 && frameTimerTicks >= 5) {
            player.motionX *= 4.25;
            player.motionZ *= 4.25;
            frameMoved = true;
        }
        if (!player.onGround) strafe(MoveUtil.getSpeed(), 1.0, false);
        frameTimerTicks++;
    }

    private void runMiJump() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving()) return;
        if (player.onGround && !player.movementInput.jump) {
            player.motionY += 0.1;
            player.motionX *= 1.8;
            player.motionZ *= 1.8;
            double currentSpeed = MoveUtil.getSpeed();
            if (currentSpeed > 0.66) strafe(0.66, 1.0, false);
        }
        strafe(MoveUtil.getSpeed(), 1.0, false);
    }

    private void runOnGround() {
        EntityPlayerSP player = mc.thePlayer;
        if (!isMoving() || player.fallDistance > 3.994F || player.isInWater()
                || player.isOnLadder() || player.isCollidedHorizontally) return;
        player.posY -= 0.3993000090122223;
        player.motionY = -1000.0;
        player.cameraPitch = 0.3F;
        player.distanceWalkedModified = 44.0F;
        timer.timerSpeed = 1.0F;
        if (player.onGround) {
            player.posY += 0.3993000090122223;
            player.motionY = 0.3993000090122223;
            player.distanceWalkedOnStepModified = 44.0F;
            player.motionX *= 1.590000033378601;
            player.motionZ *= 1.590000033378601;
            player.cameraPitch = 0.0F;
            timer.timerSpeed = 1.199F;
        }
    }

    private void runSlowHop() {
        EntityPlayerSP player = mc.thePlayer;
        if (isInLiquidOrClimbable(player)) return;
        if (isMoving()) {
            if (player.onGround) player.jump();
            else strafe(MoveUtil.getSpeed() * 1.011, 1.0, false);
        } else {
            stopXZ();
        }
    }

    private void runCustom() {
        EntityPlayerSP player = mc.thePlayer;
        ItemStack heldItem = player.getHeldItem();
        boolean consuming = heldItem != null && (heldItem.getItem() instanceof ItemFood
                || heldItem.getItem() instanceof ItemPotion || heldItem.getItem() instanceof ItemBucketMilk);
        if ((notOnVoid.getValue() && !hasGroundBelow())
                || (notOnFalling.getValue() && player.fallDistance > 2.5F)
                || (notOnConsuming.getValue() && player.isUsingItem() && consuming)) {
            if (player.onGround) player.jump();
            timer.timerSpeed = 1.0F;
            return;
        }
        if (!isMoving()) return;
        if (player.onGround) {
            if (customGroundStrafe.getValue() > 0.0F) strafe(customGroundStrafe.getValue(), 1.0, false);
            timer.timerSpeed = customGroundTimer.getValue();
            player.motionY = customY.getValue();
        } else {
            if (customAirStrafe.getValue() > 0.0F) strafe(customAirStrafe.getValue(), 1.0, false);
            timer.timerSpeed = player.ticksExisted % customAirTimerTick.getValue() == 0
                    ? customAirTimer.getValue() : 1.0F;
        }
    }

    @EventTarget(Priority.LOW)
    public void onMoveInput(MoveInputEvent event) {
        if (!isEnabled() || !isMode(LEGIT) || !canBoostLegit() || mc.thePlayer.onGround) return;
        if (boosting && RotationState.isActived() && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }

    @EventTarget(Priority.LOW)
    public void onStrafe(StrafeEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        if (isMode(DEFAULT) && canBoost()) {
            runDefault(event);
        } else if (isMode(LEGIT) && canBoostLegit()) {
            if (!onlyJumping.getValue() && mc.thePlayer.onGround && MoveUtil.isForwardPressed()) mc.thePlayer.jump();
        } else if (isMode(HYPIXEL_HOP) && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()
                && mc.thePlayer.onGround && isMoving()) {
            mc.thePlayer.jump();
            if (!mc.thePlayer.isUsingItem()) strafe(0.4, 1.0, false);
        }
    }

    @EventTarget(Priority.LOW)
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        if (isMode(DEFAULT) && canBoost()) mc.thePlayer.movementInput.jump = false;
        if (isMode(VULCAN_GROUND_288)) mc.thePlayer.movementInput.jump = false;
    }

    @EventTarget(Priority.LOW)
    public void onJump(JumpEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        if (isMode(VULCAN_GROUND_288)) {
            event.setCancelled(true);
        } else if (isMode(HYPIXEL_LOW_HOP) && isMoving()) {
            double minimum = 0.281 + 0.13 * Math.max(0, getPotionAmplifier(Potion.moveSpeed));
            strafe(Math.max(MoveUtil.getSpeed(), minimum), 1.0, false);
        }
    }

    @EventTarget(Priority.LOW)
    public void onPacket(PacketEvent event) {
        if (!isEnabled() || event.getType() != EventType.SEND || !isMode(VULCAN_GROUND_288)
                || !(event.getPacket() instanceof C03PacketPlayer) || !collidesBottom()) return;
        IAccessorC03PacketPlayer packet = (IAccessorC03PacketPlayer) event.getPacket();
        packet.setY(packet.getY() + 0.005);
    }

    private void runDefault(StrafeEvent event) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42F;
            MoveUtil.setSpeed(MoveUtil.getJumpMotion() * multiplier.getValue(), MoveUtil.getMoveYaw());
        } else {
            if (friction.getValue() != 1.0F) event.setFriction(event.getFriction() * friction.getValue());
            if (strafe.getValue() > 0) {
                double speed = MoveUtil.getSpeed();
                MoveUtil.setSpeed(speed * (100 - strafe.getValue()) / 100.0, MoveUtil.getDirectionYaw());
                MoveUtil.addSpeed(speed * strafe.getValue() / 100.0, MoveUtil.getMoveYaw());
                MoveUtil.setSpeed(speed);
            }
        }
    }

    private void strafe(double speed, double strength, boolean stopWhenNoInput) {
        if (!isMoving()) {
            if (stopWhenNoInput) stopXZ();
            return;
        }
        double previousX = mc.thePlayer.motionX * (1.0 - strength);
        double previousZ = mc.thePlayer.motionZ * (1.0 - strength);
        double useSpeed = speed * strength;
        double yaw = Math.toRadians(MoveUtil.getMoveYaw());
        mc.thePlayer.motionX = -Math.sin(yaw) * useSpeed + previousX;
        mc.thePlayer.motionZ = Math.cos(yaw) * useSpeed + previousZ;
    }

    private void stopXZ() {
        mc.thePlayer.motionX = 0.0;
        mc.thePlayer.motionZ = 0.0;
    }

    private boolean collidesVertically(EntityPlayerSP player) {
        return player.isCollidedVertically || !mc.theWorld.getCollidingBoundingBoxes(player,
                player.getEntityBoundingBox().offset(0.0, player.motionY, 0.0)).isEmpty();
    }

    private boolean collidesBottom() {
        return mc.thePlayer != null && mc.theWorld != null && !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().offset(0.0, -0.005, 0.0)).isEmpty();
    }

    private boolean hasGroundBelow() {
        EntityPlayerSP player = mc.thePlayer;
        MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(
                new Vec3(player.posX, player.getEntityBoundingBox().minY, player.posZ),
                new Vec3(player.posX, 0.0, player.posZ), false, true, false);
        return hit != null || player.getEntityBoundingBox().minY <= 0.0;
    }

    private boolean hasSpeedAmplifierAtLeast(int amplifier) {
        return mc.thePlayer.isPotionActive(Potion.moveSpeed)
                && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() >= amplifier;
    }

    private int getPotionAmplifier(Potion potion) {
        return mc.thePlayer.isPotionActive(potion) ? mc.thePlayer.getActivePotionEffect(potion).getAmplifier() : 0;
    }

    private double calculatePotionSpeed(double base) {
        return base + 0.199999999 * getPotionAmplifier(Potion.moveSpeed);
    }

    private double baseMoveSpeed() {
        double speed = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            speed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return speed;
    }

    private double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public String[] getSuffix() {
        return new String[]{mode.getModeString()};
    }
}
