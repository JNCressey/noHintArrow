package com.nohintarrow;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

@Slf4j
@PluginDescriptor(
	name = "No Hint-arrow"
)
public class NoHintarrowPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private NoHintarrowConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

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
		if(client.hasHintArrow())
		{
			arrowActiveTicks++;

			// Convert seconds to game ticks (1 tick = 0.6s)
			int maxTicks = (int) Math.ceil(config.clearDelaySeconds() / 0.6);

			if (arrowActiveTicks >= maxTicks)
			{
				client.clearHintArrow();
				arrowActiveTicks = 0; // reset counter
				if (config.doAlerts()) {
					chatMessageManager.queue(
							QueuedMessage.builder()
									.type(ChatMessageType.GAMEMESSAGE) // Game info style
									.runeLiteFormattedMessage(
											String.format("<col=%06x>", config.alertColor().getRGB() & 0xFFFFFF)
													+ "Hint arrow removed."
													+ "</col>"
									)
									.build()
					);
				}
			}
		}
		else
		{
			// No arrow active, reset counter
			arrowActiveTicks = 0;
		}
	}

}
