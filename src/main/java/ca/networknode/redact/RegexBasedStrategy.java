/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package ca.networknode.redact;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.RegexBasedLocationExtractionStrategy;
import com.itextpdf.pdfcleanup.autosweep.ICleanupStrategy;

import java.util.regex.Pattern;

/**
 * This class represents a regular expression based cleanup strategy
 */
public class RegexBasedStrategy extends RegexBasedLocationExtractionStrategy implements ICleanupStrategy {
	
	   	private Color redactionColor;
	    private Color redactionOverlayColor;
	    private String redactionOverlayText;
	    private Pattern _pattern;

	    public RegexBasedStrategy(String regex) {
	    	super(regex);
	    }

	    public RegexBasedStrategy(Pattern pattern) {
	    	super(pattern);
	    	this._pattern = pattern;
	    }
	
	    public Color getRedactionColor(IPdfTextLocation location) {
	        return redactionColor;
	    }

	    public Color getOverlayColor() {
	        return redactionOverlayColor;
	    }

	    public String getOverlayText() {
	        return redactionOverlayText;
	    }

	    public RegexBasedStrategy setRedactionColor(Color color) {
	        this.redactionColor = color;
	        return this;
	    }
	    
	    public RegexBasedStrategy setRedactionOverlayColor(Color color) {
	        this.redactionOverlayColor = color;
	        return this;
	    }
	    
	    public RegexBasedStrategy setRedactionOverlayText(String redactionOverlayText) {
	        this.redactionOverlayText = redactionOverlayText;
	        return this;
	    }

	    public ICleanupStrategy reset() {
	        return new RegexBasedStrategy(_pattern)
        		.setRedactionColor(new DeviceRgb(115, 203, 235))
        		.setRedactionOverlayColor(this.redactionOverlayColor)
        		.setRedactionOverlayText(this.redactionOverlayText);
	    }
}