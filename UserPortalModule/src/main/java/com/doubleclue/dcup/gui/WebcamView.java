package com.doubleclue.dcup.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.event.CaptureEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.gui.JsfUtils;

@Named("webcamView")
@SessionScoped
public class WebcamView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String documentName;
	
	byte [] photoData;
	
	
	public void oncapture(CaptureEvent captureEvent) {
       
				
		photoData = captureEvent.getData();

//        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
//        String newFileName = externalContext.getRealPath("") + File.separator + "resources" + File.separator + "demo"
//                + File.separator + "images" + File.separator + "photocam" + File.separator + filename + ".jpeg";

//        FileImageOutputStream imageOutput;
//        try {
//            imageOutput = new FileImageOutputStream(new File(newFileName));
//            imageOutput.write(data, 0, data.length);
//            imageOutput.close();
//        }
//        catch (IOException e) {
//            throw new FacesException("Error in writing captured image.", e);
//        }
    }
	
	public StreamedContent getPhoto() {
		
		if (photoData != null) {
			InputStream in = new ByteArrayInputStream(photoData);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getEmptyImage();
		}
	}


	public String getDocumentName() {
		return documentName;
	}


	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	

}
