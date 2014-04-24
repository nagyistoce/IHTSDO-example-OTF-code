package gov.va.demo;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

/**
 * Hello world!
 * 
 * @goal modify
 */
public class ComponentModify extends AbstractMojo
{
	private AppInitializer appDb;
	BdbTerminologyStore store;
	ViewCoordinate vc;

	public void execute() throws MojoExecutionException
	{
		try
		{
			appDb = new AppInitializer();

			store = appDb.getDB();
			vc = appDb.getVC();

			ConceptChronicleBI newConcept = store.getConcept(UUID.fromString("68f2819c-4080-5e81-868d-868e50a80ac8"));
			
			//TODO these don't work....
			DescriptionChronicleBI desc = newConcept.getDescriptions().iterator().next();
			RelationshipChronicleBI rel = newConcept.getRelationshipsOutgoing().iterator().next();

			modifyDescAttributes(desc);
			modifyRelationshipAttributes(rel);
			modifyConceptAttributes(newConcept);

			retireDesc(desc);
			retireRel(rel);
			retireConcept(newConcept);
			
			printAllVersions(desc, rel, newConcept);

		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected Failure", e);
		}
	}
	
	private void retireConcept(ConceptChronicleBI newConcept) throws IOException, InvalidCAB, ContradictionException {
		ConceptAttributeVersionBI attr = newConcept.getConceptAttributes().getVersion(vc);
		ConceptAttributeAB acab = attr.makeBlueprint(vc,  IdDirective.PRESERVE,  RefexDirective.EXCLUDE);
		
		acab.setStatus(Status.INACTIVE);
		ConceptAttributeChronicleBI cabi = appDb.getBuilder().constructIfNotCurrent(acab);
		
		appDb.getDB().addUncommitted(cabi.getEnclosingConcept());
	}

	private void retireRel(RelationshipChronicleBI fullRel) throws ContradictionException, InvalidCAB, IOException {
		RelationshipVersionBI rel = fullRel.getVersion(vc);
//		RelationshipCAB rcab = rel.makeBlueprint(vc,  IdDirective.PRESERVE,  RefexDirective.EXCLUDE);
		
		// Need to put in fix to handle QUALIFIERS
		RelationshipCAB rcab = new RelationshipCAB(rel.getConceptNid(), rel.getTypeNid(), rel.getDestinationNid(), 1, RelationshipType.QUALIFIER, rel, vc, IdDirective.PRESERVE, RefexDirective.EXCLUDE);

		rcab.setStatus(Status.INACTIVE);
		
		RelationshipChronicleBI rcbi = appDb.getBuilder().constructIfNotCurrent(rcab);
		
		appDb.getDB().addUncommitted(rcbi.getEnclosingConcept());
		
	}

	private void retireDesc(DescriptionChronicleBI fullDesc) throws ContradictionException, InvalidCAB, IOException {
		DescriptionVersionBI desc = fullDesc.getVersion(vc);
		DescriptionCAB dcab = desc.makeBlueprint(vc,  IdDirective.PRESERVE, RefexDirective.EXCLUDE);
		dcab.setStatus(Status.INACTIVE);
		
		DescriptionChronicleBI dcbi = appDb.getBuilder().constructIfNotCurrent(dcab);
		
		appDb.getDB().addUncommitted(dcbi.getEnclosingConcept());
	}

	private void printAllVersions(DescriptionChronicleBI desc,
			RelationshipChronicleBI rel, ConceptChronicleBI newConcept) throws IOException {
		ConceptPrinter printer = new ConceptPrinter(store, vc);
		
		System.out.println("\n\n\n\n\n****Printing two versions of descripition ****");
		printer.printAllVersions(desc);
		
		System.out.println("\n\n\n\n\n****Printing two versions of relationship****");
		printer.printAllVersions(rel);
		
		System.out.println("\n\n\n\n\n****Printing two versions of concept attributes****");
		printer.printAllVersions(newConcept);
		
	}

	private void modifyDescAttributes(DescriptionChronicleBI fullDesc) throws PropertyVetoException, IOException, ContradictionException, InvalidCAB
	{
		DescriptionVersionBI desc = fullDesc.getVersion(vc);
		DescriptionCAB dcab = new DescriptionCAB(desc.getConceptNid(), SnomedMetadataRfx.getDES_SYNONYM_NID(), LanguageCode.DA_DK, "New Text attempt", true, desc, vc, IdDirective.PRESERVE, RefexDirective.EXCLUDE);
		DescriptionChronicleBI dcbi = appDb.getBuilder().constructIfNotCurrent(dcab);
		
		appDb.getDB().addUncommitted(dcbi.getEnclosingConcept());
	}
	
	private void modifyRelationshipAttributes(RelationshipChronicleBI fullRel) throws IOException, InvalidCAB, ContradictionException
	{
		RelationshipVersionBI rel = fullRel.getVersion(vc);
		// Need to get in fix for Group Id
		RelationshipCAB rcab = new RelationshipCAB(rel.getConceptNid(), rel.getTypeNid(), rel.getDestinationNid(), 1, RelationshipType.QUALIFIER, rel, vc, IdDirective.PRESERVE, RefexDirective.EXCLUDE);
		RelationshipChronicleBI rcbi = appDb.getBuilder().constructIfNotCurrent(rcab);
		
		appDb.getDB().addUncommitted(rcbi.getEnclosingConcept());
	}
	
	private void modifyConceptAttributes(ConceptChronicleBI con) throws IOException, ContradictionException, InvalidCAB
	{
		ConceptAttributeAB cab = new ConceptAttributeAB(con.getConceptNid(), true, RefexDirective.EXCLUDE);
		// Need to add a fix for storing isDefined 
		
		ConceptAttributeChronicleBI cabi = appDb.getBuilder().constructIfNotCurrent(cab);
		
		appDb.getDB().addUncommitted(cabi.getEnclosingConcept());
	}
	
	public static void main(String[] args) throws MojoExecutionException
	{
		new ComponentModify().execute();
	}
}
