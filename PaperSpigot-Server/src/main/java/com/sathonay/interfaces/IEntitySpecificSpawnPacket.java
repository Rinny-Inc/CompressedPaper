package com.sathonay.interfaces;

import net.minecraft.server.Packet;

// Credtis: Sathonay
// https://github.com/sathonay/nPaper/commit/f83cec4
public interface IEntitySpecificSpawnPacket {
	public Packet createSpecificSpawnPacket();
}
