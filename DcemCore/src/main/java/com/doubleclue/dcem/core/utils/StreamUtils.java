package com.doubleclue.dcem.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;


/**
 * 
 * This class provides some utilities for Input- and Output-Streams.
 * 
 * @author 
 * 
 */
public class StreamUtils {
	
	public static void close(OutputStream s) {
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				//nothing to do
			}
		}		
	}

	public static void close(InputStream s) {
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				//nothing to do
			}
		}		
	}	
	
	public static void close(Reader r) {
		if (r != null) {
			try {
                r.close();
            } catch (IOException e) {
            	//nothing to do
            }
        }		
	}	
	
	public static void close(Writer r) {
		if (r != null) {
			try {
                r.close();
            } catch (IOException e) {
            	//nothing to do
            }
        }		
	}		

	public static void close(FileChannel fc) {
		if (fc != null) {
			try {
				fc.close();
			} catch (IOException e) {
				// nothing to do
			}
		}
	}
	
}
