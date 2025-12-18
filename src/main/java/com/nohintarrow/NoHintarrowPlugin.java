package com.nohintarrow;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.Optional;

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

	//region overlay
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NoHintarrowOverlay overlay;
	//endregion

	// Tracks how many ticks the arrow has been active for when to clear hint arrow
	private int arrowActiveTicks = 0;

	//track what has been marked by the substitute marker in order to remove the marker
	private boolean isSubstituteMarkerSet = false;
	private int substituteMarkerActiveTicks = 0;


	@Override
	protected void startUp() throws Exception
	{
		log.info("no-hintarrow plugin started!");
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		log.info("no-hintarrow plugin stopped!");
	}

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


		if (
				(isSubstituteMarkerSet && (substituteMarkerActiveTicks >= getSubstituteMarkerDurationTicks())) // remove marker after duration
				|| client.hasHintArrow() // remove marker if new hint arrow has been set
		)
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
		if (config.doDebugMessages())
		{
			debugHintArrowValues();
		}

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
			printInChatbox("Hint arrow removed.");
		}
	}

	private void printInChatbox(String message)
	{
		log.info(message);
		chatMessageManager.queue(
				QueuedMessage.builder()
						.type(ChatMessageType.GAMEMESSAGE) // Game info style
						.runeLiteFormattedMessage(
								String.format("<col=%06x>", config.alertColor().getRGB() & 0xFFFFFF)
										+ message
										+ "</col>"
						)
						.build()
		);
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



	// use for testing, debug info put in chatbox
	private void debugHintArrowValues()
	{
		try {
			String[] hintarrowTypeNames = {
					"NONE",
					"NPC",
					"COORDINATE",
					"PLAYER",
					"WORLDENTITY"
			};
			printInChatbox("hint arrow type " + String.valueOf(client.getHintArrowType()) + " (" + hintarrowTypeNames[client.getHintArrowType()] + ")");

			printInChatbox(
					"NPC name "
					+ Optional.ofNullable(client.getHintArrowNpc())
							.map(NPC::getName)
							.orElse("-null-")
			);
			printInChatbox(
					"Coordinates "
					+ Optional.ofNullable(client.getHintArrowPoint())
							.map(p->String.valueOf(p.getX()) + "," + String.valueOf(p.getY()))
							.orElse("-null-")
			);
			printInChatbox(
					"player name "
					+ Optional.ofNullable(client.getHintArrowPlayer())
							.map(Player::getName)
							.orElse("-null-")
			);
			printInChatbox(
					"worldentity "
					+ "..." /* there is no Client::getHintArrowWorldEntity? */ //todo?
			);
		} catch (Exception e) {
			printInChatbox(e.getMessage());
			throw new RuntimeException(e);
		}

	}

}
