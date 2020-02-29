package com.feed_the_beast.mods.ftbchunks.api;

import com.feed_the_beast.mods.ftbchunks.ClaimedChunkPlayerData;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public interface ClaimedChunkGroup
{
	ClaimedChunkPlayerData getPlayerData();

	String getId();

	@Nullable
	ITextComponent getCustomName();

	int getColorOverride();
}