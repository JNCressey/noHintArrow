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
import net.runelite.api.HintArrowType;
import net.runelite.client.ui.overlay.OverlayManager;

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

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NoHintarrowOverlay overlay;

	// Tracks how many ticks the arrow has been active for when to clear hint arrow
	private int arrowActiveTicks = 0;

	//track what has been marked by the substitute marker in order to remove the marker
	private boolean isSubstituteMarkerSet = false;
	private int substituteMarkerActiveTicks = 0;

	@Provides
	NoHintarrowConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NoHintarrowConfig.class);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		//region timers
		//region Arrow timer
		if(client.hasHintArrow())
		{
			arrowActiveTicks++; //increment counter

		}
		else
		{
			// No arrow active, reset counter
			arrowActiveTicks = 0;
		}
		//endregion

		//region Substitute Marker timer
		if(isSubstituteMarkerSet){
			substituteMarkerActiveTicks++; //increment counter
		}
		else
		{
			// No substitute marker active, reset counter
			substituteMarkerActiveTicks = 0;
		}
		//endregion
		//endregion


		if (client.hasHintArrow() && (arrowActiveTicks >= getClearDelayTicks()))
		{
			clearHintArrow();
		}

		if (isSubstituteMarkerSet && (substituteMarkerActiveTicks >= getSubstituteMarkerDurationTicks()))
		{
			clearSubstituteMarker();
		}

	}



	//region remove hint-arrow
	// the user config for clear delay converted to game ticks (1 tick = 0.6s)
	private int getClearDelayTicks(){
		return (int) Math.ceil(config.clearDelaySeconds() / 0.6);
	}


	private void clearHintArrow()
	{
		updateSubstituteMarker();

		arrowActiveTicks = 0; // reset counter

		client.clearHintArrow();

		sendAlertRemovedHintArrow();
	}
	//endregion



	//region chatbox alerts
	// send chatbox alert that hint-arrow was removed, if alerts enabled
	private void sendAlertRemovedHintArrow()
	{
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
	//endregion



	//region substitute marker
	// the user config for substitute marker duration converted to game ticks (1 tick = 0.6s)
	private int getSubstituteMarkerDurationTicks(){
		return (int) Math.ceil(config.substituteMarkerDurationSeconds() / 0.6);
	}


	private void updateSubstituteMarker()
	{
		clearSubstituteMarker();

		if (client.hasHintArrow() && (client.getHintArrowType() != HintArrowType.NONE))
		{
			isSubstituteMarkerSet = overlay.updateSubstituteMarker();
		}
	}


	private void clearSubstituteMarker()
	{
		isSubstituteMarkerSet = false;
		substituteMarkerActiveTicks = 0;

		overlay.clearSubstituteMarker();
	}

	//endregion

	@Override
	protected void startUp() throws Exception
	{
		log.info("ChatHighlightPlayerPlugin started!");
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		log.info("ChatHighlightPlayerPlugin stopped!");
	}

}
