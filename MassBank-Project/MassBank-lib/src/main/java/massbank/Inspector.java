package massbank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;


/**
 * This class converts a record file to a html file for inspection.
 * @author rmeier
 * @version 01-03-2019
 */
public class Inspector {
	private static final Logger logger = LogManager.getLogger(Inspector.class);
	

	
	public static void main(String[] arguments) throws Exception {
		if (arguments.length==2) {
			String input = FileUtils.readFileToString(new File(arguments[0]), StandardCharsets.UTF_8);
			Validator.hasNonStandardChars(input);
			Record record = Validator.validate(input, "");
			if (record == null) {
				logger.error("Error in " + arguments[0]+ ". Exiting...");
				System.exit(1);
			}
			else {
				logger.trace("Validation passed for " + arguments[0] + ".");
			}
			
			String accession = record.ACCESSION();
			String shortname = record.RECORD_TITLE().get(0)+ " Mass Spectrum";
			// find InChIKey in CH_LINK
			String inchikey = null;
			for (Pair<String,String> link : record.CH_LINK()) {
				if ("INCHIKEY".equals(link.getKey())) {
					inchikey=link.getValue();
				}
			}
			String keywords =
				accession + ", " 
				+ shortname +", "
				+ (inchikey != null ? inchikey + ", " : "")
			    + "mass spectrum, MassBank record, mass spectrometry, mass spectral library";
			String description	= 
				"This MassBank Record with Accession " + accession + 
				" contains the " + record.AC_MASS_SPECTROMETRY_MS_TYPE() + " mass spectrum" + 
				" of '" + record.RECORD_TITLE().get(0) + "'" +
				(inchikey != null ? " with the InChIKey '" + inchikey + "'" : "") + 
				".";
			String recordstring = record.createRecordString();
			String structureddata = record.createStructuredData();
			IAtomContainer mol = record.CH_SMILES_obj();
			String svg = new DepictionGenerator().withAtomColors().depict(mol).toSvgStr(Depiction.UNITS_PX);				
			
			
			String css = FileUtils.readFileToString(new File(arguments[0]), StandardCharsets.UTF_8);

			
			
			StringBuilder sb = new StringBuilder();
			sb.append(
			"<!DOCTYPE html>\n" + 
			"<html lang=\"en\">\n" + 
			"<head>\n" + 
			"	<title>"+shortname+"</title>\n" + 
			"	<meta charset=\"UTF-8\">\n" + 
			"	<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">\n" + 
			"	<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">\n" + 
			"	<link rel=\"stylesheet\" href=\"https://www.w3schools.com/lib/w3-theme-grey.css\">\n" + 
			"	<link rel=\"stylesheet\" type=\"text/css\" href=\"css.new/massbank.css\">\n" + 
			"	<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js\"></script>\n" + 

			"	<!-- 	hier anpassen -->\n" + 
			"	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" + 
			"	<meta name=\"variableMeasured\" content=\"m/z\">\n" + 
			"	\n" + 
			"	<link rel=\"stylesheet\" type=\"text/css\" href=\"css/Common.css\">\n" + 
			"	<script type=\"text/javascript\" src=\"script/Common.js\"></script>\n" + 
			"	<!-- SpeckTackle dependencies-->\n" + 
			"	<script src=\"https://d3js.org/d3.v3.min.js\"></script>\n" + 
			"	<!-- SpeckTackle library-->\n" + 
			"	<script type=\"text/javascript\" src=\"script/st.min.js\" charset=\"utf-8\"></script>\n" + 
			"	<!-- SpeckTackle style sheet-->\n" + 
			"	<link rel=\"stylesheet\" href=\"css/st.css\" type=\"text/css\" />\n" + 
			"	<!-- SpeckTackle MassBank loading script-->\n" + 
			"	<script type=\"text/javascript\" src=\"script/massbank_specktackle.js\"></script>\n" + 
			structureddata + "\n" +
			css + "\n" +
			"</head>\n"
			);
			
			sb.append(
			"<body class=\"w3-theme-gradient\">\n" + 
			"  	<header class=\"w3-container w3-top w3-text-dark-grey w3-grey\">\n" + 
			"		<div class=\"w3-bar\">\n" + 
			"			<div class=\"w3-left\">\n" + 
			"				<h1>\n" + 
			"					<b>MassBank Record: "+ accession + "</b>\n" + 
			"				</h1>\n" + 
			"			</div>\n" + 
			"	<div class=\"w3-padding\">\n" + 
			"		<h3><b>" + record.RECORD_TITLE1() +"</b></h3>\n" + 
			"			<div class=\"w3-row w3-padding-small\">\n" + 
			"				<div class=\"w3-twothird w3-text-grey w3-small w3-padding-small\">\n" + 
			"					Mass Spectrum\n" + 
			"					<div id=\"spectrum_canvas\" peaks=\"${peaks}\" style=\"height:200px; width:600px; max-width:100%; background-color:white\"></div>\n" + 
			"				</div>\n" + 
			"				<div class=\"w3-third w3-text-grey w3-small w3-padding-small\">\n" + 
			"					Chemical Structure<br>\n" + 
			svg + "\n" + 
			"				</div>\n" + 
			"			</div>\n" + 
			"	</div>\n" + 
			
			"	<div class=\"monospace w3-padding w3-small\" style=\"height:auto;margin:auto\">\n" + 
			"		<hr>\n" + 
			recordstring +"\n" + 
			"	</div>\n" + 
			"</body>\n" + 
			"</html>"
			);

			File file = new File(arguments[1]);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			    writer.write(sb.toString());
			}
			
		}
		else {
			logger.error("Input and output file required. Exiting...");
		}
	}
}
