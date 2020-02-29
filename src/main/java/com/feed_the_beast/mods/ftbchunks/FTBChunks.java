package com.feed_the_beast.mods.ftbchunks;

import com.feed_the_beast.mods.ftbchunks.api.ChunkDimPos;
import com.feed_the_beast.mods.ftbchunks.api.ClaimedChunk;
import com.feed_the_beast.mods.ftbchunks.api.FTBChunksAPI;
import com.feed_the_beast.mods.ftbchunks.client.FTBChunksClient;
import com.feed_the_beast.mods.ftbchunks.impl.ClaimedChunkManagerImpl;
import com.feed_the_beast.mods.ftbchunks.impl.ClaimedChunkPlayerDataImpl;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
@Mod("ftbchunks")
public class FTBChunks
{
	public static final Logger LOGGER = LogManager.getLogger("FTB Chunks");

	public FTBChunks()
	{
		MinecraftForge.EVENT_BUS.addListener(FTBChunksCommands::new);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
		MinecraftForge.EVENT_BUS.addListener(this::worldSaved);
		MinecraftForge.EVENT_BUS.addListener(this::loggedIn);

		MinecraftForge.EVENT_BUS.addListener(this::blockLeftClick);
		MinecraftForge.EVENT_BUS.addListener(this::blockRightClick);
		MinecraftForge.EVENT_BUS.addListener(this::itemRightClick);
		MinecraftForge.EVENT_BUS.addListener(this::blockBreak);
		MinecraftForge.EVENT_BUS.addListener(this::blockPlace);
		MinecraftForge.EVENT_BUS.addListener(this::fillBucket);

		MinecraftForge.EVENT_BUS.addListener(this::chunkChange);
		MinecraftForge.EVENT_BUS.addListener(this::mobSpawned);
		MinecraftForge.EVENT_BUS.addListener(this::explosionDetonate);

		//noinspection Convert2MethodRef
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> new FTBChunksClient());
	}

	private void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		FTBChunksAPI.manager = new ClaimedChunkManagerImpl();
	}

	private void serverStarted(FMLServerStartedEvent event)
	{
		((ClaimedChunkManagerImpl) FTBChunksAPI.manager).serverStarted(event.getServer());
	}

	private void serverStopped(FMLServerStoppedEvent event)
	{
		FTBChunksAPI.manager = null;
	}

	private void worldSaved(WorldEvent.Save event)
	{
		if (!event.getWorld().isRemote())
		{
			((ClaimedChunkManagerImpl) FTBChunksAPI.manager).serverSaved();
		}
	}

	private void loggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		ClaimedChunkPlayerDataImpl data = (ClaimedChunkPlayerDataImpl) FTBChunksAPI.manager.getData((ServerPlayerEntity) event.getPlayer());

		if (!data.name.equals(event.getPlayer().getGameProfile().getName()))
		{
			data.name = event.getPlayer().getGameProfile().getName();
			data.save();
		}
	}

	private void blockLeftClick(PlayerInteractEvent.LeftClickBlock event)
	{
		if (event.getPlayer() instanceof ServerPlayerEntity)
		{
			ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getWorld(), event.getPos()));

			if (chunk != null)
			{
				if (!chunk.canEdit((ServerPlayerEntity) event.getPlayer(), event.getWorld().getBlockState(event.getPos())))
				{
					event.setCanceled(true);
				}
			}
		}
	}

	private void blockRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.getPlayer() instanceof ServerPlayerEntity)
		{
			ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getWorld(), event.getPos()));

			if (chunk != null)
			{
				if (!chunk.canInteract((ServerPlayerEntity) event.getPlayer(), event.getWorld().getBlockState(event.getPos())))
				{
					event.setCanceled(true);
				}
			}
		}
	}

	private void itemRightClick(PlayerInteractEvent.RightClickItem event)
	{
		if (event.getPlayer() instanceof ServerPlayerEntity)
		{
			ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getWorld(), event.getPos()));

			if (chunk != null)
			{
				if (!chunk.canInteract((ServerPlayerEntity) event.getPlayer(), event.getWorld().getBlockState(event.getPos())))
				{
					event.setCanceled(true);
				}
			}
		}
	}

	private void blockBreak(BlockEvent.BreakEvent event)
	{
		if (event.getPlayer() instanceof ServerPlayerEntity)
		{
			ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getWorld(), event.getPos()));

			if (chunk != null)
			{
				if (!chunk.canEdit((ServerPlayerEntity) event.getPlayer(), event.getState()))
				{
					event.setCanceled(true);
				}
			}
		}
	}

	private void blockPlace(BlockEvent.EntityPlaceEvent event)
	{
		if (event.getEntity() instanceof ServerPlayerEntity)
		{
			if (event instanceof BlockEvent.EntityMultiPlaceEvent)
			{
				for (BlockSnapshot snapshot : ((BlockEvent.EntityMultiPlaceEvent) event).getReplacedBlockSnapshots())
				{
					ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(snapshot.getWorld(), snapshot.getPos()));

					if (chunk != null && !chunk.canEdit((ServerPlayerEntity) event.getEntity(), snapshot.getCurrentBlock()))
					{
						event.setCanceled(true);
						return;
					}
				}
			}
			else
			{
				ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getWorld(), event.getPos()));

				if (chunk != null && !chunk.canEdit((ServerPlayerEntity) event.getEntity(), event.getPlacedBlock()))
				{
					event.setCanceled(true);
				}
			}
		}
	}

	private void fillBucket(FillBucketEvent event)
	{
		if (event.getEntity() instanceof ServerPlayerEntity && event.getTarget() != null && event.getTarget() instanceof BlockRayTraceResult)
		{
			ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getWorld(), ((BlockRayTraceResult) event.getTarget()).getPos()));

			Fluid fluid = Fluids.EMPTY;

			if (event.getEmptyBucket().getItem() instanceof BucketItem)
			{
				fluid = ((BucketItem) event.getEmptyBucket().getItem()).getFluid();
			}

			if (chunk != null && !chunk.canEdit((ServerPlayerEntity) event.getEntity(), fluid.getDefaultState().getBlockState()))
			{
				event.setCanceled(true);
			}
		}
	}

	private void chunkChange(EntityEvent.EnteringChunk event)
	{
		if (event.getEntity() instanceof ServerPlayerEntity && (event.getOldChunkX() != event.getNewChunkX() || event.getOldChunkZ() != event.getNewChunkZ()))
		{
			ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getEntity()));

			String s = chunk == null ? "" : (chunk.getGroupID() + ":" + chunk.getPlayerData().getName());

			if (!event.getEntity().getPersistentData().getString("ftbchunks_lastchunk").equals(s))
			{
				event.getEntity().getPersistentData().putString("ftbchunks_lastchunk", s);

				if (chunk != null)
				{
					((ServerPlayerEntity) event.getEntity()).sendStatusMessage(chunk.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA), true);
				}
				else
				{
					((ServerPlayerEntity) event.getEntity()).sendStatusMessage(new TranslationTextComponent("wilderness").applyTextStyle(TextFormatting.DARK_GREEN), true);
				}
			}
		}
	}

	private void mobSpawned(LivingSpawnEvent.CheckSpawn event)
	{
		if (!event.getWorld().isRemote())
		{
			switch (event.getSpawnReason())
			{
				case NATURAL:
				case CHUNK_GENERATION:
				case SPAWNER:
				case STRUCTURE:
				case JOCKEY:
				case PATROL:
				{
					ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(new ChunkDimPos(event.getWorld().getDimension().getType(), MathHelper.floor(event.getX()), MathHelper.floor(event.getZ())));

					if (chunk != null && !chunk.canEntitySpawn(event.getEntity()))
					{
						event.setResult(Event.Result.DENY);
					}
				}
			}
		}
	}

	private void explosionDetonate(ExplosionEvent.Detonate event)
	{
		// check config if explosion blocking is disabled

		if (event.getWorld().isRemote() || event.getExplosion().getAffectedBlockPositions().isEmpty())
		{
			return;
		}

		List<BlockPos> list = new ArrayList<>(event.getExplosion().getAffectedBlockPositions());
		event.getExplosion().clearAffectedBlockPositions();
		Map<ChunkDimPos, Boolean> map = new HashMap<>();

		for (BlockPos pos : list)
		{
			if (map.computeIfAbsent(new ChunkDimPos(event.getWorld(), pos), cpos ->
			{
				ClaimedChunk chunk = FTBChunksAPI.manager.getChunk(cpos);
				return chunk == null || chunk.allowExplosions();
			}))
			{
				event.getExplosion().getAffectedBlockPositions().add(pos);
			}
		}
	}
}