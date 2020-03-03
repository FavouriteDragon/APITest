package com.favouritedragon.apitest.skills;

import com.favouritedragon.apitest.APITest;
import swordskillsapi.api.skill.SkillBase;
import swordskillsapi.api.skill.SkillGroup;

public class SkillRegisterHandler {

	public static final SkillGroup DEFAULT = new SkillGroup(APITest.MODID, -1).setDisplayName("API Test").register();
	public static final SkillGroup UNARMED = new SkillGroup("unarmed", 0).setHasTooltip().setDisplayName("Unarmed").register();

	/* Skill List **/
	public static final SkillBase deflect = new Deflect("deflect").addDefaultTooltip().register("deflect");
	public static final SkillBase sunbolt = new Sunbolt("sunbolt").addDefaultTooltip().register("sunbolt");

	//Just so skills get registered
	public static void init() {
	}
}
