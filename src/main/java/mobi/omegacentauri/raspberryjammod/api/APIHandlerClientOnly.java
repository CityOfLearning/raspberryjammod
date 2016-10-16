package mobi.omegacentauri.raspberryjammod.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import mobi.omegacentauri.raspberryjammod.util.Location;
import mobi.omegacentauri.raspberryjammod.util.Vec3w;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

// This class is meant to provide most of the APIHandler facility while one is connected to a
// server. Of course, any block changes won't get written back to the server.

public class APIHandlerClientOnly extends APIHandler {

	public APIHandlerClientOnly(MCEventHandler eventHandler, PrintWriter writer) throws IOException {
		super(eventHandler, writer);
		APIRegistry.Python2MinecraftApi.setUseClientMethods(true);
	}
}
