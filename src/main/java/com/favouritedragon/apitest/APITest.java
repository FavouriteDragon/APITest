package com.favouritedragon.apitest;

import com.favouritedragon.apitest.skills.SkillRegisterHandler;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import swordskillsapi.api.skill.SkillBase;
import swordskillsapi.api.skill.SkillRegistry;

@Mod(modid = APITest.MODID, version = APITest.VERSION)
public class APITest {
	public static final String MODID = "api_test";
	public static final String VERSION = "1.0";


	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		SkillRegisterHandler.init();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {

	}
}
