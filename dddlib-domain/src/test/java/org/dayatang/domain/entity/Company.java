package org.dayatang.domain.entity;

public class Company extends Organization {

	private int level;

	public Company(String name, int level) {
		super(name);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String[] businessKeys() {
		return new String[] { "name", "level" };
	}

}