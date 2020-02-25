package com.favouritedragon.apitest.skills;

import swordskillsapi.api.skill.SkillBase;
import swordskillsapi.api.skill.SkillGroup;

public class Skills {

	public static SkillGroup UNARMED = new SkillGroup("unarmed", 0).setHasTooltip().setDisplayName("Unarmed").register();

	/* Skill List **/
	public static final SkillBase deflect = new Deflect("deflect").addDefaultTooltip().register("deflect");
}
