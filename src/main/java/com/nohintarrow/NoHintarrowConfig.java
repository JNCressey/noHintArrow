package com.nohintarrow;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import java.awt.Color;

@ConfigGroup("noHintarrow")
public interface NoHintarrowConfig extends Config
{

	@ConfigItem(
			keyName = "clearDelaySeconds",
			name = "Clear Delay (seconds)",
			description = "How many seconds before the hint arrow is cleared automatically",
			position=0
	)
	default int clearDelaySeconds()
	{
		return 0; // default 0 seconds
	}

	//region Alert Settings
	@ConfigSection(
			name = "Alert Settings",
			description = "Alert message configuration",
			position = 1,
			closedByDefault = true
	)
	String alertSection = "alertSection";

	@ConfigItem(
			keyName = "doAlerts",
			name = "Do Alerts",
			description = "Show message in chat when a hint arrow is removed",
			section = alertSection,
			position = 0
	)
	default boolean doAlerts()
	{

		return true; // default will show alerts
	}

	@ConfigItem(
			keyName = "alertColor",
			name = "Alert Color",
			description = "Choose the color for alert text",
			section = alertSection,
			position = 1
	)
	default Color alertColor()
	{
		return new Color(0x7F007F); // default same purple as default trade messages
	}
	//endregion

	//region Substitute Marker Settings
	@ConfigSection(
			name = "Substitute Marker Settings",
			description = "Substitute Marker configuration",
			position = 2,
			closedByDefault = true
	)
	String substituteMarkerSection = "substituteMarkerSection";

	@ConfigItem(
			keyName = "doSubstituteMarker",
			name = "Use a substitute marker",
			description = "Show a substitute marker for the removed hint arrow",
			section = substituteMarkerSection,
			position = 0
	)
	default boolean doSubstituteMarker()
	{
		return false; // default won't use a substitute marker
	}

	@ConfigItem(
			keyName = "substituteMarkerDurationSeconds",
			name = "Duration (seconds)",
			description = "How many seconds before the substitute marker is cleared",
			section = substituteMarkerSection,
			position = 1
	)
	default int substituteMarkerDurationSeconds()
	{
		return 60; // default 1 minute
	}

	@ConfigItem(
			keyName = "substituteMarkerColor",
			name = "Marker Color",
			description = "Choose the color for the substitute marker",
			section = substituteMarkerSection,
			position = 2
	)
	default Color substituteMarkerColor() {
		return Color.YELLOW; // default yellow
	}

	@ConfigItem(
			keyName = "showSubstituteMarkerLabel",
			name = "Show Label",
			description = "Show a text label (\"Hint\") on the substitute marker",
			section = substituteMarkerSection,
			position = 3
	)
	default boolean showSubstituteMarkerLabel()
	{
		return true; // default will show label for substitute marker
	}
	//endregion

	//region debug
	@ConfigSection(
			name = "Debug",
			description = "Debug",
			position = 3,
			closedByDefault = true
	)
	String debugSection = "debugSection";

	@ConfigItem(
			keyName = "doDebug",
			name = "Debug Mode",
			description = "Enable shift click to manually set hint arrows",
			section = debugSection,
			position = 0
	)
	default boolean doDebug()
	{
		return false; // default won't show debug menu options
	}

	@ConfigItem(
			keyName = "doDebugMessages",
			name = "Debug Messages",
			description = "Show debug messages in chatbox",
			section = debugSection,
			position = 0
	)
	default boolean doDebugMessages()
	{
		return false; // default won't show debug
	}
	//endregion
}
