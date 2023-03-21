package ca.networknode.redact;

import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.pdfcleanup.autosweep.ICleanupStrategy;


public class RedactLaunch {

	static class TreeLinkProcessor extends SimpleFileVisitor<Path> {
		
	    private Path source;
	    private Path target;
	    private Path sourceBase;
	    private String regex;
	    private Color redactionOverlayColor;
	    private String redactionOverlayText;
	    
	    private Integer totalDocCount = 0;
	    private Integer totalPageCount = 0;

	    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.pdf");
        
	    TreeLinkProcessor(String baseStr, String relativeSource, Path source, String regex, Color redactionOverlayColor, String redactionOverlayText) throws IOException {
    		this.regex = regex;
    		this.redactionOverlayColor = redactionOverlayColor;
		    this.redactionOverlayText = redactionOverlayText;
	        this.source = source;
	        this.sourceBase = Paths.get(String.format("%s/%s", baseStr, relativeSource.split(File.separator)[0]));
	        this.target = Paths.get(String.format("%s-redacted", sourceBase.toString()));

	        try {
	        	new File(target.toString()).mkdirs();
	        } catch (SecurityException x) {
	            System.err.format("Unable to create: %s", target.toString());
	        }
	    }
	    
	    
	    public void redact(Path path) throws IOException, InterruptedException {
	    	ICleanupStrategy cleanupStrategy = new RegexBasedStrategy(Pattern.compile(regex, Pattern.CASE_INSENSITIVE))
        		.setRedactionColor(new DeviceRgb(115, 203, 235))
        		.setRedactionOverlayColor(this.redactionOverlayColor)
        		.setRedactionOverlayText(this.redactionOverlayText);

	    	PdfAutoRedactTools autoSweep = new PdfAutoRedactTools(cleanupStrategy);

		    try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(path.toString()), new PdfWriter(target.resolve(sourceBase.relativize(path)).toString()))){
		        
		        totalDocCount++;

		        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
		        	
		        	autoSweep.tentativeCleanUp(pdfDoc.getPage(i));
		        	
		        	totalPageCount++;

	        		System.out.println(path.toString());

		        }
	            
			 } catch (BadPasswordException bpe) {
				 System.out.println("BadPasswordException " + path.toString());
		     } catch (com.itextpdf.io.exceptions.IOException ioe) {
				 System.out.println("IOException " + path.toString());
		     } catch(com.itextpdf.kernel.exceptions.PdfException pdfe) {
		    	 	System.out.println("PdfException " + path.toString());
		     } catch (java.lang.NullPointerException npe) {
		    	 	System.out.println("Null pointer " +  npe.toString());
		     } catch (Exception e) {
	    	 	System.out.println("Null pointer:: " + e.toString());
		    }
		}
	    
	    @Override
	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	    		
	    		if (matcher.matches(file.getFileName())) {
	    			try {
	    				this.redact(file);
	 	    	     } catch (com.itextpdf.io.exceptions.IOException e) {
	 	    			 System.out.println("IOException " + file.toString());
	 	    	     } catch (java.lang.InterruptedException e) {
	 	    	    	 	System.out.println("InterruptedException " + file.toString());
		    		 } catch (java.io.IOException e) {
		    	    	 	System.out.println("InterruptedException " + file.toString());
		    	     }
	    		}
	    		else {
	    			 try {
	    				 Path targetFilePath = target.resolve(source.relativize(file));
	    				 Files.copy(file, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
	 	    	     } catch (com.itextpdf.io.exceptions.IOException e) {
	 	    			 System.out.println("IOException " + file.toString());
	 	    	     }
	    		}
	        return FileVisitResult.CONTINUE;
	    }
	    
	    @Override
	    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            Path newdir = target.resolve(sourceBase.relativize(dir));
            
            try {
            	new File(newdir.toString()).mkdirs();

            } catch (NullPointerException x) {
                System.err.format("Unable to create: %s: %s%n", newdir, x);
                return SKIP_SUBTREE;
            }
            //System.out.println(dir.toString());
	        return CONTINUE;
	    }
	
		public static void main(String[] args) throws IOException {
			String base = args[0];
			String source = args[1];
			String regex = args[2];
			String colorOverlayName = args[3];
			String overLayText = args[4];
			Field field;
			
			try {
				
				field = ColorConstants.class.getDeclaredField(colorOverlayName);
				field.setAccessible(true);
				
				String pwd = String.format("%s/%s", base, source);
		        System.out.println("Working Directory = " + pwd);
		        
		        Path dir = Paths.get(pwd);
		        System.out.println("Files.walkFileTree");
		        
		        TreeLinkProcessor tlp = new TreeLinkProcessor(base, source, dir, regex, (Color)field.get(ColorConstants.class), overLayText);
		        Files.walkFileTree(dir, tlp);
		        
		        System.out.println(String.format("complete - processed %s pages", tlp.totalPageCount.toString()));
		        
			} catch (NoSuchFieldException | SecurityException e1) {
				e1.printStackTrace();
	        } catch (IOException e) {
	            logException("walkFileTree", e);
	        } catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		
		 
		private static void logException(String title, IOException e) {
	        System.err.println(title + "\terror: " + e);
	    }
	}
}
