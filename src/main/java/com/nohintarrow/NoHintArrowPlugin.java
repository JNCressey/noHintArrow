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
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.coords.WorldPoint;

@Slf4j
@PluginDescriptor(
	name = "No Hint-arrow"
)
public class NoHintArrowPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private NoHintArrowConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	//region overlay
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NoHintArrowOverlay overlay;
	//endregion

	// Tracks how many ticks the arrow has been active for when to clear hint arrow
	private int arrowActiveTicks = 0;

	//track what has been marked by the substitute marker in order to remove the marker
	private boolean isSubstituteMarkerSet = false;
	private int substituteMarkerActiveTicks = 0;


	@Override
	protected void startUp() throws Exception
	{
		log.info("noHintArrow plugin started!");
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		log.info("noHintArrow plugin stopped!");
	}

	@Provides
	NoHintArrowConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NoHintArrowConfig.class);
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


	//region debug
	// use for testing, debug info put in chatbox
	private void debugHintArrowValues()
	{
		try {
			String[] hintArrowTypeNames = {
					"NONE",
					"NPC",
					"COORDINATE",
					"PLAYER",
					"WORLDENTITY"
			};
			printInChatbox("hint arrow type " + client.getHintArrowType() + " (" + hintArrowTypeNames[client.getHintArrowType()] + ")");

			printInChatbox(
					"NPC name "
					+ Optional.ofNullable(client.getHintArrowNpc())
							.map(NPC::getName)
							.orElse("-null-")
			);
			printInChatbox(
					"Coordinates "
					+ Optional.ofNullable(client.getHintArrowPoint())
							.map(p->p.getX() + "," + p.getY())
							.orElse("-null-")
			);
			printInChatbox(
					"player name "
					+ Optional.ofNullable(client.getHintArrowPlayer())
							.map(Player::getName)
							.orElse("-null-")
			);
		} catch (Exception e) {
			printInChatbox(e.getMessage());
			throw new RuntimeException(e);
		}

	}


	// use for testing, manually set hint arrows when shift clicking on things
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		if (!config.doDebug()){ return; } // only add debug options if debug mode on

		final boolean hotKeyPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
		if (!hotKeyPressed){ return; } // only add debug options if shift key is held

		MenuAction menuAction = event.getMenuEntry().getType();


		//region add setHintArrow options to menu

		// Coordinate from ground
		if (menuAction == MenuAction.WALK) {
			final int worldId = event.getMenuEntry().getWorldViewId();
			Optional.ofNullable(client.getWorldView(worldId))
					.map(WorldView::getSelectedSceneTile)
					.map(e->WorldPoint.fromLocalInstance(client, e.getLocalLocation()))
					.ifPresent(worldPoint ->
						client.getMenu().createMenuEntry(-1)
								.setOption("setHintArrow")
								.setTarget("Tile")
								.setType(MenuAction.RUNELITE)
								.onClick(e ->
										client.setHintArrow(worldPoint)));
		}

		// NPC
		if (menuAction == MenuAction.EXAMINE_NPC) {
			client.getMenu().createMenuEntry(-1)
					.setOption("setHintArrow")
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
							client.setHintArrow(event.getMenuEntry().getNpc()));
		}

		// Player
		Player player = event.getMenuEntry().getPlayer();
		String option = event.getMenuEntry().getOption();
		if ((player != null) && (option.equals("Follow"))) {
			client.getMenu().createMenuEntry(-1)
					.setOption("setHintArrow")
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
							client.setHintArrow(player));
		}

		//endregion

	}

	//endregion
}
