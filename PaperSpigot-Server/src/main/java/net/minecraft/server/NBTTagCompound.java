package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

public class NBTTagCompound extends NBTBase {
	//private static final Logger b = LogManager.getLogger();

	private Map<String, NBTBase> map = Maps.newHashMapWithExpectedSize(8);//new HashMap<String, NBTBase>(); // Rinny

	void write(DataOutput paramDataOutput) throws IOException {
		for (Entry<String, NBTBase> entry : this.map.entrySet()) { // Rinny
			a(entry.getKey(), entry.getValue(), paramDataOutput);
		}
		/*for (String str : this.map.keySet()) {
			NBTBase nBTBase = (NBTBase) this.map.get(str);
			a(str, nBTBase, paramDataOutput);
		}*/
		paramDataOutput.writeByte(0);
	}

	void load(DataInput paramDataInput, int paramInt, NBTReadLimiter paramNBTReadLimiter) throws IOException {
		if (paramInt > 512)
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		this.map.clear();
		byte b;
		while ((b = a(paramDataInput, paramNBTReadLimiter)) != 0) {
			String str = b(paramDataInput, paramNBTReadLimiter);
			paramNBTReadLimiter.a((16 * str.length()));
			NBTBase nBTBase = a(b, str, paramDataInput, paramInt + 1, paramNBTReadLimiter);
			this.map.put(str, nBTBase);
		}
	}

	public Set<String> c() {
		return this.map.keySet();
	}

	public byte getTypeId() {
		return 10;
	}

	public void set(String paramString, NBTBase paramNBTBase) {
		this.map.put(paramString, paramNBTBase);
	}

	public void setByte(String paramString, byte paramByte) {
		this.map.put(paramString, new NBTTagByte(paramByte));
	}

	public void setShort(String paramString, short paramShort) {
		this.map.put(paramString, new NBTTagShort(paramShort));
	}

	public void setInt(String paramString, int paramInt) {
		this.map.put(paramString, new NBTTagInt(paramInt));
	}

	public void setLong(String paramString, long paramLong) {
		this.map.put(paramString, new NBTTagLong(paramLong));
	}

	public void setFloat(String paramString, float paramFloat) {
		this.map.put(paramString, new NBTTagFloat(paramFloat));
	}

	public void setDouble(String paramString, double paramDouble) {
		this.map.put(paramString, new NBTTagDouble(paramDouble));
	}

	public void setString(String paramString1, String paramString2) {
		this.map.put(paramString1, new NBTTagString(paramString2));
	}

	public void setByteArray(String paramString, byte[] paramArrayOfbyte) {
		this.map.put(paramString, new NBTTagByteArray(paramArrayOfbyte));
	}

	public void setIntArray(String paramString, int[] paramArrayOfint) {
		this.map.put(paramString, new NBTTagIntArray(paramArrayOfint));
	}

	public void setBoolean(String paramString, boolean paramBoolean) {
		setByte(paramString, (byte) (paramBoolean ? 1 : 0));
	}

	public NBTBase get(String paramString) {
		return (NBTBase) this.map.get(paramString);
	}

	public byte b(String paramString) {
		NBTBase nBTBase = (NBTBase) this.map.get(paramString);
		if (nBTBase != null)
			return nBTBase.getTypeId();
		return 0;
	}

	public boolean hasKey(String paramString) {
		return this.map.containsKey(paramString);
	}

	public boolean hasKeyOfType(String paramString, int paramInt) {
		byte b = b(paramString);
		if (b == paramInt)
			return true;
		if (paramInt == 99)
			return (b == 1 || b == 2 || b == 3 || b == 4 || b == 5 || b == 6);
		return false;
	}

	public byte getByte(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return 0;
			return ((NBTNumber) this.map.get(paramString)).f();
		} catch (ClassCastException classCastException) {
			return 0;
		}
	}

	public short getShort(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return 0;
			return ((NBTNumber) this.map.get(paramString)).e();
		} catch (ClassCastException classCastException) {
			return 0;
		}
	}

	public int getInt(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return 0;
			return ((NBTNumber) this.map.get(paramString)).d();
		} catch (ClassCastException classCastException) {
			return 0;
		}
	}

	public long getLong(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return 0L;
			return ((NBTNumber) this.map.get(paramString)).c();
		} catch (ClassCastException classCastException) {
			return 0L;
		}
	}

	public float getFloat(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return 0.0F;
			return ((NBTNumber) this.map.get(paramString)).h();
		} catch (ClassCastException classCastException) {
			return 0.0F;
		}
	}

	public double getDouble(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return 0.0D;
			return ((NBTNumber) this.map.get(paramString)).g();
		} catch (ClassCastException classCastException) {
			return 0.0D;
		}
	}

	public String getString(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return "";
			return ((NBTBase) this.map.get(paramString)).a_();
		} catch (ClassCastException classCastException) {
			return "";
		}
	}

	public byte[] getByteArray(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return new byte[0];
			return ((NBTTagByteArray) this.map.get(paramString)).c();
		} catch (ClassCastException classCastException) {
			throw new ReportedException(a(paramString, 7, classCastException));
		}
	}

	public int[] getIntArray(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return new int[0];
			return ((NBTTagIntArray) this.map.get(paramString)).c();
		} catch (ClassCastException classCastException) {
			throw new ReportedException(a(paramString, 11, classCastException));
		}
	}

	public NBTTagCompound getCompound(String paramString) {
		try {
			if (!this.map.containsKey(paramString))
				return new NBTTagCompound();
			return (NBTTagCompound) this.map.get(paramString);
		} catch (ClassCastException classCastException) {
			throw new ReportedException(a(paramString, 10, classCastException));
		}
	}

	public NBTTagList getList(String paramString, int paramInt) {
		try {
			if (b(paramString) != 9)
				return new NBTTagList();
			NBTTagList nBTTagList = (NBTTagList) this.map.get(paramString);
			if (!nBTTagList.isEmpty() && nBTTagList.d() != paramInt) // Rinny - use !isEmpty instead of size > 0
				return new NBTTagList();
			return nBTTagList;
		} catch (ClassCastException classCastException) {
			throw new ReportedException(a(paramString, 9, classCastException));
		}
	}

	public boolean getBoolean(String paramString) {
		return (getByte(paramString) != 0);
	}

	public void remove(String paramString) {
		this.map.remove(paramString);
	}

	public String toString() {
		String str = "{";
		for (String str1 : this.map.keySet())
			str = str + str1 + ':' + this.map.get(str1) + ',';
		return str + "}";
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	private CrashReport a(String paramString, int paramInt, ClassCastException paramClassCastException) {
		CrashReport crashReport = CrashReport.a(paramClassCastException, "Reading NBT data");
		CrashReportSystemDetails crashReportSystemDetails = crashReport.a("Corrupt NBT tag", 1);
		crashReportSystemDetails.a("Tag type found", new CrashReportCorruptNBTTag(this, paramString));
		crashReportSystemDetails.a("Tag type expected", new CrashReportCorruptNBTTag2(this, paramInt));
		crashReportSystemDetails.a("Tag name", paramString);
		return crashReport;
	}

	public NBTBase clone() {
		NBTTagCompound nBTTagCompound = new NBTTagCompound();
		for (String str : this.map.keySet())
			nBTTagCompound.set(str, ((NBTBase) this.map.get(str)).clone());
		return nBTTagCompound;
	}

	public boolean equals(Object paramObject) {
		if (super.equals(paramObject)) {
			NBTTagCompound nBTTagCompound = (NBTTagCompound) paramObject;
			return this.map.entrySet().equals(nBTTagCompound.map.entrySet());
		}
		return false;
	}

	public int hashCode() {
		return super.hashCode() ^ this.map.hashCode();
	}

	private static void a(String paramString, NBTBase paramNBTBase, DataOutput paramDataOutput) throws IOException {
		paramDataOutput.writeByte(paramNBTBase.getTypeId());
		if (paramNBTBase.getTypeId() == 0)
			return;
		paramDataOutput.writeUTF(paramString);
		paramNBTBase.write(paramDataOutput);
	}

	private static byte a(DataInput paramDataInput, NBTReadLimiter paramNBTReadLimiter) throws IOException {
		return paramDataInput.readByte();
	}

	private static String b(DataInput paramDataInput, NBTReadLimiter paramNBTReadLimiter) throws IOException {
		return paramDataInput.readUTF();
	}

	static NBTBase a(byte paramByte, String paramString, DataInput paramDataInput, int paramInt, NBTReadLimiter paramNBTReadLimiter) {
		NBTBase nBTBase = NBTBase.createTag(paramByte);
		try {
			nBTBase.load(paramDataInput, paramInt, paramNBTReadLimiter);
		} catch (IOException iOException) {
			CrashReport crashReport = CrashReport.a(iOException, "Loading NBT data");
			CrashReportSystemDetails crashReportSystemDetails = crashReport.a("NBT Tag");
			crashReportSystemDetails.a("Tag name", paramString);
			crashReportSystemDetails.a("Tag type", Byte.valueOf(paramByte));
			throw new ReportedException(crashReport);
		}
		return nBTBase;
	}
}