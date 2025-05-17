package net.minecraft.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import org.spigotmc.SpigotDebreakifier;

public class PacketPlayOutMapChunk extends Packet {
	public int a;

	public int b;

	public int c;

	public int d;

	public byte[] e;

	public byte[] f;

	public boolean g;

	public int h;

	public static byte[] i = new byte[196864];

	public World world;

	public int mask;

	public EntityPlayer entityPlayer;

	private static final byte[] emptyChunkBytes = new byte[256];

	private static final byte[] emptyChunkBytesDeflated;

	public List<NBTTagCompound> tileEntityData;

	static {
		Deflater deflater = new Deflater(9);
		deflater.setInput(emptyChunkBytes);
		deflater.finish();
		byte[] b = new byte[256];
		int len = deflater.deflate(b);
		deflater.end();
		emptyChunkBytesDeflated = new byte[len];
		System.arraycopy(b, 0, emptyChunkBytesDeflated, 0, len);
	}

	public PacketPlayOutMapChunk() {
	}

	public PacketPlayOutMapChunk(Chunk chunk, boolean flag, int i , EntityPlayer entityPlayer) {
		this.world = chunk.world;
		this.mask = i;
		this.a = chunk.locX;
		this.b = chunk.locZ;
		this.g = flag;
		this.entityPlayer = entityPlayer;
		if (this.g && this.mask == 0)
			return;
		ChunkMap chunkmap = a(chunk, flag, i);
		this.d = chunkmap.c;
		this.c = chunkmap.b;
		this.f = chunkmap.a;
	}

	public PacketPlayOutMapChunk(Chunk chunk, boolean flag, ChunkMap chunkmap, EntityPlayer entityPlayer) {
		this.world = chunk.world;
		this.mask = chunkmap.b;
		this.a = chunk.locX;
		this.b = chunk.locZ;
		this.g = flag;
		this.entityPlayer = entityPlayer;
		this.d = chunkmap.c;
		this.c = chunkmap.b;
		this.f = chunkmap.a;
	}

	public void setTileEntities(Map<ChunkPosition, TileEntity> tileEntities) {
		if (this.g) {
			this.tileEntityData = new ArrayList<NBTTagCompound>(tileEntities.size());
		} else {
			this.tileEntityData = new ArrayList<NBTTagCompound>();
		}
		for (Map.Entry<ChunkPosition, TileEntity> entry : tileEntities.entrySet()) {
			if (this.g || (this.c & 1 << ((ChunkPosition) entry.getKey()).y >> 4) != 0) {
				NBTTagCompound data = new NBTTagCompound();
				((TileEntity) entry.getValue()).b(data);
				this.tileEntityData.add(((TileEntity) entry.getValue()).toNbt107());
			}
		}
	}

	public static int c() {
		return 196864;
	}

	public void a(PacketDataSerializer packetdataserializer) throws IOException {
	}

	public void b(PacketDataSerializer packetdataserializer) {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeInt(this.b);
		packetdataserializer.writeBoolean(this.g);
		packetdataserializer.writeShort((short) (this.c & 0xFFFF));
		if (this.g && this.mask == 0) {
			packetdataserializer.writeShort(0);
			packetdataserializer.writeInt(emptyChunkBytesDeflated.length);
			packetdataserializer.writeBytes(emptyChunkBytesDeflated);
			return;
		}
		Deflater deflater = new Deflater(4);
		byte[] deflated = new byte[this.f.length + 100];
		int deflatedLen = 0;
		try {
			deflater.setInput(this.f, 0, this.f.length);
			deflater.finish();
			deflatedLen = deflater.deflate(deflated);
		} finally {
			deflater.end();
		}
		packetdataserializer.writeShort((short) (this.d & 0xFFFF));
		packetdataserializer.writeInt(deflatedLen);
		packetdataserializer.writeBytes(deflated, 0, deflatedLen);
	}

	public void write47(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeInt(this.b);
		packetdataserializer.writeBoolean(this.g);
		packetdataserializer.writeShort((short) (this.c & 0xFFFF));
		if (this.g && this.mask == 0) {
			a(packetdataserializer, emptyChunkBytes);
			return;
		}
		int idArrayLength = 0;
		int index = 0;
		byte[] buf = new byte[8192];
		for (int j = 0; j < 16; j++) {
			if ((this.c & 1 << j) != 0)
				idArrayLength += 4096;
		}
		packetdataserializer.b(this.f.length + (idArrayLength >> 1));
		for (int section = 0; section < 16; section++) {
			if ((this.c & 1 << section) != 0) {
				int bufIndex = 0;
				int end = index + 4096;
				for (; index < end; index++) {
					int id = this.f[index] & 0xFF;
					int data = this.f[idArrayLength + (index >> 1)] & 0xFF;
					data = ((index & 0x1) == 0) ? (data & 0xF) : (data >> 4 & 0xF);
					data = SpigotDebreakifier.getCorrectedData(id, data);
					buf[bufIndex++] = (byte) ((id << 4 | data) & 0xFF);
					buf[bufIndex++] = (byte) (id >> 4 & 0xFF);
				}
				packetdataserializer.writeBytes(buf);
			}
		}
		index = idArrayLength + (idArrayLength >> 1);
		packetdataserializer.writeBytes(this.f, index, this.f.length - index);
	}

	public void write107(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeInt(this.b);
		packetdataserializer.writeBoolean(this.g);
		packetdataserializer.b(this.c);
		io.noks.protocol107.ChunkWriter.write(packetdataserializer, this);
	}

	public void write110(PacketDataSerializer packetdataserializer) throws IOException {
		packetdataserializer.writeInt(this.a);
		packetdataserializer.writeInt(this.b);
		packetdataserializer.writeBoolean(this.g);
		packetdataserializer.b(this.c);
		net.badlion.gspigot.protocol110.ChunkWriter.write(packetdataserializer, this);
	}

	public void a(PacketPlayOutListener packetplayoutlistener) {
		packetplayoutlistener.a(this);
	}

	public String b() {
		return String.format("x=%d, z=%d, full=%b, sects=%d, add=%d, size=%d",
				new Object[] { Integer.valueOf(this.a), Integer.valueOf(this.b), Boolean.valueOf(this.g),
						Integer.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.h) });
	}

	public static ChunkMap a(Chunk chunk, boolean flag, int i) {
		int j = 0;
		ChunkSection[] achunksection = chunk.getSections();
		int k = 0;
		ChunkMap chunkmap = new ChunkMap();
		byte[] abyte = PacketPlayOutMapChunk.i;
		if (flag) {
			chunk.q = true;
		}
		int l;
		for (l = 0; l < achunksection.length; l++) {
			if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
				chunkmap.b |= 1 << l;
				if (achunksection[l].getExtendedIdArray() != null) {
					chunkmap.c |= 1 << l;
					k++;
				}
			}
		}
		for (l = 0; l < achunksection.length; l++) {
			if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
				byte[] abyte1 = achunksection[l].getIdArray();
				System.arraycopy(abyte1, 0, abyte, j, abyte1.length);
				j += abyte1.length;
			}
		}
		for (l = 0; l < achunksection.length; l++) {
			if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
				NibbleArray nibblearray = achunksection[l].getDataArray();
				System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
				j += nibblearray.a.length;
			}
		}
		for (l = 0; l < achunksection.length; l++) {
			if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
				NibbleArray nibblearray = achunksection[l].getEmittedLightArray();
				System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
				j += nibblearray.a.length;
			}
		}
		if (!chunk.world.worldProvider.g) {
			for (l = 0; l < achunksection.length; l++) {
				if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty()) && (i & 1 << l) != 0) {
					NibbleArray nibblearray = achunksection[l].getSkyLightArray();
					System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
					j += nibblearray.a.length;
				}
			}
		}
		if (k > 0) {
			for (l = 0; l < achunksection.length; l++) {
				if (achunksection[l] != null && (!flag || !achunksection[l].isEmpty())
						&& achunksection[l].getExtendedIdArray() != null && (i & 1 << l) != 0) {
					NibbleArray nibblearray = achunksection[l].getExtendedIdArray();
					System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
					j += nibblearray.a.length;
				}
			}
		}
		if (flag) {
			byte[] abyte2 = chunk.m();
			System.arraycopy(abyte2, 0, abyte, j, abyte2.length);
			j += abyte2.length;
		}
		chunkmap.a = new byte[j];
		System.arraycopy(abyte, 0, chunkmap.a, 0, j);
		return chunkmap;
	}

	public void handle(PacketListener packetlistener) {
		a((PacketPlayOutListener) packetlistener);
	}
}
