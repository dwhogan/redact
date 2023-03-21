package ca.networknode.redact;

import java.util.List;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfRedactAnnotation;
import com.itextpdf.pdfcleanup.autosweep.ICleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.PdfAutoSweepTools;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;


public class PdfAutoRedactTools extends PdfAutoSweepTools{
	
	private int _annotationNumber = 1;
	private RegexBasedStrategy _strategy;
	
	public PdfAutoRedactTools(ICleanupStrategy strategy) {
		super(strategy);
		
		_strategy= (RegexBasedStrategy)strategy;

	}
	
	@Override
    public void tentativeCleanUp(PdfPage pdfPage) {
        List<PdfCleanUpLocation> cleanUpLocations = getPdfCleanUpLocations(pdfPage);
        for (PdfCleanUpLocation loc : cleanUpLocations) {
            PdfString title = new PdfString("Annotation:" + _annotationNumber);
            _annotationNumber++;
            float[] color = loc.getCleanUpColor().getColorValue();
            float[] overlayColor = _strategy.getOverlayColor().getColorValue();
            String overlayText = _strategy.getOverlayText();
 
            PdfAnnotation redact = new PdfRedactAnnotation(loc.getRegion())
	            .setDefaultAppearance(new PdfString(String.format("/Helv 11 Tf %.0f %.0f %.0f RG", overlayColor[0], overlayColor[1], overlayColor[2])))
            		//.setDefaultAppearance(da)
	            .setOverlayText(new PdfString(overlayText))
	            .setTitle(title)
	            .put(PdfName.Subj, PdfName.Redact)
            	.put(PdfName.IC, new PdfArray(color))
	            .put(PdfName.OC, new PdfArray(new float[]{255, 0, 0}));
            pdfPage.addAnnotation(redact);
        }
    }

}
