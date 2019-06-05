/*
 * Copyright (c) 2018, Jasper Ketelaar <Jasper0781@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.mta.enchantment;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import api.Client;
import api.GameState;
import api.Item;
import api.ItemID;
import api.Player;
import api.Tile;
import api.coords.WorldPoint;
import api.events.GameStateChanged;
import api.events.GameTick;
import api.events.ItemDespawned;
import api.events.ItemSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.mta.MTAConfig;
import net.runelite.client.plugins.mta.MTARoom;

@Slf4j
public class EnchantmentRoom extends MTARoom
{
	private static final int MTA_ENCHANT_REGION = 13462;

	private final Client client;
	private final List<WorldPoint> dragonstones = new ArrayList<>();

	@Inject
	private EnchantmentRoom(MTAConfig config, Client client)
	{
		super(config);
		this.client = client;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			dragonstones.clear();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!inside() || !config.enchantment())
		{
			return;
		}

		WorldPoint nearest = findNearestStone();
		if (nearest != null)
		{
			client.setHintArrow(nearest);
		}
		else
		{
			client.clearHintArrow();
		}
	}

	private WorldPoint findNearestStone()
	{
		WorldPoint nearest = null;
		double dist = Double.MAX_VALUE;
		WorldPoint local = client.getLocalPlayer().getWorldLocation();
		for (WorldPoint worldPoint : dragonstones)
		{
			double currDist = local.distanceTo(worldPoint);
			if (nearest == null || currDist < dist)
			{
				dist = currDist;
				nearest = worldPoint;
			}
		}
		return nearest;
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final Item item = itemSpawned.getItem();
		final Tile tile = itemSpawned.getTile();

		if (item.getId() == ItemID.DRAGONSTONE_6903)
		{
			WorldPoint location = tile.getWorldLocation();
			log.debug("Adding dragonstone at {}", location);
			dragonstones.add(location);
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		final Item item = itemDespawned.getItem();
		final Tile tile = itemDespawned.getTile();

		if (item.getId() == ItemID.DRAGONSTONE_6903)
		{
			WorldPoint location = tile.getWorldLocation();
			log.debug("Removed dragonstone at {}", location);
			dragonstones.remove(location);
		}
	}

	@Override
	public boolean inside()
	{
		Player player = client.getLocalPlayer();
		return player != null && player.getWorldLocation().getRegionID() == MTA_ENCHANT_REGION
			&& player.getWorldLocation().getPlane() == 0;
	}
}
