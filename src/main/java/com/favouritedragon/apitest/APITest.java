package com.favouritedragon.apitest;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = APITest.MODID, version = APITest.VERSION)
public class APITest {
	public static final String MODID = "examplemod";
	public static final String VERSION = "1.0";


	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

	@EventHandler
	public void init(FMLInitializationEvent event) {

	}
}
