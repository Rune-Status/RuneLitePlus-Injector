package net.runelite.client.plugins.spawntimer;

import api.NPC;

public class thing 
{
	private NPC npc;
	private int tick;
	public void setNpc(NPC n)
	{
		npc = n;
	}
	NPC getNpc()
	{
		return npc;
	}
	public void setTick(int n)
	{
		tick = n;
	}
	int getTick()
	{
		return tick;
	}
}