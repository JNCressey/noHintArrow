package com.nohintarrow;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "No Hint-arrow"
)
public class NoHintarrowPlugin extends Plugin
{
//	@Inject
//	private Client client;
//
//	@Inject
//	private NoHintarrowConfig config;
//
//	@Override
//	protected void startUp() throws Exception
//	{
//		log.debug("Example started!");
//	}
//
//	@Override
//	protected void shutDown() throws Exception
//	{
//		log.debug("Example stopped!");
//	}
//
//	@Subscribe
//	public void onGameStateChanged(GameStateChanged gameStateChanged)
//	{
//		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
//		{
//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
//		}
//	}
//
//	@Provides
//	NoHintarrowConfig provideConfig(ConfigManager configManager)
//	{
//		return configManager.getConfig(NoHintarrowConfig.class);
//	}

	@Inject
	private Client client;

	@Inject
	private NoHintarrowConfig config;

	// Tracks how many ticks the arrow has been active
	private int arrowActiveTicks = 0;

	@Provides
	NoHintarrowConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NoHintarrowConfig.class);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Check if a hint arrow is active
		//if (client.getHintArrowNpc() != -1 || client.getHintArrowPlayer() != -1 || client.getHintArrowPoint() != -1)
		if(client.hasHintArrow())
		{
			arrowActiveTicks++;

			// Convert seconds to game ticks (1 tick = 0.6s)
			int maxTicks = (int) Math.ceil(config.clearDelaySeconds() / 0.6);

			if (arrowActiveTicks >= maxTicks)
			{
				client.clearHintArrow();
				arrowActiveTicks = 0; // reset counter
			}
		}
		else
		{
			// No arrow active, reset counter
			arrowActiveTicks = 0;
		}
	}

}
