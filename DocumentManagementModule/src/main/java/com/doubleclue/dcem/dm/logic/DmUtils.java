package com.doubleclue.dcem.dm.logic;

import java.io.File;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.utils.process.ProcessResult;
import com.doubleclue.dcem.core.utils.process.ProcessUtil;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;

public class DmUtils {

	public static boolean isValidName(String name) {
		if (name == null) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			if (DmConstants.SPECIAL_CHARACTERS.contains(name.subSequence(i, i + 1))) {
				return false;
			}
		}
		return true;
	}

	public static String convertTextToHtml(File contentFile) throws Exception {
		String text = getStringContent(contentFile);
		return convertTextToHtml(text);
	}
	
	public static String convertTextToHtml(String text)  {
		if (text == null || text.isBlank()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<p>");
		String lineEnd = "\n";
		int ind = text.indexOf('\n');
		if (ind > 0) {
			if (text.charAt(ind - 1) == '\r') {
				lineEnd = "\r\n";
			}
		}
		sb.append(text.replace(lineEnd, "<br></br>"));
		sb.append("</p>");
		return sb.toString();
	}

	public static String getStringContent(File contentFile) throws Exception {
		try {
			return Files.readString(contentFile.toPath(), StandardCharsets.UTF_8);
		} catch (MalformedInputException e) {
			return Files.readString(contentFile.toPath(), StandardCharsets.ISO_8859_1);
		}
	}

	public static File createMp4Thumbnail(File mp4File, String ffmpegPath) throws Exception {
		File thumbnail = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, ".png");
		List<String> cmdList = new ArrayList<>();
		if (ffmpegPath == null || ffmpegPath.isBlank()) {
			cmdList.add("ffmpeg.exe");
		} else {
			if (ffmpegPath.endsWith(File.pathSeparator) == false) {
				ffmpegPath = ffmpegPath + (File.separator);
			}
			cmdList.add(ffmpegPath + "ffmpeg.exe");
		}
		cmdList.add("-i");
		cmdList.add(mp4File.getAbsolutePath());
		cmdList.add("-y");
		cmdList.add("-ss");
		cmdList.add("00:00:01.000");
		cmdList.add("-vframes");
		cmdList.add("1");
		cmdList.add(thumbnail.getAbsolutePath());
		ProcessResult result = ProcessUtil.executeProcess(cmdList, 10);
		if (result.getProcessExitCode() != 0) {
			thumbnail.delete();
			throw new Exception("Exit Code: " + result.getProcessExitCode());
		}
		return thumbnail;
	}

	
	

}
