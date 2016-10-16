package mobi.omegacentauri.raspberryjammod.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.google.common.collect.Maps;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockNBT;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockState;
import mobi.omegacentauri.raspberryjammod.actions.SetBlocksNBT;
import mobi.omegacentauri.raspberryjammod.actions.SetBlocksState;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import mobi.omegacentauri.raspberryjammod.util.BlockState;
import mobi.omegacentauri.raspberryjammod.util.Location;
import mobi.omegacentauri.raspberryjammod.util.SetDimension;
import mobi.omegacentauri.raspberryjammod.util.Vec3w;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class APIRegistry {

	public static boolean initd = false;

	@FunctionalInterface
	public static interface CommandRunnable {
		void execute(String args, Scanner scan, MCEventHandler eventHandler);
	}

	public static Map<String, CommandRunnable> commands = Maps.newHashMap();

	public static void registerCommand(String name, CommandRunnable executableCode) {
		try {
			commands.put(name, executableCode);
			RaspberryJamMod.logger.error("Registering Command: " + name);
		} catch (Exception e) {
			RaspberryJamMod.logger.error("Command already registered");
		}
	}

	public static CommandRunnable getExectuableCode(String name) {
		return commands.get(name);
	}

	public static void setExectuableCode(String name, CommandRunnable code) {
		commands.replace(name, code);
	}

	public static boolean runCommand(String name, String args, Scanner scan, MCEventHandler eventHandler) {
		try {
			RaspberryJamMod.logger.error("Executing Command: " + name);
			commands.get(name).execute(args, scan, eventHandler);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void init() {
		Python2MinecraftApi.init();
	}

	public static class Python2MinecraftApi {

		public static class ChatDescription {
			int id;
			String message;

			public ChatDescription(int entityId, String message) {
				id = entityId;
				this.message = message;
			}
		}

		static class HitDescription {
			private String description;

			public HitDescription(World[] worlds, PlayerInteractEvent event) {
				Vec3i pos = Location.encodeVec3i(worlds, event.entityPlayer.getEntityWorld(), event.pos.getX(),
						event.pos.getY(), event.pos.getZ());
				description = "" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + numericFace(event.face)
						+ "," + event.entity.getEntityId();
			}

			public String getDescription() {
				return description;
			}

			private int numericFace(EnumFacing face) {
				switch (face) {
				case DOWN:
					return 0;
				case UP:
					return 1;
				case NORTH:
					return 2;
				case SOUTH:
					return 3;
				case WEST:
					return 4;
				case EAST:
					return 5;
				default:
					return 7;
				}
			}
		}

		// world.checkpoint.save/restore, player.setting,
		// world.setting(nametags_visible,*),
		// camera.setFixed() unsupported
		// camera.setNormal(id) and camera.setFollow(id) uses spectating, and so
		// it
		// moves the
		// player along with the entity that was set as camera
		protected static final String CHAT = "chat.post";
		protected static final String SETBLOCK = "world.setBlock";
		protected static final String SETBLOCKS = "world.setBlocks";
		protected static final String GETBLOCK = "world.getBlock";
		protected static final String GETBLOCKWITHDATA = "world.getBlockWithData";
		protected static final String GETBLOCKS = "world.getBlocks";
		protected static final String GETBLOCKSWITHDATA = "world.getBlocksWithData";
		protected static final String GETHEIGHT = "world.getHeight";
		protected static final String WORLDSPAWNENTITY = "world.spawnEntity";
		protected static final String WORLDSPAWNPARTICLE = "world.spawnParticle";
		protected static final String WORLDDELETEENTITY = "world.removeEntity";
		protected static final String WORLDGETPLAYERIDS = "world.getPlayerIds";

		protected static final String WORLDGETPLAYERID = "world.getPlayerId";
		protected static final String WORLDSETTING = "world.setting";

		// EXPERIMENTAL AND UNSUPPORTED
		protected static final String GETLIGHTLEVEL = "block.getLightLevel";
		protected static final String SETLIGHTLEVEL = "block.setLightLevel";

		protected static final String EVENTSBLOCKHITS = "events.block.hits";
		protected static final String EVENTSCHATPOSTS = "events.chat.posts";

		protected static final String EVENTSCLEAR = "events.clear";
		protected static final String EVENTSSETTING = "events.setting";
		// camera.*
		protected static final String SETFOLLOW = "setFollow";
		protected static final String SETNORMAL = "setNormal";
		protected static final String GETENTITYID = "getEntityId";

		// EXPERIMENTAL AND UNSUPPORTED
		protected static final String SETDEBUG = "setDebug";
		protected static final String SETDISTANCE = "setDistance";

		// player.* or entity.*
		protected static final String GETDIRECTION = "getDirection";
		protected static final String GETPITCH = "getPitch";
		protected static final String GETPOS = "getPos";
		protected static final String GETROTATION = "getRotation";
		protected static final String GETTILE = "getTile";

		// EXPERIMENTAL AND UNSUPPORTED
		protected static final String SETDIMENSION = "setDimension";

		protected static final String SETDIRECTION = "setDirection";
		protected static final String SETPITCH = "setPitch";
		protected static final String SETPOS = "setPos";
		protected static final String SETROTATION = "setRotation";

		protected static final String SETTILE = "setTile";

		protected static final String GETNAME = "getNameAndUUID";
		protected static final float TOO_SMALL = (float) 1e-9;
		protected static final int MAX_CHATS = 512;
		protected static final int MAX_HITS = 512;

		protected static boolean useClientMethods = false;

		public static boolean doesUseClientMethods() {
			return useClientMethods;
		}

		public static void setUseClientMethods(boolean state) {
			useClientMethods = state;
		}

		protected static String getRest(Scanner scan) {
			StringBuilder out = new StringBuilder();

			while (scan.hasNext()) {
				if (out.length() > 0) {
					out.append(",");
				}
				out.append(scan.next());
			}
			return out.toString();
		}

		public static void globalMessage(String message) {
			for (World w : MinecraftServer.getServer().worldServers) {
				for (EntityPlayer p : w.playerEntities) {
					p.addChatComponentMessage(new ChatComponentText(message));
				}
			}
		}

		public static int trunc(double x) {
			return (int) Math.floor(x);
		}

		protected static World[] serverWorlds;
		protected static MCEventHandler eventHandler;
		protected static boolean listening = true;
		protected static Minecraft mc;
		protected static PrintWriter writer = null;
		protected static boolean includeNBTWithData = false;
		protected static boolean havePlayer;
		protected static int playerId;

		protected static EntityPlayerMP playerMP;

		protected static List<HitDescription> hits = new LinkedList<HitDescription>();

		protected static List<ChatDescription> chats = new LinkedList<ChatDescription>();

		private volatile static boolean restrictToSword = true;

		private volatile static boolean detectLeftClick = RaspberryJamMod.leftClickToo;

		public static void addChatDescription(ChatDescription cd) {
			synchronized (chats) {
				if (chats.size() >= MAX_CHATS) {
					chats.remove(0);
				}
				chats.add(cd);
			}
		}

		protected static void chat(String msg) {
			if (!RaspberryJamMod.integrated || RaspberryJamMod.globalChatMessages && !useClientMethods) {
				globalMessage(msg);
			} else {
				mc.thePlayer.addChatComponentMessage(new ChatComponentText(msg));
			}
		}

		public static void clearAllEvents() {
			hits.clear();
			chats.clear();
		}

		public void clearChats() {
			synchronized (chats) {
				chats.clear();
			}
		}

		public void clearHits() {
			synchronized (hits) {
				hits.clear();
			}
		}

		protected static void entityCommand(int id, String cmd, Scanner scan) {
			if (cmd.equals(GETPOS)) {
				entityGetPos(id);
			} else if (cmd.equals(GETTILE)) {
				entityGetTile(id);
			} else if (cmd.equals(GETROTATION)) {
				entityGetRotation(id);
			} else if (cmd.equals(SETROTATION)) {
				entitySetRotation(id, scan.nextFloat());
			} else if (cmd.equals(GETPITCH)) {
				entityGetPitch(id);
			} else if (cmd.equals(SETPITCH)) {
				entitySetPitch(id, scan.nextFloat());
			} else if (cmd.equals(GETDIRECTION)) {
				entityGetDirection(id);
			} else if (cmd.equals(SETDIRECTION)) {
				entitySetDirection(id, scan);
			} else if (cmd.equals(SETTILE)) {
				entitySetTile(id, scan);
			} else if (cmd.equals(SETPOS)) {
				entitySetPos(id, scan);
			} else if (cmd.equals(SETDIMENSION)) {
				entitySetDimension(id, scan.nextInt());
			} else if (cmd.equals(GETNAME)) {
				entityGetNameAndUUID(id);
			} else {
				unknownCommand();
			}
		}

		protected static void entityGetDirection(int id) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				// sendLine(e.getLookVec());
				double pitch = (e.rotationPitch * Math.PI) / 180.;
				double yaw = (e.rotationYaw * Math.PI) / 180.;
				double x = Math.cos(-pitch) * Math.sin(-yaw);
				double z = Math.cos(-pitch) * Math.cos(-yaw);
				double y = Math.sin(-pitch);
				sendLine(new Vec3(x, y, z));
			}
		}

		protected static void entityGetNameAndUUID(int id) {
			Entity e = getServerEntityByID(id);
			if (e == null) {
				fail("Unknown entity");
			} else {
				sendLine(e.getName() + "," + e.getUniqueID());
			}
		}

		protected static void entityGetPitch(int id) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				sendLine(normalizeAngle(e.rotationPitch));
			}
		}

		protected static void entityGetPos(int id) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				World w = e.getEntityWorld();
				Vec3 pos0 = e.getPositionVector();
				while (w != e.getEntityWorld()) {
					// Rare concurrency issue: entity switched worlds between
					// getting w and pos0.
					// To be somewhat safe, let's sleep for approximately a
					// server
					// tick and get
					// everything again.
					try {
						Thread.sleep(50);
					} catch (Exception exc) {
					}
					w = e.getEntityWorld();
					pos0 = e.getPositionVector();
				}

				Vec3 pos = Location.encodeVec3(serverWorlds, w, pos0);
				sendLine(pos);
			}
		}

		protected static void entityGetRotation(int id) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				sendLine(normalizeAngle(e.rotationYaw));
			}
		}

		protected static void entityGetTile(int id) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				World w = e.getEntityWorld();
				e.getPositionVector();

				while (w != e.getEntityWorld()) {
					// Rare concurrency issue: entity switched worlds between
					// getting w and pos0.
					// To be somewhat safe, let's sleep for approximately a
					// server
					// tick and get
					// everything again.
					try {
						Thread.sleep(50);
					} catch (Exception exc) {
					}
					w = e.getEntityWorld();
					e.getPositionVector();
				}

				Vec3 pos = Location.encodeVec3(serverWorlds, w, e.getPositionVector());
				sendLine("" + trunc(pos.xCoord) + "," + trunc(pos.yCoord) + "," + trunc(pos.zCoord));
			}
		}

		protected static void entitySetDimension(int id, int dimension) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				eventHandler.queueServerAction(new SetDimension(e, dimension));
			}
		}

		protected static void entitySetDirection(Entity e, double x, double y, double z) {
			double xz = Math.sqrt((x * x) + (z * z));

			if (xz >= TOO_SMALL) {
				float yaw = (float) ((Math.atan2(-x, z) * 180) / Math.PI);
				e.setRotationYawHead(yaw);
				e.rotationYaw = yaw;
			}

			if (((x * x) + (y * y) + (z * z)) >= (TOO_SMALL * TOO_SMALL)) {
				e.rotationPitch = (float) ((Math.atan2(-y, xz) * 180) / Math.PI);
			}
		}

		protected static void entitySetDirection(int id, Scanner scan) {
			double x = scan.nextDouble();
			double y = scan.nextDouble();
			double z = scan.nextDouble();
			Entity e = getServerEntityByID(id);
			if (e != null) {
				entitySetDirection(e, x, y, z);
			}

			if (!RaspberryJamMod.integrated) {
				return;
			}

			e = mc.theWorld.getEntityByID(id);
			if (e != null) {
				entitySetDirection(e, x, y, z);
			}
		}

		protected static void entitySetPitch(int id, float angle) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				e.rotationPitch = angle;
			}

			if (!RaspberryJamMod.integrated) {
				return;
			}

			e = mc.theWorld.getEntityByID(id);
			if (e != null) {
				e.rotationPitch = angle;
			}
		}

		protected static void entitySetPos(int id, Scanner scan) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				float serverYaw = 0f;
				serverYaw = e.rotationYaw;

				double x = scan.nextDouble();
				double y = scan.nextDouble();
				double z = scan.nextDouble();
				Vec3w pos = Location.decodeVec3w(serverWorlds, x, y, z);
				if (pos.getWorld() != e.getEntityWorld()) {
					// e.setWorld(pos.world);
					RaspberryJamMod.logger.info("World change unsupported");
					// TODO: implement moving between worlds
					return;
				}
				e.setPositionAndUpdate(pos.xCoord, pos.yCoord, pos.zCoord);
				e.setRotationYawHead(serverYaw);

				if (!RaspberryJamMod.integrated) {
					return;
				}

				e = mc.theWorld.getEntityByID(id);
				if (e != null) {
					e.rotationYaw = serverYaw;
					e.setRotationYawHead(serverYaw);
				}
			}
		}

		protected static void entitySetRotation(int id, float angle) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				e.rotationYaw = angle;
				e.setRotationYawHead(angle);
			}

			if (!RaspberryJamMod.integrated) {
				return;
			}

			e = mc.theWorld.getEntityByID(id);
			if (e != null) {
				e.rotationYaw = angle;
				e.setRotationYawHead(angle);
			}
		}

		protected static void entitySetTile(int id, Scanner scan) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				float serverYaw = 0f;
				if (e != null) {
					serverYaw = e.rotationYaw;
					Location pos = getBlockLocation(scan);
					if (pos.getWorld() != e.getEntityWorld()) {
						// TODO: implement moving between worlds
						return;
					}
					e.setPositionAndUpdate(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					e.setRotationYawHead(serverYaw);
				}

				if (!RaspberryJamMod.integrated) {
					return;
				}

				e = mc.theWorld.getEntityByID(id);
				if (e != null) {
					e.rotationYaw = serverYaw;
					e.setRotationYawHead(serverYaw);
				}
			}
		}

		public int eventCount() {
			synchronized (hits) {
				return hits.size();
			}
		}

		protected static void fail(String string) {
			System.err.println("Error: " + string);
			sendLine("Fail");
		}

		protected static Location getBlockLocation(Scanner scan) {
			int x = scan.nextInt();
			int y = scan.nextInt();
			int z = scan.nextInt();
			return Location.decodeLocation(serverWorlds, x, y, z);
		}

		public static String getChatsAndClear() {
			StringBuilder out = new StringBuilder();

			synchronized (chats) {
				hits.size();
				for (ChatDescription c : chats) {
					if (out.length() > 0) {
						out.append("|");
					}
					out.append(c.id);
					out.append(",");
					out.append(c.message.replace("&", "&amp;").replace("|", "&#124;"));
				}
				chats.clear();
			}

			return out.toString();
		}

		public static String getHitsAndClear() {
			String out = "";

			synchronized (hits) {
				hits.size();
				for (HitDescription e : hits) {
					if (out.length() > 0) {
						out += "|";
					}
					out += e.getDescription();
				}
				hits.clear();
			}

			return out;
		}

		protected static Entity getServerEntityByID(int id) {
			if (!useClientMethods) {
				if (id == playerId) {
					return playerMP;
				}
				for (World w : serverWorlds) {
					Entity e = w.getEntityByID(id);
					if (e != null) {
						return e;
					}
				}
				fail("Cannot find entity " + id);
				return null;
			} else {
				Entity e = mc.theWorld.getEntityByID(id);
				if (e == null) {
					fail("Cannot find entity " + id);
				}
				return e;
			}
		}

		public static PrintWriter getWriter() {
			return writer;
		}

		protected static boolean holdingSword(EntityPlayer player) {
			ItemStack item = player.getHeldItem();
			if (item != null) {
				return item.getItem() instanceof ItemSword;
			}
			return false;
		}

		protected static float normalizeAngle(float angle) {
			angle = angle % 360;
			if (angle <= -180) {
				angle += 360;
			}
			if (angle > 180) {
				angle -= 360;
			}
			return angle;
		}

		public static void onClick(PlayerInteractEvent event) {
			if ((event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
					|| (detectLeftClick && (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))) {
				if (!restrictToSword || holdingSword(event.entityPlayer)) {
					synchronized (hits) {
						if (hits.size() >= MAX_HITS) {
							hits.remove(0);
						}
						hits.add(new HitDescription(eventHandler.getWorlds(), event));
					}
				}
			}
			if (eventHandler.stopChanges) {
				event.setCanceled(true);
			}
		}

		protected static void removeEntity(int id) {
			Entity e = getServerEntityByID(id);
			if (e != null) {
				e.getEntityWorld().removeEntity(e);
			}
		}

		protected static void sendLine(BlockPos pos) {
			sendLine("" + pos.getX() + "," + pos.getY() + "," + pos.getZ());
		}

		protected static void sendLine(double x) {
			sendLine(Double.toString(x));
		}

		protected static void sendLine(int x) {
			sendLine(Integer.toString(x));
		}

		static void sendLine(String string) {
			try {
				getWriter().print(string + "\n");
				getWriter().flush();
			} catch (Exception e) {
			}
		}

		protected static void sendLine(Vec3 v) {
			sendLine("" + v.xCoord + "," + v.yCoord + "," + v.zCoord);
		}

		static boolean refresh() {
			if (!useClientMethods) {
				serverWorlds = MinecraftServer.getServer().worldServers;

				if (serverWorlds == null) {
					fail("Worlds not available");
					return false;
				}

				if (!havePlayer) {
					if (RaspberryJamMod.integrated) {
						mc = Minecraft.getMinecraft();

						if (mc == null) {
							fail("Minecraft client not yet available");
						}

						if (mc.thePlayer == null) {
							fail("Client player not available");
							return false;
						}
						playerId = mc.thePlayer.getEntityId();
						for (World w : serverWorlds) {
							Entity e = w.getEntityByID(playerId);
							if (e != null) {
								playerMP = (EntityPlayerMP) e;
							}
						}
					} else {
						playerMP = null;
						int firstId = 0;

						for (World w : serverWorlds) {
							for (EntityPlayer p : w.playerEntities) {
								int id = p.getEntityId();
								if ((playerMP == null) || (id < firstId)) {
									firstId = id;
									playerMP = (EntityPlayerMP) p;
								}
							}
						}
					}
					if (playerMP == null) {
						fail("Player not found");
						return false;
					}
					havePlayer = true;
				}
				return true;
			} else {
				if (!RaspberryJamMod.integrated) {
					fail("This requires the client");
					return false;
				}

				mc = Minecraft.getMinecraft();
				if (mc == null) {
					fail("Minecraft client not yet available");
					return false;
				}

				serverWorlds = new World[] { mc.theWorld };

				if (mc.thePlayer == null) {
					fail("Client player not available");
					return false;
				}

				playerId = mc.thePlayer.getEntityId();
				playerMP = null;
				havePlayer = true;
				return true;
			}
		}

		public static void setWriter(PrintWriter p_writer) {
			writer = p_writer;
		}

		protected static void spawnEntity(Scanner scan) {
			String entityId = scan.next();
			double x0 = scan.nextDouble();
			double y0 = scan.nextDouble();
			double z0 = scan.nextDouble();
			Vec3w pos = Location.decodeVec3w(serverWorlds, x0, y0, z0);
			String tagString = getRest(scan);
			Entity entity;
			if (tagString.length() > 0) {
				NBTTagCompound tags;
				try {
					tags = JsonToNBT.getTagFromJson(tagString);
				} catch (NBTException e) {
					fail("Cannot parse tags");
					return;
				}
				tags.setString("id", entityId);
				entity = EntityList.createEntityFromNBT(tags, pos.getWorld());
			} else {
				entity = EntityList.createEntityByName(entityId, pos.getWorld());
			}

			if (entity == null) {
				fail("Cannot create entity");
			} else {
				entity.setPositionAndUpdate(pos.xCoord, pos.yCoord, pos.zCoord);
				pos.getWorld().spawnEntityInWorld(entity);
				sendLine(entity.getEntityId());
			}
		}

		protected static void spawnParticle(Scanner scan) {
			if (!useClientMethods) {
			String particleName = scan.next();
			double x0 = scan.nextDouble();
			double y0 = scan.nextDouble();
			double z0 = scan.nextDouble();
			Vec3w pos = Location.decodeVec3w(serverWorlds, x0, y0, z0);
			double dx = scan.nextDouble();
			double dy = scan.nextDouble();
			double dz = scan.nextDouble();
			double speed = scan.nextDouble();
			int count = scan.nextInt();

			int[] extras = null;
			EnumParticleTypes particle = null;
			for (EnumParticleTypes e : EnumParticleTypes.values()) {
				if (e.getParticleName().equals(particleName)) {
					particle = e;
					extras = new int[e.getArgumentCount()];
					try {
						for (int i = 0; i < extras.length; i++) {
							extras[i] = scan.nextInt();
						}
					} catch (Exception exc) {
					}
					break;
				}
			}
			if (particle == null) {
				fail("Cannot find particle type");
			} else {
				((WorldServer) pos.getWorld()).spawnParticle(particle, false, pos.xCoord, pos.yCoord, pos.zCoord, count,
						dx, dy, dz, speed, extras);
			}
			}else {
				String particleName = scan.next();
				double x0 = scan.nextDouble();
				double y0 = scan.nextDouble();
				double z0 = scan.nextDouble();
				Vec3w pos = Location.decodeVec3w(serverWorlds, x0, y0, z0);
				double dx = scan.nextDouble();
				double dy = scan.nextDouble();
				double dz = scan.nextDouble();
				scan.nextDouble();
				int count = scan.nextInt();

				int[] extras = null;
				EnumParticleTypes particle = null;
				for (EnumParticleTypes e : EnumParticleTypes.values()) {
					if (e.getParticleName().equals(particleName)) {
						particle = e;
						extras = new int[e.getArgumentCount()];
						try {
							for (int i = 0; i < extras.length; i++) {
								extras[i] = scan.nextInt();
							}
						} catch (Exception exc) {
						}
						break;
					}
				}
				if (particle == null) {
					fail("Cannot find particle type");
				} else {
					for (int i = 0; i < count; i++) {
						pos.getWorld().spawnParticle(particle, false, pos.xCoord, pos.yCoord, pos.zCoord, dx, dy, dz, extras);
					}
				}
			}
		}

		protected static void unknownCommand() {
			fail("unknown command");
		}

		public static void init() {
			APIRegistry.registerCommand(SETBLOCK, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				Location pos = getBlockLocation(scan);
				short id = scan.nextShort();
				short meta = scan.hasNextShort() ? scan.nextShort() : 0;
				String tagString = getRest(scan);

				SetBlockState setState;

				if (tagString.contains("{")) {
					try {
						setState = new SetBlockNBT(pos, id, meta, JsonToNBT.getTagFromJson(tagString));
					} catch (NBTException e) {
						System.err.println("Cannot parse NBT");
						setState = new SetBlockState(pos, id, meta);
					}
				} else {
					setState = new SetBlockState(pos, id, meta);
				}

				eventHandler.queueServerAction(setState);
			});
			APIRegistry.registerCommand(GETBLOCK, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				Location pos = getBlockLocation(scan);
				int id = eventHandler.getBlockId(pos);

				sendLine(id);
			});
			APIRegistry.registerCommand(GETBLOCKWITHDATA, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				if (includeNBTWithData) {
					sendLine(eventHandler.describeBlockState(getBlockLocation(scan)));
				} else {
					BlockState state = eventHandler.getBlockState(getBlockLocation(scan));
					sendLine("" + state.id + "," + state.meta);
				}
			});
			APIRegistry.registerCommand(GETBLOCKS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				Location pos1 = getBlockLocation(scan);
				Location pos2 = getBlockLocation(scan);
				StringBuilder out = new StringBuilder();
				int x1 = Math.min(pos1.getX(), pos2.getX());
				int x2 = Math.max(pos1.getX(), pos2.getX());
				int y1 = Math.min(pos1.getY(), pos2.getY());
				int y2 = Math.max(pos1.getY(), pos2.getY());
				int z1 = Math.min(pos1.getZ(), pos2.getZ());
				int z2 = Math.max(pos1.getZ(), pos2.getZ());
				for (int y = y1; y <= y2; y++) {
					for (int x = x1; x <= x2; x++) {
						for (int z = z1; z <= z2; z++) {
							if (out.length() != 0) {
								out.append(",");
							}
							out.append(eventHandler.getBlockId(new Location(pos1.getWorld(), x, y, z)));
						}
					}
				}
				sendLine(out.toString());
			});
			APIRegistry.registerCommand(GETBLOCKSWITHDATA, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				Location pos1 = getBlockLocation(scan);
				Location pos2 = getBlockLocation(scan);
				StringBuilder out = new StringBuilder();
				int x1 = Math.min(pos1.getX(), pos2.getX());
				int x2 = Math.max(pos1.getX(), pos2.getX());
				int y1 = Math.min(pos1.getY(), pos2.getY());
				int y2 = Math.max(pos1.getY(), pos2.getY());
				int z1 = Math.min(pos1.getZ(), pos2.getZ());
				int z2 = Math.max(pos1.getZ(), pos2.getZ());
				for (int y = y1; y <= y2; y++) {
					for (int x = x1; x <= x2; x++) {
						for (int z = z1; z <= z2; z++) {
							if (out.length() != 0) {
								out.append("|");
							}
							Location pos = new Location(pos1.getWorld(), x, y, z);
							if (includeNBTWithData) {
								out.append(eventHandler.describeBlockState(pos).replace("&", "&amp;").replace("|",
										"&#124;"));
							} else {
								BlockState state = eventHandler.getBlockState(pos);
								out.append("" + state.id + "," + state.meta);
							}
						}
					}
				}
				sendLine(out.toString());
			});
			APIRegistry.registerCommand(GETHEIGHT, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				int x0 = scan.nextInt();
				int z0 = scan.nextInt();
				Location pos = Location.decodeLocation(serverWorlds, x0, 0, z0);
				Chunk chunk = serverWorlds[0].getChunkFromBlockCoords(pos);
				int h = chunk.getHeight(pos);
				int x = pos.getX();
				int z = pos.getZ();
				for (int y = serverWorlds[0].getHeight(); y >= h; y--) {
					Block b = chunk.getBlock(x, y, z);
					if (b != Blocks.air) {
						h = y;
						break;
					}
				}

				h -= serverWorlds[0].getSpawnPoint().getY();

				sendLine(h);
			});
			APIRegistry.registerCommand(GETLIGHTLEVEL, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				sendLine(Block.getBlockById(scan.nextInt()).getLightValue() / 15.);
			});
			APIRegistry.registerCommand(SETLIGHTLEVEL, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				int id = scan.nextInt();
				float value = scan.nextFloat();
				Block.getBlockById(id).setLightLevel(value);
			});
			APIRegistry.registerCommand(SETBLOCKS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				Location pos1 = getBlockLocation(scan);
				Location pos2 = getBlockLocation(scan);

				short id = scan.nextShort();
				short meta = scan.hasNextShort() ? scan.nextShort() : 0;

				String tagString = getRest(scan);

				SetBlocksState setState;

				if (tagString.contains("{")) {
					try {
						setState = new SetBlocksNBT(pos1, pos2, id, meta, JsonToNBT.getTagFromJson(tagString));
					} catch (NBTException e) {
						setState = new SetBlocksState(pos1, pos2, id, meta);
					}
				} else {
					setState = new SetBlocksState(pos1, pos2, id, meta);
				}

				eventHandler.queueServerAction(setState);
			});
			APIRegistry.registerCommand(CHAT, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				chat(args);
			});
			APIRegistry.registerCommand(WORLDGETPLAYERIDS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				List<Integer> players = new ArrayList<Integer>();
				for (World w : serverWorlds) {
					for (EntityPlayer p : w.playerEntities) {
						players.add(p.getEntityId());
					}
				}
				Collections.sort(players);

				String ids = "";
				for (Integer id : players) {
					if (ids.length() > 0) {
						ids += "|";
					}
					ids += id;
				}
				sendLine(ids);
			});
			APIRegistry.registerCommand(WORLDGETPLAYERID, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				if (scan.hasNext()) {
					String name = scan.next();
					for (World w : serverWorlds) {
						for (EntityPlayer p : w.playerEntities) {
							if (p.getName().equals(name)) {
								sendLine(p.getEntityId());
								return;
							}
						}
					}
					fail("Unknown player");
				} else {
					// unofficial API to get current player ID
					sendLine(playerId);
				}
			});
			APIRegistry.registerCommand(WORLDDELETEENTITY, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				removeEntity(scan.nextInt());
			});
			APIRegistry.registerCommand(WORLDSPAWNENTITY, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				spawnEntity(scan);
			});
			APIRegistry.registerCommand(WORLDSPAWNPARTICLE,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						spawnParticle(scan);
					});
			APIRegistry.registerCommand(EVENTSCLEAR, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				clearAllEvents();
			});
			APIRegistry.registerCommand(EVENTSBLOCKHITS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				sendLine(getHitsAndClear());
			});
			APIRegistry.registerCommand(EVENTSCHATPOSTS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				sendLine(getChatsAndClear());
			});
			APIRegistry.registerCommand(WORLDSETTING, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				String setting = scan.next();
				if (setting.equals("world_immutable")) {
					eventHandler.setStopChanges(scan.nextInt() != 0);
				} else if (setting.equals("include_nbt_with_data")) {
					includeNBTWithData = (scan.nextInt() != 0);
				} else if (setting.equals("pause_drawing")) {
					eventHandler.setPause(scan.nextInt() != 0);
					// name_tags not supported
				}
			});
			APIRegistry.registerCommand(EVENTSSETTING, (String args, Scanner scan, MCEventHandler eventHandler) -> {
				String setting = scan.next();
				if (setting.equals("restrict_to_sword")) {
					restrictToSword = (scan.nextInt() != 0);
				} else if (setting.equals("detect_left_click")) {
					detectLeftClick = (scan.nextInt() != 0);
				}
			});

			// player
			APIRegistry.registerCommand("player." + GETPOS,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetPos(playerId);
					});
			APIRegistry.registerCommand("player." + GETTILE,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetTile(playerId);
					});
			APIRegistry.registerCommand("player." + GETROTATION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetRotation(playerId);
					});
			APIRegistry.registerCommand("player." + SETROTATION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetRotation(playerId, scan.nextFloat());
					});
			APIRegistry.registerCommand("player." + GETPITCH,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetPitch(playerId);
					});
			APIRegistry.registerCommand("player." + SETPITCH,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetPitch(playerId, scan.nextFloat());
					});
			APIRegistry.registerCommand("player." + GETDIRECTION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetDirection(playerId);
					});
			APIRegistry.registerCommand("player." + SETDIRECTION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetDirection(playerId, scan);
					});
			APIRegistry.registerCommand("player." + SETTILE,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetTile(playerId, scan);
					});
			APIRegistry.registerCommand("player." + SETPOS,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetPos(playerId, scan);
					});
			APIRegistry.registerCommand("player." + SETDIMENSION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetDimension(playerId, scan.nextInt());
					});
			APIRegistry.registerCommand("player." + GETNAME,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetNameAndUUID(playerId);
					});

			// entity
			APIRegistry.registerCommand("entity." + GETPOS,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetPos(scan.nextInt());
					});
			APIRegistry.registerCommand("entity." + GETTILE,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetTile(scan.nextInt());
					});
			APIRegistry.registerCommand("entity." + GETROTATION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetRotation(scan.nextInt());
					});
			APIRegistry.registerCommand("entity." + SETROTATION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetRotation(scan.nextInt(), scan.nextFloat());
					});
			APIRegistry.registerCommand("entity." + GETPITCH,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetPitch(scan.nextInt());
					});
			APIRegistry.registerCommand("entity." + SETPITCH,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetPitch(scan.nextInt(), scan.nextFloat());
					});
			APIRegistry.registerCommand("entity." + GETDIRECTION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetDirection(scan.nextInt());
					});
			APIRegistry.registerCommand("entity." + SETDIRECTION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetDirection(scan.nextInt(), scan);
					});
			APIRegistry.registerCommand("entity." + SETTILE,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetTile(scan.nextInt(), scan);
					});
			APIRegistry.registerCommand("entity." + SETPOS,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetPos(scan.nextInt(), scan);
					});
			APIRegistry.registerCommand("entity." + SETDIMENSION,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entitySetDimension(scan.nextInt(), scan.nextInt());
					});
			APIRegistry.registerCommand("entity." + GETNAME,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						entityGetNameAndUUID(scan.nextInt());
					});
			APIRegistry.registerCommand("camera." + GETENTITYID,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						sendLine(playerMP.getSpectatingEntity().getEntityId());
					});
			APIRegistry.registerCommand("camera." + SETFOLLOW,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						if (!RaspberryJamMod.integrated) {
							return;
						}

						mc.gameSettings.debugCamEnable = false;

						if (playerMP != null) {
							if (!scan.hasNext()) {
								playerMP.setSpectatingEntity(null);
							} else {
								Entity entity = getServerEntityByID(scan.nextInt());
								if (entity != null) {
									playerMP.setSpectatingEntity(entity);
								}
							}
						}
						mc.gameSettings.thirdPersonView = 1;
						mc.entityRenderer.loadEntityShader((Entity) null);
					});
			APIRegistry.registerCommand("camera." + SETNORMAL,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						if (!RaspberryJamMod.integrated) {
							return;
						}

						mc.gameSettings.debugCamEnable = false;

						if (playerMP != null) {
							if (!scan.hasNext()) {
								playerMP.setSpectatingEntity(null);
							} else {
								Entity entity = getServerEntityByID(scan.nextInt());
								if (entity != null) {
									playerMP.setSpectatingEntity(entity);
								}
							}
						}
						mc.gameSettings.thirdPersonView = 0;
						mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
					});
			APIRegistry.registerCommand("camera." + SETDEBUG,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						if (!RaspberryJamMod.integrated) {
							return;
						}

						mc.gameSettings.debugCamEnable = true;
					});
			APIRegistry.registerCommand("camera." + SETDISTANCE,
					(String args, Scanner scan, MCEventHandler eventHandler) -> {
						Float d = scan.nextFloat();
						Class c = net.minecraft.client.renderer.EntityRenderer.class;
						try {
							Field f = c.getDeclaredField("thirdPersonDistance");
							f.setAccessible(true);
							f.set(mc.entityRenderer, d);
						} catch (Exception e) {
							RaspberryJamMod.logger.error("" + e);
						}
						try {
							Field f = c.getDeclaredField("thirdPersonDistanceTemp");
							f.setAccessible(true);
							f.set(mc.entityRenderer, d);
						} catch (Exception e) {
							RaspberryJamMod.logger.error("" + e);
						}
					});
		}
	}

}
