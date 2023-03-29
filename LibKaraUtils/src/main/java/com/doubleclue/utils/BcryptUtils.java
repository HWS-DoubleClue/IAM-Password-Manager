package com.doubleclue.utils;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class BcryptUtils {
	
	private static final int DEFAULT_STRENGTH = 8;
	public static final int OPTIMAL_STRENGTH = calculateOptimalStrength();
	
	private BcryptUtils() {
	}
	
	public static String hash(String rawPassword) {
		return new BCryptPasswordEncoder(OPTIMAL_STRENGTH).encode(rawPassword);
	}
	
	public static boolean isValid(String rawPassword, String hash) {
		return new BCryptPasswordEncoder().matches(rawPassword, hash);
	}
	
	public static boolean needsNewHash(String oldHash) {
		if (oldHash == null || oldHash.isEmpty()) {
			return true;
		}
		return new BCryptPasswordEncoder(OPTIMAL_STRENGTH).upgradeEncoding(oldHash);
	}
	
	private static int calculateOptimalStrength() {
		int strength = DEFAULT_STRENGTH;
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(strength);
		Instant start = Instant.now();
		encoder.encode("durance-check");
		Instant finish = Instant.now();
		long durance = Duration.between(start, finish).toMillis();
		while (durance < 500) {
			strength++;
			durance *= 2;
		}
		return strength;
	}
}
