package com.cryo.cache.loaders.model.material.properties.class149s;

public abstract class Class149 {

	public static int anInt1744;

	public int anInt1743;

	public int anInt1741;

	public int anInt1742;

	public Class149(int i_1, int i_2, int i_3) {
		anInt1743 = i_1;
		anInt1741 = i_2;
		anInt1742 = i_3;
	}

	public static int method2565(CharSequence charsequence_0, char var_1) {
		int i_3 = 0;
		int i_4 = charsequence_0.length();
		for (int i_5 = 0; i_5 < i_4; i_5++) {
			if (charsequence_0.charAt(i_5) == var_1) {
				++i_3;
			}
		}
		return i_3;
	}


	public abstract void method2556(int var1, int var2);

	public abstract void method2557(int var1, int var2);

	public abstract void method2561(int var1, int var2);
}