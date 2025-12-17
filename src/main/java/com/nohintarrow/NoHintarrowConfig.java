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
			section = alertSection
	)
	default boolean doAlerts()
	{
		return true; // default will show alerts
	}
	@ConfigItem(
			keyName = "alertColor",
			name = "Alert Color",
			description = "Choose the color for alert text",
			section = alertSection
	)
	default Color alertColor()
	{
		return new Color(0x7F007F); // default same purple as default trade messages
	}

}
