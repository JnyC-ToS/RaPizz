package com.rapizz;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA512 {
	private static final @NotNull MessageDigest sha512;

	static {
		try {
			sha512 = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to load SHA-512 algorithm");
		}
	}

	private SHA512() {
		throw new UnsupportedOperationException();
	}

	public static byte @NotNull[] hash(@NotNull String value) {
		sha512.reset();
		return sha512.digest(value.getBytes());
	}

	public static @NotNull String hashToHex(@NotNull String value) {
		return bytesToHex(hash(value));
	}

	public static @NotNull String bytesToHex(byte @NotNull[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length << 1);
		for (byte b : bytes) {
			int i = 0xff & b;
			if (i <= 0x0f)
				sb.append('0');
			sb.append(Integer.toHexString(i));
		}
		return sb.toString();
	}
}
