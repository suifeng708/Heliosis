package myau.module.modules;

import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.*;
import myau.mixin.*;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import myau.util.RenderUtil;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "Fly", enabled = "false", hidden = "false", description = "LiquidBounce flight modes", category = Category.MOVEMENT)
public class Fly extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final int VANILLA = 0;
    private static final int SMOOTH_VANILLA = 1;
    private static final int NCP = 2;
    private static final int OLD_NCP = 3;
    private static final int AAC_1910 = 4;
    private static final int AAC_305 = 5;
    private static final int AAC_316 = 6;
    private static final int AAC_3312 = 7;
    private static final int AAC_3312_GLIDE = 8;
    private static final int AAC_3313 = 9;
    private static final int CUBECRAFT = 10;
    private static final int HYPIXEL = 11;
    private static final int BOOST_HYPIXEL = 12;
    private static final int FREE_HYPIXEL = 13;
    private static final int NERUX_VACE = 14;
    private static final int MINESUCHT = 15;
    private static final int BLOCKS_MC = 16;
    private static final int BLOCKS_MC_2 = 17;
    private static final int SPARTAN = 18;
    private static final int SPARTAN_2 = 19;
    private static final int BUG_SPARTAN = 20;
    private static final int VULCAN = 21;
    private static final int VULCAN_OLD = 22;
    private static final int VULCAN_GHOST = 23;
    private static final int VERUS = 24;
    private static final int VERUS_GLIDE = 25;
    private static final int MINE_SECURE = 26;
    private static final int HAWK_EYE = 27;
    private static final int HAC = 28;
    private static final int WATCH_CAT = 29;
    private static final int JETPACK = 30;
    private static final int KEEP_ALIVE = 31;
    private static final int COLLIDE = 32;
    private static final int JUMP = 33;
    private static final int FLAG = 34;
    private static final int FIREBALL = 35;

    public final ModeProperty mode = new ModeProperty("Mode", VANILLA, new String[]{
            "Vanilla", "SmoothVanilla", "NCP", "OldNCP",
            "AAC1.9.10", "AAC3.0.5", "AAC3.1.6-Gomme", "AAC3.3.12", "AAC3.3.12-Glide", "AAC3.3.13",
            "CubeCraft", "Hypixel", "BoostHypixel", "FreeHypixel", "NeruxVace", "Minesucht",
            "BlocksMC", "BlocksMC2", "Spartan", "Spartan2", "BugSpartan",
            "Vulcan", "VulcanOld", "VulcanGhost", "Verus", "VerusGlide",
            "MineSecure", "HawkEye", "HAC", "WatchCat", "Jetpack", "KeepAlive",
            "Collide", "Jump", "Flag", "Fireball"
    });

    public final FloatProperty vanillaSpeed = new FloatProperty("VanillaSpeed", 2.0F, 0.0F, 10.0F,
            () -> isMode(VANILLA, KEEP_ALIVE, MINE_SECURE, BUG_SPARTAN));
    public final BooleanProperty vanillaKickBypass = new BooleanProperty("VanillaKickBypass", false,
            () -> isMode(VANILLA, SMOOTH_VANILLA));
    public final FloatProperty ncpMotion = new FloatProperty("NCPMotion", 0.0F, 0.0F, 1.0F, () -> mode.getValue() == NCP);

    public final FloatProperty aacSpeed = new FloatProperty("AAC1.9.10-Speed", 0.3F, 0.0F, 1.0F, () -> mode.getValue() == AAC_1910);
    public final BooleanProperty aacFast = new BooleanProperty("AAC3.0.5-Fast", true, () -> mode.getValue() == AAC_305);
    public final FloatProperty aacMotion = new FloatProperty("AAC3.3.12-Motion", 10.0F, 0.1F, 10.0F, () -> mode.getValue() == AAC_3312);
    public final FloatProperty aacMotion2 = new FloatProperty("AAC3.3.13-Motion", 10.0F, 0.1F, 10.0F, () -> mode.getValue() == AAC_3313);

    public final BooleanProperty hypixelBoost = new BooleanProperty("Hypixel-Boost", true, () -> mode.getValue() == HYPIXEL);
    public final IntProperty hypixelBoostDelay = new IntProperty("Hypixel-BoostDelay", 1200, 50, 2000,
            () -> mode.getValue() == HYPIXEL && hypixelBoost.getValue());
    public final FloatProperty hypixelBoostTimer = new FloatProperty("Hypixel-BoostTimer", 1.0F, 0.1F, 5.0F,
            () -> mode.getValue() == HYPIXEL && hypixelBoost.getValue());

    public final IntProperty neruxVaceTicks = new IntProperty("NeruxVace-Ticks", 6, 2, 20, () -> mode.getValue() == NERUX_VACE);

    public final BooleanProperty damage = new BooleanProperty("Damage", false, () -> mode.getValue() == VERUS);
    public final BooleanProperty timerSlow = new BooleanProperty("TimerSlow", true, () -> mode.getValue() == VERUS);
    public final IntProperty boostTicks = new IntProperty("BoostTicks", 20, 1, 30, () -> mode.getValue() == VERUS);
    public final FloatProperty boostMotion = new FloatProperty("BoostMotion", 6.5F, 1.0F, 9.85F, () -> mode.getValue() == VERUS);
    public final FloatProperty yBoost = new FloatProperty("YBoost", 0.42F, 0.0F, 10.0F, () -> mode.getValue() == VERUS);

    public final BooleanProperty stable = new BooleanProperty("Stable", false, () -> isMode(BLOCKS_MC, BLOCKS_MC_2));
    public final BooleanProperty timerSlowed = new BooleanProperty("TimerSlowed", true, () -> isMode(BLOCKS_MC, BLOCKS_MC_2));
    public final FloatProperty boostSpeed = new FloatProperty("BoostSpeed", 6.0F, 1.0F, 15.0F, () -> isMode(BLOCKS_MC, BLOCKS_MC_2));
    public final FloatProperty extraBoost = new FloatProperty("ExtraSpeed", 1.0F, 0.0F, 2.0F, () -> isMode(BLOCKS_MC, BLOCKS_MC_2));
    public final BooleanProperty stopOnLanding = new BooleanProperty("StopOnLanding", true, () -> isMode(BLOCKS_MC, BLOCKS_MC_2));
    public final BooleanProperty stopOnNoMove = new BooleanProperty("StopOnNoMove", false, () -> isMode(BLOCKS_MC, BLOCKS_MC_2));
    public final BooleanProperty debugFly = new BooleanProperty("Debug", false, () -> isMode(BLOCKS_MC, BLOCKS_MC_2));

    public final ModeProperty pitchMode = new ModeProperty("PitchMode", 0, new String[]{"Custom", "Smart"}, () -> mode.getValue() == FIREBALL);
    public final FloatProperty rotationPitch = new FloatProperty("Pitch", 90.0F, 0.0F, 90.0F,
            () -> mode.getValue() == FIREBALL && pitchMode.getValue() == 0);
    public final BooleanProperty invertYaw = new BooleanProperty("InvertYaw", true,
            () -> mode.getValue() == FIREBALL && pitchMode.getValue() == 0);
    public final ModeProperty autoFireball = new ModeProperty("AutoFireball", 2, new String[]{"Off", "Pick", "Spoof", "Switch"},
            () -> mode.getValue() == FIREBALL);
    public final BooleanProperty swing = new BooleanProperty("Swing", true, () -> mode.getValue() == FIREBALL);
    public final IntProperty fireballTry = new IntProperty("MaxFireballTry", 1, 0, 2, () -> mode.getValue() == FIREBALL);
    public final ModeProperty fireballThrow = new ModeProperty("FireballThrow", 0, new String[]{"Normal", "Edge"},
            () -> mode.getValue() == FIREBALL);
    public final FloatProperty edgeThreshold = new FloatProperty("EdgeThreshold", 1.05F, 1.0F, 2.0F,
            () -> mode.getValue() == FIREBALL && fireballThrow.getValue() == 1);
    public final BooleanProperty autoJump = new BooleanProperty("AutoJump", true, () -> mode.getValue() == FIREBALL);
    public final BooleanProperty mark = new BooleanProperty("Mark", true);

    private int activeMode = -1;
    private double startY;
    private double jumpY;
    private boolean wasFlying;
    private long groundTimer;
    private long modeTimer;
    private int airTicks;
    private int tick;
    private int secondaryTick;
    private boolean noFlag;
    private boolean wasDead;
    private double aacJump;
    private float startYaw;
    private float startPitch;
    private int boostState;
    private double moveSpeed;
    private double lastDistance;
    private boolean blocksFlying;
    private boolean blocksNotUnder;
    private boolean blocksTeleported;
    private boolean blocksBlinked;
    private int verusBoostTicks;
    private long minesuchtTime;
    private int fireballStage;
    private int firedTicks;
    private boolean wasFired;
    private BlockPos firePosition;

    private boolean isMode(int... modes) {
        int current = mode.getValue();
        for (int candidate : modes) if (current == candidate) return true;
        return false;
    }

    @Override
    public void onEnabled() {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        wasFlying = player.capabilities.isFlying;
        activeMode = mode.getValue();
        startY = jumpY = player.posY;
        groundTimer = System.currentTimeMillis();
        resetModeState();
        enableMode(activeMode);
    }

    @Override
    public void onDisabled() {
        EntityPlayerSP player = mc.thePlayer;
        disableMode(activeMode);
        activeMode = -1;
        timer().setTimerSpeed(1.0F);
        if (player == null) return;

        player.capabilities.isFlying = wasFlying;
        player.stepHeight = 0.6F;
        player.jumpMovementFactor = 0.02F;
        ((IAccessorEntityPlayer) player).setSpeedInAir(0.02F);
        if (!isMode(AAC_1910, AAC_305, AAC_316, AAC_3312, AAC_3312_GLIDE, AAC_3313,
                HYPIXEL, VERUS_GLIDE, SMOOTH_VANILLA, VANILLA, FIREBALL, COLLIDE, JUMP)) {
            stop();
        }
    }

    private void resetModeState() {
        tick = secondaryTick = airTicks = 0;
        noFlag = wasDead = false;
        aacJump = 3.8;
        boostState = 1;
        moveSpeed = 0.1;
        lastDistance = 0.0;
        blocksFlying = blocksNotUnder = blocksTeleported = blocksBlinked = false;
        verusBoostTicks = 0;
        minesuchtTime = 0L;
        fireballStage = firedTicks = 0;
        wasFired = false;
        firePosition = null;
        modeTimer = System.currentTimeMillis();
    }

    private void enableMode(int selectedMode) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        switch (selectedMode) {
            case NCP:
                if (player.onGround) sendNcpDamage(true);
                break;
            case OLD_NCP:
                if (player.onGround) {
                    for (int i = 0; i < 4; i++) {
                        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 1.01, player.posZ, false));
                        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false));
                    }
                    player.jump();
                    player.swingItem();
                }
                break;
            case BUG_SPARTAN:
                sendNcpDamage(false);
                break;
            case BOOST_HYPIXEL:
                enableBoostHypixel(player);
                break;
            case FREE_HYPIXEL:
                player.setPositionAndUpdate(player.posX, player.posY + 0.42, player.posZ);
                startYaw = player.rotationYaw;
                startPitch = player.rotationPitch;
                break;
            case VERUS:
                enableVerus(player);
                break;
            case VULCAN_GHOST:
                player.addChatMessage(new ChatComponentText("VulcanGhost: sneak when landing, then retrace and sneak again."));
                break;
        }
    }

    private void disableMode(int selectedMode) {
        if (selectedMode == BLOCKS_MC_2 && Myau.blinkManager != null && blocksBlinked) {
            Myau.blinkManager.setBlinkState(false, BlinkModules.FLY);
        }
        if (selectedMode == FIREBALL) restoreFireballSlot();
        blocksBlinked = false;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (!isEnabled() || player == null || mc.theWorld == null) return;

        if (event.getType() == EventType.POST) {
            if (activeMode == BOOST_HYPIXEL) {
                double x = player.posX - player.prevPosX;
                double z = player.posZ - player.prevPosZ;
                lastDistance = Math.hypot(x, z);
            }
            return;
        }

        if (activeMode != mode.getValue()) {
            disableMode(activeMode);
            activeMode = mode.getValue();
            startY = jumpY = player.posY;
            resetModeState();
            enableMode(activeMode);
        }

        airTicks = player.onGround ? 0 : airTicks + 1;

        switch (activeMode) {
            case VANILLA:
                player.capabilities.isFlying = false;
                player.onGround = false;
                ((IAccessorEntity) player).setIsInWeb(false);
                strafe(vanillaSpeed.getValue(), true);
                player.motionY = verticalSpeed(vanillaSpeed.getValue());
                handleVanillaKickBypass();
                break;
            case SMOOTH_VANILLA:
                player.capabilities.isFlying = true;
                handleVanillaKickBypass();
                break;
            case NCP:
                player.motionY = mc.gameSettings.keyBindSneak.isKeyDown() ? -0.5 : -ncpMotion.getValue();
                strafeCurrent();
                break;
            case OLD_NCP:
                if (startY > player.posY) player.motionY = -1.0E-30;
                if (mc.gameSettings.keyBindSneak.isKeyDown()) player.motionY = -0.2;
                if (mc.gameSettings.keyBindJump.isKeyDown() && player.posY < startY - 0.1) player.motionY = 0.2;
                strafeCurrent();
                break;
            case AAC_1910:
                if (mc.gameSettings.keyBindJump.isKeyDown()) aacJump += 0.2;
                if (mc.gameSettings.keyBindSneak.isKeyDown()) aacJump -= 0.2;
                if (startY + aacJump > player.posY) {
                    PacketUtil.sendPacket(new C03PacketPlayer(true));
                    player.motionY = 0.8;
                    strafe(aacSpeed.getValue(), false);
                }
                strafeCurrent();
                break;
            case AAC_305:
                if (tick == 2) player.motionY = 0.1;
                else if (tick > 2) tick = 0;
                if (aacFast.getValue()) player.jumpMovementFactor = player.movementInput.moveStrafe == 0.0F ? 0.08F : 0.0F;
                tick++;
                break;
            case AAC_316:
                player.capabilities.isFlying = true;
                if (tick == 2) player.motionY += 0.05;
                else if (tick > 2) {
                    player.motionY -= 0.05;
                    tick = 0;
                }
                tick++;
                if (!noFlag) PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, player.onGround));
                if (player.posY <= 0.0) noFlag = true;
                break;
            case AAC_3312:
                if (player.posY < -70.0) player.motionY = aacMotion.getValue();
                handleAacControlKey();
                break;
            case AAC_3312_GLIDE:
                if (!player.onGround) tick++;
                if (tick == 2) timer().setTimerSpeed(1.0F);
                else if (tick == 12) timer().setTimerSpeed(0.1F);
                else if (tick >= 12 && !player.onGround) {
                    tick = 0;
                    player.motionY = 0.015;
                }
                break;
            case AAC_3313:
                if (player.isDead) wasDead = true;
                if (wasDead || player.onGround) {
                    wasDead = false;
                    player.motionY = aacMotion2.getValue();
                    player.onGround = false;
                }
                handleAacControlKey();
                break;
            case CUBECRAFT:
                timer().setTimerSpeed(0.6F);
                tick++;
                double cubeSpeed = tick >= 2 ? 2.4 : 0.2;
                if (tick >= 2) tick = 0;
                MoveUtil.setSpeed(cubeSpeed, player.rotationYaw);
                break;
            case HYPIXEL:
                updateHypixel(player);
                break;
            case BOOST_HYPIXEL:
                updateBoostHypixel(player);
                break;
            case FREE_HYPIXEL:
                updateFreeHypixel(player);
                break;
            case NERUX_VACE:
                if (!player.onGround) tick++;
                if (tick >= neruxVaceTicks.getValue() && !player.onGround) {
                    tick = 0;
                    player.motionY = 0.015;
                }
                break;
            case MINESUCHT:
                updateMinesucht(player);
                break;
            case BLOCKS_MC:
                updateBlocksMc(player, false);
                break;
            case BLOCKS_MC_2:
                updateBlocksMc(player, true);
                break;
            case SPARTAN:
                player.motionY = 0.0;
                if (++tick >= 12) {
                    PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 8.0, player.posZ, true));
                    PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY - 8.0, player.posZ, true));
                    tick = 0;
                }
                break;
            case SPARTAN_2:
                strafe(0.264F, false);
                if (player.ticksExisted % 8 == 0) {
                    PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 10.0, player.posZ, true));
                }
                break;
            case BUG_SPARTAN:
                player.capabilities.isFlying = false;
                player.motionY = verticalSpeed(vanillaSpeed.getValue());
                strafe(vanillaSpeed.getValue(), true);
                break;
            case VULCAN:
                if (!player.onGround && player.fallDistance > 0.0F) player.motionY = player.ticksExisted % 2 == 0 ? -0.155 : -0.1;
                break;
            case VULCAN_OLD:
                if (!player.onGround && player.fallDistance > 0.0F) {
                    player.motionY = player.ticksExisted % 2 == 0 ? -0.1 : -0.16;
                    player.jumpMovementFactor = 0.0265F;
                }
                break;
            case VERUS:
                stop();
                if (verusBoostTicks == 0 && player.hurtTime > 0) verusBoostTicks = boostTicks.getValue();
                verusBoostTicks--;
                if (timerSlow.getValue()) timer().setTimerSpeed(player.ticksExisted % 3 == 0 ? 0.15F : 0.08F);
                strafe(boostMotion.getValue(), true);
                break;
            case VERUS_GLIDE:
                if (!player.isInWater() && !player.isInLava() && !((IAccessorEntity) player).getIsInWeb()
                        && !player.isOnLadder() && !player.onGround && player.fallDistance > 1.0F) {
                    player.motionY = -0.09800000190734863;
                    strafe(player.movementInput.moveForward != 0.0F && player.movementInput.moveStrafe != 0.0F ? 0.334F : 0.3345F, false);
                }
                break;
            case MINE_SECURE:
                updateMineSecure(player);
                break;
            case HAWK_EYE:
                player.motionY = player.motionY <= -0.42 ? 0.42 : -0.42;
                break;
            case HAC:
                player.motionX *= 0.8;
                player.motionZ *= 0.8;
                player.motionY = player.motionY <= -0.42 ? 0.42 : -0.42;
                break;
            case WATCH_CAT:
                strafe(0.15F, false);
                player.setSprinting(true);
                if (player.posY < startY + 2.0) {
                    player.motionY = ThreadLocalRandom.current().nextDouble(0.0, 0.5);
                } else if (startY > player.posY) {
                    stopXZ();
                }
                break;
            case JETPACK:
                updateJetpack(player);
                break;
            case KEEP_ALIVE:
                PacketUtil.sendPacket(new C00PacketKeepAlive());
                player.capabilities.isFlying = false;
                player.motionY = verticalSpeed(vanillaSpeed.getValue());
                strafe(vanillaSpeed.getValue(), true);
                break;
            case JUMP:
                if (player.onGround && !((IAccessorEntityLivingBase) player).isJumping()) player.jump();
                if ((mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) || player.onGround) jumpY = player.posY;
                break;
            case FLAG:
                updateFlag(player);
                break;
            case FIREBALL:
                updateFireball(event, player);
                break;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (!isEnabled()) return;
        if (isMode(VANILLA, NCP, OLD_NCP, AAC_1910, CUBECRAFT, BOOST_HYPIXEL, SPARTAN_2,
                BUG_SPARTAN, VERUS, VERUS_GLIDE, MINE_SECURE, WATCH_CAT, KEEP_ALIVE)) {
            event.setFriction(0.0F);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!isEnabled() || event.getType() != EventType.PRE || activeMode != FIREBALL || mc.thePlayer == null) return;
        tickFireball(mc.thePlayer);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;

        if (event.getType() == EventType.SEND && event.getPacket() instanceof C03PacketPlayer) {
            if (isMode(NCP, VERUS)) ((IAccessorC03PacketPlayer) event.getPacket()).setOnGround(true);
            if (isMode(HYPIXEL, BOOST_HYPIXEL)) ((IAccessorC03PacketPlayer) event.getPacket()).setOnGround(false);
        }

        if (event.getType() == EventType.RECEIVE && event.getPacket() instanceof S08PacketPlayerPosLook) {
            if (activeMode == VULCAN_GHOST) {
                event.setCancelled(true);
            } else if (activeMode == BOOST_HYPIXEL) {
                mc.thePlayer.addChatMessage(new ChatComponentText("BoostHypixel Fly: setback detected."));
                setEnabled(false);
            }
        }
    }

    @EventTarget
    public void onJump(JumpEvent event) {
        if (!isEnabled()) return;
        if (isMode(VERUS, HYPIXEL, BOOST_HYPIXEL)) event.setCancelled(true);
    }

    @EventTarget
    public void onBlockBB(BlockBBEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (!isEnabled() || player == null) return;

        BlockPos pos = event.getPos();
        if (isMode(HYPIXEL, BOOST_HYPIXEL) && event.getBlock() == Blocks.air && pos.getY() < player.posY) {
            event.setBoundingBox(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, player.posY, pos.getZ() + 1.0));
            return;
        }

        if (isMode(COLLIDE, VULCAN_GHOST)) {
            if (!mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) return;
            Material material = event.getBlock().getMaterial();
            if (!material.blocksMovement() && material != Material.carpet && material != Material.vine
                    && material != Material.snow && !(event.getBlock() instanceof BlockLadder)) {
                event.setBoundingBox(new AxisAlignedBB(-2.0, -1.0, -2.0, 2.0, 1.0, 2.0)
                        .offset(pos.getX(), pos.getY(), pos.getZ()));
            }
            return;
        }

        if (activeMode == JUMP) {
            boolean belowJump = !mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()
                    ? pos.getY() < jumpY : pos.getY() <= jumpY;
            Material material = event.getBlock().getMaterial();
            if (belowJump && !material.blocksMovement() && material != Material.carpet && material != Material.vine
                    && material != Material.snow && !(event.getBlock() instanceof BlockLadder)) {
                event.setBoundingBox(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, jumpY, pos.getZ() + 1.0));
            }
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        if (isEnabled()) setEnabled(false);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !mark.getValue() || mc.thePlayer == null || isMode(VANILLA, SMOOTH_VANILLA)) return;

        double y = startY + 2.0 + (activeMode == BOOST_HYPIXEL ? 0.42 : 0.0);
        AxisAlignedBB box = new AxisAlignedBB(
                mc.thePlayer.posX - 1.0, y, mc.thePlayer.posZ - 1.0,
                mc.thePlayer.posX + 1.0, y + 0.02, mc.thePlayer.posZ + 1.0
        ).offset(
                -((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX(),
                -((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY(),
                -((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ()
        );
        boolean below = mc.thePlayer.getEntityBoundingBox().maxY < y;
        RenderUtil.drawBoundingBox(box, below ? 0 : 255, below ? 255 : 0, 0, 90, 1.0F);
    }

    private void sendNcpDamage(boolean requireGround) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || requireGround && !player.onGround) return;
        for (int i = 0; i < 65; i++) {
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.049, player.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false));
        }
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.1, player.posZ, true));
        player.motionX *= 0.1;
        player.motionZ *= 0.1;
        player.swingItem();
    }

    private void enableBoostHypixel(EntityPlayerSP player) {
        if (!player.onGround) return;
        for (int i = 0; i < 10; i++) PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true));
        double fallDistance = 3.0125;
        while (fallDistance > 0.0) {
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.0624986421, player.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.0625, player.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.0624986421, player.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.0000013579, player.posZ, false));
            fallDistance -= 0.0624986421;
        }
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true));
        player.jump();
        player.posY += 0.42F;
    }

    private void enableVerus(EntityPlayerSP player) {
        if (mc.theWorld.getCollidingBoundingBoxes(player, player.getEntityBoundingBox().offset(0.0, 3.0001, 0.0)).isEmpty()) {
            if (damage.getValue()) {
                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 3.0001, player.posZ, false));
            }
            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, true));
        }
        player.setPosition(player.posX, player.posY + yBoost.getValue(), player.posZ);
    }

    private void updateHypixel(EntityPlayerSP player) {
        long elapsed = System.currentTimeMillis() - modeTimer;
        if (hypixelBoost.getValue() && elapsed < hypixelBoostDelay.getValue()) {
            float remaining = (hypixelBoostDelay.getValue() - elapsed) / (float) hypixelBoostDelay.getValue();
            timer().setTimerSpeed(1.0F + hypixelBoostTimer.getValue() * remaining);
        } else {
            timer().setTimerSpeed(1.0F);
        }
        if (++tick >= 2) {
            player.setPosition(player.posX, player.posY + 1.0E-5, player.posZ);
            tick = 0;
        }
        player.stepHeight = 0.0F;
    }

    private void updateBoostHypixel(EntityPlayerSP player) {
        if (++tick >= 2) {
            player.setPosition(player.posX, player.posY + 1.0E-5, player.posZ);
            tick = 0;
        }
        player.motionY = 0.0;
        if (!isMoving()) {
            stopXZ();
            return;
        }

        double amplifier = 1.0;
        if (player.isPotionActive(Potion.moveSpeed)) {
            amplifier += 0.2 * (player.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1.0);
        }
        double baseSpeed = 0.29 * amplifier;
        if (boostState == 1) {
            moveSpeed = (player.isPotionActive(Potion.moveSpeed) ? 1.56 : 2.034) * baseSpeed;
        } else if (boostState == 2) {
            moveSpeed *= 2.16;
        } else if (boostState == 3) {
            moveSpeed = lastDistance - (player.ticksExisted % 2 == 0 ? 0.0103 : 0.0123) * (lastDistance - baseSpeed);
        } else {
            moveSpeed = lastDistance - lastDistance / 159.8;
        }
        boostState++;
        moveSpeed = Math.min(moveSpeed, 0.3);
        MoveUtil.setSpeed(moveSpeed, MoveUtil.getMoveYaw());
        player.stepHeight = 0.0F;
    }

    private void updateFreeHypixel(EntityPlayerSP player) {
        if (secondaryTick >= 10) {
            player.capabilities.isFlying = true;
            return;
        }
        player.rotationYaw = startYaw;
        player.rotationPitch = startPitch;
        stop();
        if (Math.round(startY * 1000.0) == Math.round(player.posY * 1000.0)) secondaryTick++;
    }

    private void updateMinesucht(EntityPlayerSP player) {
        if (!mc.gameSettings.keyBindForward.isKeyDown()) return;
        long now = System.currentTimeMillis();
        if (now - minesuchtTime > 99L) {
            Vec3 eyes = player.getPositionEyes(1.0F);
            Vec3 look = player.getLook(1.0F);
            Vec3 target = eyes.addVector(look.xCoord * 7.0, look.yCoord * 7.0, look.zCoord * 7.0);
            if (player.fallDistance > 0.8F) {
                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 50.0, player.posZ, false));
                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 20.0, player.posZ, true));
                player.fallDistance = 0.0F;
            }
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(target.xCoord, player.posY + 50.0, target.zCoord, true));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(target.xCoord, player.posY, target.zCoord, true));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false));
            minesuchtTime = now;
        } else {
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true));
        }
    }

    private void updateBlocksMc(EntityPlayerSP player, boolean blinkMode) {
        boolean openAbove = mc.theWorld.getCollidingBoundingBoxes(player,
                player.getEntityBoundingBox().offset(0.0, blinkMode ? 0.5 : 1.0, 0.0)).isEmpty();

        if (blocksFlying) {
            if (player.onGround && stopOnLanding.getValue() || !isMoving() && stopOnNoMove.getValue()) {
                setEnabled(false);
                return;
            }
        }

        if (!openAbove && !blocksFlying) {
            blocksNotUnder = true;
            if (blinkMode) {
                if (!blocksBlinked && Myau.blinkManager != null) {
                    blocksBlinked = Myau.blinkManager.setBlinkState(true, BlinkModules.FLY);
                }
            } else if (!blocksTeleported) {
                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY - 0.05, player.posZ, false));
                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false));
                blocksTeleported = true;
            }
            return;
        }

        boolean ready = blinkMode ? blocksBlinked : blocksTeleported;
        if (!ready) return;
        if (stable.getValue()) player.motionY = 0.0;
        if (!player.onGround && timerSlowed.getValue()) {
            int divisor = blinkMode ? 4 : 7;
            timer().setTimerSpeed(player.ticksExisted % divisor == 0 ? (blinkMode ? 0.45F : 0.415F) : (blinkMode ? 0.4F : 0.35F));
        } else {
            timer().setTimerSpeed(1.0F);
        }
        if (airTicks == 0 && blocksNotUnder) {
            strafe(boostSpeed.getValue() + extraBoost.getValue(), true);
            player.jump();
            blocksFlying = true;
            blocksNotUnder = false;
        } else if (airTicks == 1 && blocksFlying) {
            strafe(boostSpeed.getValue(), true);
        } else {
            strafeCurrent();
        }
    }

    private void updateMineSecure(EntityPlayerSP player) {
        player.capabilities.isFlying = false;
        player.motionY = mc.gameSettings.keyBindSneak.isKeyDown() ? 0.0 : -0.01;
        strafe(vanillaSpeed.getValue(), true);
        long now = System.currentTimeMillis();
        if (now - modeTimer < 150L || !mc.gameSettings.keyBindJump.isKeyDown()) return;
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 5.0, player.posZ, false));
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(0.5, -1000.0, 0.5, false));
        double yaw = Math.toRadians(player.rotationYaw);
        player.setPosition(player.posX - Math.sin(yaw) * 0.4, player.posY, player.posZ + Math.cos(yaw) * 0.4);
        modeTimer = now;
    }

    private void updateJetpack(EntityPlayerSP player) {
        if (!mc.gameSettings.keyBindJump.isKeyDown()) return;
        mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.FLAME.getParticleID(), player.posX, player.posY + 0.2, player.posZ,
                -player.motionX, -0.5, -player.motionZ);
        player.motionY += 0.15;
        player.motionX *= 1.1;
        player.motionZ *= 1.1;
    }

    private void updateFlag(EntityPlayerSP player) {
        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;
        double packetY = y + (mc.gameSettings.keyBindJump.isKeyDown() ? 1.5624 : 1.0E-8)
                - (mc.gameSettings.keyBindSneak.isKeyDown() ? 0.0624 : 2.0E-8);
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x + player.motionX * 999.0, packetY, z + player.motionZ * 999.0, true));
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x + player.motionX * 999.0, y - 6969.0, z + player.motionZ * 999.0, true));
        player.setPosition(x + player.motionX * 11.0, y, z + player.motionZ * 11.0);
        player.motionY = 0.0;
    }

    private void updateFireball(UpdateEvent event, EntityPlayerSP player) {
        int slot = findFireballSlot();
        if (slot < 0) return;
        if (player.onGround && !mc.theWorld.isAirBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ))) {
            firePosition = new BlockPos(player.posX, player.posY - 1.0, player.posZ);
        }
        if (fireballThrow.getValue() == 1 && !isNearEdge(edgeThreshold.getValue())) return;

        float yaw = invertYaw.getValue() ? player.rotationYaw + 180.0F : player.rotationYaw;
        float pitch = rotationPitch.getValue();
        if (pitchMode.getValue() == 1 && firePosition != null) {
            double x = firePosition.getX() + 0.5 - player.posX;
            double y = firePosition.getY() + 0.5 - (player.posY + player.getEyeHeight());
            double z = firePosition.getZ() + 0.5 - player.posZ;
            double horizontal = Math.hypot(x, z);
            yaw = (float) (Math.toDegrees(Math.atan2(z, x)) - 90.0);
            pitch = (float) -Math.toDegrees(Math.atan2(y, horizontal));
        }
        event.setRotation(yaw, pitch, 100);
        if (autoJump.getValue() && player.onGround && !wasFired) player.jump();
    }

    private void tickFireball(EntityPlayerSP player) {
        if (wasFired) {
            if (++firedTicks >= 2) setEnabled(false);
            return;
        }
        if (!isMoving() || fireballThrow.getValue() == 1 && !isNearEdge(edgeThreshold.getValue())) return;

        if (fireballStage == 0) {
            fireballStage = 1;
        } else if (fireballStage == 1) {
            throwFireball(player);
            fireballStage = 2;
        } else {
            wasFired = true;
            firedTicks = 0;
        }
    }

    private void throwFireball(EntityPlayerSP player) {
        int slot = findFireballSlot();
        if (slot < 0) return;
        int original = player.inventory.currentItem;
        int selection = autoFireball.getValue();
        if (selection == 0 && original != slot) return;

        if (selection == 1 || selection == 3) {
            player.inventory.currentItem = slot;
            PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
        } else if (selection == 2) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
        }

        if (swing.getValue()) player.swingItem();
        else PacketUtil.sendPacket(new C0APacketAnimation());
        ItemStack stack = player.inventory.getStackInSlot(slot);
        for (int i = 0; i < fireballTry.getValue(); i++) PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(stack));

        if (selection == 2 || selection == 3) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(original));
            if (selection == 3) player.inventory.currentItem = original;
        }
    }

    private void restoreFireballSlot() {
        fireballStage = firedTicks = 0;
        wasFired = false;
    }

    private int findFireballSlot() {
        if (mc.thePlayer == null) return -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() == Items.fire_charge) return slot;
        }
        return -1;
    }

    private boolean isNearEdge(float threshold) {
        EntityPlayerSP player = mc.thePlayer;
        double offset = Math.max(0.05, threshold - 0.95);
        AxisAlignedBB box = player.getEntityBoundingBox().expand(offset, 0.0, offset).offset(0.0, -0.2, 0.0);
        return mc.theWorld.getCollidingBoundingBoxes(player, box).isEmpty();
    }

    private void handleAacControlKey() {
        timer().setTimerSpeed(1.0F);
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            timer().setTimerSpeed(0.2F);
            ((IAccessorMinecraft) mc).setRightClickDelayTimer(0);
        }
    }

    private void handleVanillaKickBypass() {
        if (!vanillaKickBypass.getValue() || System.currentTimeMillis() - groundTimer < 1000L || mc.thePlayer == null) return;
        double ground = calculateGround() + 0.5;
        double y = mc.thePlayer.posY;
        while (y > ground) {
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true));
            if (y - 8.0 < ground) break;
            y -= 8.0;
        }
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, ground, mc.thePlayer.posZ, true));
        y = ground;
        while (y < mc.thePlayer.posY) {
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true));
            if (y + 8.0 > mc.thePlayer.posY) break;
            y += 8.0;
        }
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
        groundTimer = System.currentTimeMillis();
    }

    private double calculateGround() {
        AxisAlignedBB playerBox = mc.thePlayer.getEntityBoundingBox();
        double blockHeight = 0.05;
        double ground = mc.thePlayer.posY;
        while (ground > 0.0) {
            AxisAlignedBB box = new AxisAlignedBB(playerBox.maxX, ground + blockHeight, playerBox.maxZ,
                    playerBox.minX, ground, playerBox.minZ);
            if (mc.theWorld.checkBlockCollision(box)) {
                if (blockHeight <= 0.05) return ground + blockHeight;
                ground += blockHeight;
                blockHeight = 0.05;
            }
            ground -= blockHeight;
        }
        return 0.0;
    }

    private double verticalSpeed(float speed) {
        if (mc.gameSettings.keyBindJump.isKeyDown()) return speed;
        if (mc.gameSettings.keyBindSneak.isKeyDown()) return -speed;
        return 0.0;
    }

    private void strafe(float speed, boolean stopWhenIdle) {
        if (isMoving()) MoveUtil.setSpeed(speed, MoveUtil.getMoveYaw());
        else if (stopWhenIdle) stopXZ();
    }

    private void strafeCurrent() {
        if (isMoving()) MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
    }

    private boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F);
    }

    private void stopXZ() {
        if (mc.thePlayer == null) return;
        mc.thePlayer.motionX = 0.0;
        mc.thePlayer.motionZ = 0.0;
    }

    private void stop() {
        if (mc.thePlayer == null) return;
        stopXZ();
        mc.thePlayer.motionY = 0.0;
    }

    private IAccessorTimer timer() {
        return (IAccessorTimer) ((IAccessorMinecraft) mc).getTimer();
    }

    @Override
    public String[] getSuffix() {
        return new String[]{mode.getModeString()};
    }
}
