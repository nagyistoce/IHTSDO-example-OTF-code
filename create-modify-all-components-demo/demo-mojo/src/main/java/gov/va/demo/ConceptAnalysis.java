package gov.va.demo;

import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Hello world!
 * 
 * @goal analysis
 */
public class ConceptAnalysis extends AbstractMojo {


	public void execute() throws MojoExecutionException {
		AppInitializer appInit = new AppInitializer();
		
		BdbTerminologyStore store = appInit.getDB();
		ViewCoordinate vc = appInit.getVC();
		ConceptVersionBI con = appInit.getBloodPressureConcept();
		
		// printAllPathsTest();
		ConceptPrinter printer = new ConceptPrinter(store, vc);
		printer.printConcept(con);
		
		
		System.out.println("\n\n\n******** NEW CONCEPT **********");
		ConceptVersionBI newCon = appInit.getConcept(UUID.fromString("68f2819c-4080-5e81-868d-868e50a80ac8"));
		printer.printConcept(newCon);

	}
	
	public static void main(String[] args) throws MojoExecutionException
	{
		new ConceptAnalysis().execute();
	}

}
