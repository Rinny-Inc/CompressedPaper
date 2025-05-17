package org.bukkit;

public class AxisAlignedBB {
	private final double minX;
	private final double minY;
	private final double minZ;
	private final double maxX;
	private final double maxY;
	private final double maxZ;

	public AxisAlignedBB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	/**
	 * Gets the minimum x-coordinate of this bounding box.
	 *
	 * @return minimum x-coordinate
	 */
	public double getMinX() {
		return minX;
	}

	/**
	 * Gets the minimum y-coordinate of this bounding box.
	 *
	 * @return minimum y-coordinate
	 */
	public double getMinY() {
		return minY;
	}

	/**
	 * Gets the minimum z-coordinate of this bounding box.
	 *
	 * @return minimum z-coordinate
	 */
	public double getMinZ() {
		return minZ;
	}

	/**
	 * Gets the maximum x-coordinate of this bounding box.
	 *
	 * @return maximum x-coordinate
	 */
	public double getMaxX() {
		return maxX;
	}

	/**
	 * Gets the maximum y-coordinate of this bounding box.
	 *
	 * @return maximum y-coordinate
	 */
	public double getMaxY() {
		return maxY;
	}

	/**
	 * Gets the minimum z-coordinate of this bounding box.
	 *
	 * @return maximum z-coordinate
	 */
	public double getMaxZ() {
		return maxZ;
	}
	
	public AxisAlignedBB grow(double x, double y, double z) {
		final double d1 = this.minX - x;
		final double d2 = this.minY - y;
		final double d3 = this.minZ - z;
		final double d4 = this.maxX + x;
		final double d5 = this.maxY + y;
		final double d6 = this.maxZ + z;
		return new AxisAlignedBB(d1, d2, d3, d4, d5, d6);
	}
	
	public AxisAlignedBB shrink(double x, double y, double z) {
		final double d1 = this.minX + x;
		final double d2 = this.minY + y;
		final double d3 = this.minZ + z;
		final double d4 = this.maxX - x;
		final double d5 = this.maxY - y;
		final double d6 = this.maxZ - z;
		return new AxisAlignedBB(d1, d2, d3, d4, d5, d6);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.minX) ^ (Double.doubleToLongBits(this.minX) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.minY) ^ (Double.doubleToLongBits(this.minY) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.minZ) ^ (Double.doubleToLongBits(this.minZ) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.maxX) ^ (Double.doubleToLongBits(this.maxX) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.maxY) ^ (Double.doubleToLongBits(this.maxY) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.maxZ) ^ (Double.doubleToLongBits(this.maxZ) >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof AxisAlignedBB)) {
			return false;
		}
		AxisAlignedBB aabb = (AxisAlignedBB) other;
		return Double.compare(aabb.minX, minX) == 0 && Double.compare(aabb.minY, minY) == 0 && Double.compare(aabb.minZ, minZ) == 0 && Double.compare(aabb.maxX, maxX) == 0 && Double.compare(aabb.maxY, maxY) == 0 && Double.compare(aabb.maxZ, maxZ) == 0;
	}

	@Override
	public String toString() {
		return "AxisAlignedBB[" + minX + ", " + minY + ", " + minZ + " -> " + maxX + ", " + maxY + ", " + maxZ + "]";
	}
}