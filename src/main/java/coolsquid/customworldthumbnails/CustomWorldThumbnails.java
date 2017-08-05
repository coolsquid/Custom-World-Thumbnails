package coolsquid.customworldthumbnails;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

@Mod(modid = CustomWorldThumbnails.MODID, name = CustomWorldThumbnails.NAME, version = CustomWorldThumbnails.VERSION, dependencies = CustomWorldThumbnails.DEPENDENCIES, updateJSON = CustomWorldThumbnails.UPDATE_JSON)
public class CustomWorldThumbnails {

	public static final String MODID = "customworldthumbnails";
	public static final String NAME = "Custom World Thumbnails";
	public static final String VERSION = "1.0.0";
	public static final String DEPENDENCIES = "required-after:forge@[14.21.1.2387,)";
	public static final String UPDATE_JSON = "https://coolsquid.me/api/version/customworldthumbnails.json";

	public static final KeyBinding KEY = new KeyBinding("Use screenshot as thumbnail", Keyboard.KEY_P,
			"key.categories.misc");
	private static final File THUMBNAIL_FILE = new File(Loader.instance().getConfigDir(), "thumbnail.png");

	public static final Logger LOGGER = LogManager.getFormatterLogger(NAME);

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		ClientRegistry.registerKeyBinding(KEY);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (THUMBNAIL_FILE.exists() && Minecraft.getMinecraft().isSingleplayer()) {
			File icon = Minecraft.getMinecraft().getIntegratedServer().getWorldIconFile();
			if (!icon.exists()) {
				try {
					FileUtils.copyFile(THUMBNAIL_FILE, icon);
				} catch (IOException e) {
					LOGGER.catching(e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onScreenshot(ScreenshotEvent event) {
		if (KEY.isKeyDown() && Minecraft.getMinecraft().isSingleplayer()) {
			new Thread(() -> {
				int width = event.getImage().getWidth();
				int height = event.getImage().getHeight();
				int start = 0;
				int end = 0;
				if (width > height) {
					start = (width - height) / 2;
					width = height;
				} else {
					end = (height - width) / 2;
				}
				try {
					BufferedImage image = new BufferedImage(64, 64, 1);
					Graphics graphics = image.createGraphics();
					graphics.drawImage(event.getImage(), 0, 0, 64, 64, start, end, start + width, end + width, null);
					graphics.dispose();
					ImageIO.write(image, "png", Minecraft.getMinecraft().getIntegratedServer().getWorldIconFile());
					LOGGER.info("Saved screenshot as world thumbnail");
				} catch (IOException e) {
					LOGGER.catching(e);
				}
			}, NAME).start();
		}
		System.out.println(event);
	}
}
