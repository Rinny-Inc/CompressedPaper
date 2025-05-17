package net.minecraft.server;

import org.spigotmc.ProtocolInjector.PacketTabHeaderFooter;
import org.spigotmc.ProtocolInjector.PacketTitle;

public interface PacketPlayOutListener extends PacketListener {
	void a(PacketPlayOutSpawnEntity paramPacketPlayOutSpawnEntity);

	void a(PacketPlayOutSpawnEntityExperienceOrb paramPacketPlayOutSpawnEntityExperienceOrb);

	void a(PacketPlayOutSpawnEntityWeather paramPacketPlayOutSpawnEntityWeather);

	void a(PacketPlayOutSpawnEntityLiving paramPacketPlayOutSpawnEntityLiving);

	void a(PacketPlayOutScoreboardObjective paramPacketPlayOutScoreboardObjective);

	void a(PacketPlayOutSpawnEntityPainting paramPacketPlayOutSpawnEntityPainting);

	void a(PacketPlayOutNamedEntitySpawn paramPacketPlayOutNamedEntitySpawn);

	void a(PacketPlayOutAnimation paramPacketPlayOutAnimation);

	void a(PacketPlayOutStatistic paramPacketPlayOutStatistic);

	void a(PacketPlayOutBlockBreakAnimation paramPacketPlayOutBlockBreakAnimation);

	void a(PacketPlayOutOpenSignEditor paramPacketPlayOutOpenSignEditor);

	void a(PacketPlayOutTileEntityData paramPacketPlayOutTileEntityData);

	void a(PacketPlayOutBlockAction paramPacketPlayOutBlockAction);

	void a(PacketPlayOutBlockChange paramPacketPlayOutBlockChange);

	void a(PacketPlayOutChat paramPacketPlayOutChat);

	void a(PacketPlayOutTabComplete paramPacketPlayOutTabComplete);

	void a(PacketPlayOutMultiBlockChange paramPacketPlayOutMultiBlockChange);

	void a(PacketPlayOutMap paramPacketPlayOutMap);

	void a(PacketPlayOutTransaction paramPacketPlayOutTransaction);

	void a(PacketPlayOutCloseWindow paramPacketPlayOutCloseWindow);

	void a(PacketPlayOutWindowItems paramPacketPlayOutWindowItems);

	void a(PacketPlayOutOpenWindow paramPacketPlayOutOpenWindow);

	void a(PacketPlayOutWindowData paramPacketPlayOutWindowData);

	void a(PacketPlayOutSetSlot paramPacketPlayOutSetSlot);

	void a(PacketPlayOutCustomPayload paramPacketPlayOutCustomPayload);

	void a(PacketPlayOutKickDisconnect paramPacketPlayOutKickDisconnect);

	void a(PacketPlayOutBed paramPacketPlayOutBed);

	void a(PacketPlayOutEntityStatus paramPacketPlayOutEntityStatus);

	void a(PacketPlayOutAttachEntity paramPacketPlayOutAttachEntity);

	void a(PacketPlayOutExplosion paramPacketPlayOutExplosion);

	void a(PacketPlayOutGameStateChange paramPacketPlayOutGameStateChange);

	void a(PacketPlayOutKeepAlive paramPacketPlayOutKeepAlive);

	void a(PacketPlayOutMapChunk paramPacketPlayOutMapChunk);

	void a(PacketPlayOutMapChunkBulk paramPacketPlayOutMapChunkBulk);

	void a(PacketPlayOutWorldEvent paramPacketPlayOutWorldEvent);

	void a(PacketPlayOutLogin paramPacketPlayOutLogin);

	void a(PacketPlayOutEntity paramPacketPlayOutEntity);

	void a(PacketPlayOutPosition paramPacketPlayOutPosition);

	void a(PacketPlayOutWorldParticles paramPacketPlayOutWorldParticles);

	void a(PacketPlayOutAbilities paramPacketPlayOutAbilities);

	void a(PacketPlayOutPlayerInfo paramPacketPlayOutPlayerInfo);

	void a(PacketPlayOutEntityDestroy paramPacketPlayOutEntityDestroy);

	void a(PacketPlayOutRemoveEntityEffect paramPacketPlayOutRemoveEntityEffect);

	void a(PacketPlayOutRespawn paramPacketPlayOutRespawn);

	void a(PacketPlayOutEntityHeadRotation paramPacketPlayOutEntityHeadRotation);

	void a(PacketPlayOutHeldItemSlot paramPacketPlayOutHeldItemSlot);

	void a(PacketPlayOutScoreboardDisplayObjective paramPacketPlayOutScoreboardDisplayObjective);

	void a(PacketPlayOutEntityMetadata paramPacketPlayOutEntityMetadata);

	void a(PacketPlayOutEntityVelocity paramPacketPlayOutEntityVelocity);

	void a(PacketPlayOutEntityEquipment paramPacketPlayOutEntityEquipment);

	void a(PacketPlayOutExperience paramPacketPlayOutExperience);

	void a(PacketPlayOutUpdateHealth paramPacketPlayOutUpdateHealth);

	void a(PacketPlayOutScoreboardTeam paramPacketPlayOutScoreboardTeam);

	void a(PacketPlayOutScoreboardScore paramPacketPlayOutScoreboardScore);

	void a(PacketPlayOutSpawnPosition paramPacketPlayOutSpawnPosition);

	void a(PacketPlayOutUpdateTime paramPacketPlayOutUpdateTime);

	void a(PacketPlayOutUpdateSign paramPacketPlayOutUpdateSign);

	void a(PacketPlayOutNamedSoundEffect paramPacketPlayOutNamedSoundEffect);

	void a(PacketPlayOutCollect paramPacketPlayOutCollect);

	void a(PacketPlayOutEntityTeleport paramPacketPlayOutEntityTeleport);

	void a(PacketPlayOutUpdateAttributes paramPacketPlayOutUpdateAttributes);

	void a(PacketPlayOutEntityEffect paramPacketPlayOutEntityEffect);
	
	void a(PacketTabHeaderFooter tabHeaderPacket);
	
	void a(PacketTitle titlePacket);
}