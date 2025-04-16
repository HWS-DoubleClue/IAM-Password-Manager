package com.doubleclue.dcem.dm.preferences;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@XmlType
@XmlRootElement(name = "dmPreferences")
public class DmPreferences extends ModulePreferences {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @DcemGui(help = "An E-Mail token will expire after the value in minutes")
    @Min(0)
    int emailTokenValidFor = 60;

    @DcemGui(masterOnly = true, help = "To enable OCR, you must install 'Tesseract' and define here the data-path", style = "width: 350px")
    String tesseractDataPath;

    @DcemGui(masterOnly = true, help = "To create thumbnails from a video stream you need to install 'FFmpeg'. Set this to ffmpeg installation path. See https://ffmpeg.org/", style = "width: 350px")
    String ffmpegPath;

    @DcemGui(masterOnly = true, help = "Specify the Solr server URL. This is required for indexing and search functionality. Example: http://localhost:8983/solr/core", style = "width: 350px")
    String solrUrl;
    
    @DcemGui(help = "The maximum number of search results to return.", style = "width: 50px")
    @Min(1)
    @Max (500)
    int maxSearchResults = 50;

    public String getTesseractDataPath() {
        return tesseractDataPath;
    }

    public void setTesseractDataPath(String tesseractDataPath) {
        this.tesseractDataPath = tesseractDataPath;
    }

    public int getEmailTokenValidFor() {
        return emailTokenValidFor;
    }

    public void setEmailTokenValidFor(int emailTokenValidFor) {
        this.emailTokenValidFor = emailTokenValidFor;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public String getSolrUrl() {
        return solrUrl;
    }

    public void setSolrUrl(String solrUrl) {
    	if (solrUrl.endsWith("/") == false) {
			solrUrl = solrUrl + "/";
		}
        this.solrUrl = solrUrl;
    }
    
    public int getMaxSearchResults() {
        return maxSearchResults;
    }

    public void setMaxSearchResults(int maxSearchResults) {
        this.maxSearchResults = maxSearchResults;
    }
}
