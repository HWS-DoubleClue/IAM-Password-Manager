package com.doubleclue.utils;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class BcryptUtils {
	
	public static final int OPTIMAL_STRENGTH = calculateOptimalStrength(8);
	
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
	
	private static long measureHashingDuration(int strength) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(strength);
		Instant start = Instant.now();
		encoder.encode("durance-check");
		Instant finish = Instant.now();
		return Duration.between(start, finish).toMillis();
	}
	
	private static int calculateOptimalStrength(int strength) {
		// target duration is between 512 and 1024 milliseconds
		// increasing strength by one doubles the duration
		int[] threasholds = {8, 16, 32, 64, 128, 256, 512};
		long durance = measureHashingDuration(strength);
		int increment = 7;
		for (int threashold : threasholds) {
			if (durance < threashold) {
				return calculateOptimalStrength(strength + increment);
			}
			increment--;
		}
		return strength;
	}
}
