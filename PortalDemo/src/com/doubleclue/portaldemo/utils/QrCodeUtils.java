package com.doubleclue.portaldemo.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QrCodeUtils {

	public static byte[] createQRCode(String qrCodeData, int qrCodeheight, int qrCodewidth)
			throws WriterException, IOException {
		Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		BitMatrix matrix = new MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(matrix, "PNG", os);
		return os.toByteArray();

	}

}
