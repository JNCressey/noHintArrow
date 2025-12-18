package com.nohintarrow;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import net.runelite.api.HintArrowType;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.LocalPoint;

import javax.inject.Inject;
import java.awt.*;
import javax.annotation.Nullable;
import com.google.common.base.Strings;

public class NoHintarrowOverlay extends Overlay{

    private static final int MAX_DRAW_DISTANCE = 32;

    private int hintArrowType = HintArrowType.NONE;
    private NPC hintArrowNPC;
    private Player hintArrowPlayer;
    private WorldPoint hintArrowPoint;


    @Inject
    private Client client;

    @Inject
    private NoHintarrowConfig config;

    @Inject
    public NoHintarrowOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        if (isVisible()) {
            String label = config.showSubstituteMarkerLabel() ? "Hint" : null;
            switch (hintArrowType){
                case HintArrowType.NPC:
                    OverlayUtil.renderActorOverlay(graphics, hintArrowNPC, label, config.substituteMarkerColor());
                    break;
                case HintArrowType.COORDINATE:
                    drawTile( graphics,  hintArrowPoint,  config.substituteMarkerColor(), label);
                    break;
                case HintArrowType.PLAYER:
                    OverlayUtil.renderActorOverlay(graphics, hintArrowPlayer, label, config.substituteMarkerColor());
                    break;

                //case HintArrowType.WORLDENTITY:
                case HintArrowType.NONE:
                default:
                    //I don't see any way to handle the hint-arrow when type is HintArrowType.WORLDENTITY
                    //do nothing
                    break;
            }
        }
        return null;
    }

    private boolean isVisible()
    {
        return config.doSubstituteMarker() && (hintArrowType != HintArrowType.NONE);
    }

    public void clearSubstituteMarker()
    {
        hintArrowType = HintArrowType.NONE;
        hintArrowNPC = null;
        hintArrowPlayer = null;
        hintArrowPoint = null;
    }

    /**
     *
     * @return whether the marker has been set
     */
    public boolean updateSubstituteMarker()
    {
        clearSubstituteMarker();

        if (client.hasHintArrow())
        {
            hintArrowType = client.getHintArrowType();

            switch (hintArrowType) {
                case HintArrowType.NPC:
                    hintArrowNPC = client.getHintArrowNpc();
                    break;
                case HintArrowType.COORDINATE:
                    hintArrowPoint = client.getHintArrowPoint();
                    break;
                case HintArrowType.PLAYER:
                    hintArrowPlayer = client.getHintArrowPlayer();
                    break;

                case HintArrowType.WORLDENTITY:
                    /* there is no client.getHintArrowWorldEntity? */
                case HintArrowType.NONE:
                default:
                    clearSubstituteMarker();
                    return false;
            }
        }
        return true;
    }


    // based on GroundMarkerOverlay.drawTile from runelite/runelite-client/src/main/java/net/runelite/client/plugins/groundmarkers/GroundMarkerOverlay.java
    private void drawTile(Graphics2D graphics, WorldPoint point, Color color, @Nullable String label)
    {
        //region set parameters
        final int borderWidth = 2;
        final Stroke borderStroke = new BasicStroke((float) borderWidth);
        final int fillOpacity =50;
        //endregion

        if (client.getLocalPlayer().getWorldView().isTopLevel())
        {
            WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

            if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
            {
                return;
            }
        }

        LocalPoint lp = LocalPoint.fromWorld(client.findWorldViewFromWorldPoint(point), point);
        if (lp == null)
        {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly != null)
        {
            OverlayUtil.renderPolygon(graphics, poly, color, new Color(0, 0, 0, fillOpacity), borderStroke);
        }

        if (!Strings.isNullOrEmpty(label))
        {
            Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
            if (canvasTextLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
            }
        }
    }
}
