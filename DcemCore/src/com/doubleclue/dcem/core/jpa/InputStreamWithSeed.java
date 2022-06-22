package com.doubleclue.dcem.core.jpa;

import java.io.IOException;
import java.io.InputStream;

import com.doubleclue.utils.RandomUtils;

public class InputStreamWithSeed extends InputStream {
	
	InputStream inputStream;
	
	byte [] seed; 
	int readIndex = 0;
	int seedLength;

	public InputStreamWithSeed(InputStream inputStream, int seedLength) {
		super();
		this.inputStream = inputStream;
		this.seedLength = seedLength;
		seed = RandomUtils.getRandom(seedLength);
	}	

	@Override
	public int read() throws IOException {
		if (readIndex < seedLength) {
			readIndex++;
			return seed[readIndex];
		}
		return inputStream.read();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read (b, 0, b.length);
	}
	
	@Override
	public
	int read(byte[] b, int off, int len) throws IOException {
		int min = 0;
		if (readIndex < seedLength) {
			min = Math.min(seedLength, len);
			for (int i =0; i < min; i++) {		
				b[i] = seed[i];
				readIndex++;
			}
			off += readIndex;
			len -= min;
		}
		int length = inputStream.read(b, off, len);
		return length + min;
	}
	
	

}
