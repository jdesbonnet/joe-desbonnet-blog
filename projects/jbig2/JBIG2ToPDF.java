/**
 * Rewrite in Java of pdf.py (jbig2enc output to PDF converter) script in jbig2enc.
 * 
 * Written for the sole purpose of enterstanding what's going on (I found the
 * python script a little terse (I'm not a python programmer), but right now
 * this is not much better. I plan to embelish with more comments as I 
 * understand it better.
 *
 * Related links and documents:
 *
 * PDF specification:
 * http://wwwimages.adobe.com/content/dam/Adobe/en/devnet/pdf/pdfs/PDF32000_2008.pdf
 *
 * JBIG2 specification:
 * http://www.jpeg.org/public/fcd14492.pdf 
 *
 * jbig2enc project:
 * https://github.com/agl/jbig2enc
 *
 *
 * Compiling:
 *
 * javac JIB2ToPDF.java
 * (no external dependencies)
 *
 *
 * Running:
 *
 * java JIB2ToPDF <symbol-table-file> <page-file-0> [<page-file-1> [<page-file-2 ...]] > output.pdf
 *
 * Symbol and page files are generated from the jbig2 utility in jbig2enc, eg:
 * ./jbig2enc/src/jbig2 -p -s bilevel_test.jpg 
 *
 *
 * 7 May 2014.
 * Joe Desbonnet, jdesbonnet@gmail.com
 *
 */
import java.util.*;
import java.io.*;


public class JBIG2ToPDF {

	/**
 	 * First arg is the symbol table file, subsequent args are the page files.
	 * (output from jbig2enc)
	 */
	public static void main (String[] arg) throws IOException {

		File symbolTableFile = new File(arg[0]);
		List<File> pageFiles = new ArrayList<File>();

		Doc doc = new Doc();


		doc.addObject(new Obj()
				.set ("Type","/Catalog")
				.set ("Outlines", "2 0 R")
				.set ("Pages", "3 0 R")
		);
			
		doc.addObject(new Obj()
				.set("Type","/Outlines")
				.set("Count", "0")
		);

		Obj pages = new Obj();
		pages.set("Type","/Pages");
		doc.addObject(pages);

		System.err.println ("symbolTableFile=" + symbolTableFile.getPath());

		Obj symbolTable = new Obj()
			.setStream(readFile(symbolTableFile));
		doc.addObject(symbolTable);


		for (int i  = 1; i < arg.length; i++) {
			File pageFile = new File(arg[i]);
			pageFiles.add(pageFile);

			byte[] fileContents = readFile(pageFile);

			DataInputStream din = new DataInputStream(new FileInputStream(pageFile));
			din.skip(11);
			int width = din.readInt();
			int height = din.readInt();
			int xres = din.readInt();
			int yres = din.readInt();

			if (xres == 0) xres = 72;
			if (yres == 0) yres = 72;

			System.err.println (pageFile.getName() + ": " + width + "x" + height + " (" + xres + "," + yres +")");

			// XObjects: see http://blogs.adobe.com/ReferenceXObjects/
			Obj xobj = new Obj()
				.set("Type","/XObject")
				.set("Subtype","/Image")
				.set("Width", ""+width)
				.set("Height", ""+height)
				.set("ColorSpace","/DeviceGray")
				.set("BitsPerComponent","1")
				.set("Filter", "/JBIG2Decode");
			xobj
				.set("DecodeParms", " << /JBIG2Globals " + symbolTable.id + " 0 R >>")
				.setStream(fileContents);

			Obj contents = new Obj()
				.setStream("q " + ((width*72)/xres) + " 0 0 " + ((height*72)/yres) + " 0 0 cm /Im1 Do Q");
			

			Obj resources = new Obj()
				.set("ProcSet", "[/PDF /ImageB]")
				.set("XObject","<< /Im1 " + xobj.id + " 0 R >>");

			Obj page = new Obj()
				.set("Type", "/Page")
				.set("Parent", "3 0 R")
				.set("MediaBox", "[ 0 0 " + ((width*72)/xres) + " " + ((height*72)/yres) + " ]")
				.set("Contents", contents.id  + " 0 R")
				.set("Resources", resources.id + " 0 R");

			doc.addObject(xobj);
			doc.addObject(contents);
			doc.addObject(resources);
			doc.addObject(page);

			doc.addPage(page);

		}

		pages.set("Count", "" + pageFiles.size() );

		StringBuffer buf = new StringBuffer();
		buf.append("[ ");
		for (Obj page : doc.getPages()) {
			buf.append (page.id + " 0 R");
		}
		buf.append(" ]");
		pages.set("Kids", buf.toString());
 
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		doc.write(baout);
		System.out.write(baout.toByteArray());
	}

	private static class Doc {
		List<Obj> objs = new ArrayList<Obj>();
		List<Obj> pageObjs = new ArrayList<Obj>();

		private StringBuffer buf = new StringBuffer();
		private int offset = 0;
		List<Integer>offsets = new ArrayList<Integer>();

		public Obj addObject (Obj obj) {
			objs.add(obj);
			return obj;
		}

		public Obj addPage (Obj page) {
			pageObjs.add(page);
			return page;
		}
		public List<Obj> getPages () {
			return pageObjs;
		}

		public void write (OutputStream out) throws IOException {

			byte[] pdfHeader = "%PDF-1.4\n".getBytes();
			out.write ("%PDF-1.4\n".getBytes());
			offset = pdfHeader.length;

			for (Obj obj : objs) {
				offsets.add(new Integer(offset));
				ByteArrayOutputStream baout = new ByteArrayOutputStream();
				obj.write(baout);
				byte[] babytes = baout.toByteArray();
				out.write(babytes);
				offset += babytes.length;
			}

			int xrefstart = offset;

			StringBuffer buf = new StringBuffer();
			buf.append("xref\n");
			buf.append("0 " + (offsets.size()+1) + "\n");
			buf.append("0000000000 65535 f \n");
			for (Integer o : offsets) {
				buf.append(String.format("%010d",o) + " 00000 n \n");
			}
			buf.append("\ntrailer\n");
			buf.append("<< /Size " + (offsets.size()+1) + "\n");
			buf.append ("/Root 1 0 R >>");
			buf.append("startxref\n");
			buf.append(""+xrefstart + "\n");
			buf.append("%%EOF");

			out.write (buf.toString().getBytes());
			
		}

	}

	private static class Obj {
		static int idCounter=1;

		private int id = idCounter++;

		private Map<String,String> dictionary = new TreeMap<String,String>();
		private byte[] stream;

		/**
 		 * Set entry in dictionary. Return self to allow method chaining.
		 */
		public Obj set(String key, String value) {
			dictionary.put(key,value);
			return this;
		}

		public Obj setStream (byte[] stream) {
			this.stream = stream;
			return set ("Length", ""+stream.length);
		}

		public Obj setStream (String s) {
			return setStream (s.getBytes());
		}


		public void write (OutputStream out) throws IOException {

			out.write ( ("" + id + " 0 obj\n").getBytes() );

			// PDF specification §7.3.7 Dictionary Objects (key value pairs)
			// Keys 'Type' and 'Subtype' are special.
			out.write ("<< ".getBytes());
			for (String key : dictionary.keySet()) {
				out.write ( ("/" + key + " " + dictionary.get(key) + "\n").getBytes() );
			}
			out.write (">>\n".getBytes());
			
			// PDF specification §7.3.8 Stream Objects
			// Dictionary 'Length' entry is required if stream present.
			if (stream != null) {
				out.write ("stream\n".getBytes());
				System.err.println ("writing stream " + stream.length + " bytes");
				out.write (stream);
				out.write ("\nendstream\n".getBytes());
			}

			out.write ("endobj\n\n".getBytes());

		}

	}


	/**
	 * Read the contents of a file and return as byte array.
	 */
	private static byte[] readFile(File file) throws IOException {
		RandomAccessFile f = new RandomAccessFile(file, "r");
		int length = (int) file.length();
		byte[] data = new byte[length];
		f.readFully(data);
		return data;
 	}


}

