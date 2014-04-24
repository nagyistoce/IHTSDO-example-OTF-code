package gov.va.demo;

import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Hello world!
 * 
 * @goal creation
 */
public class ComponentCreation extends AbstractMojo {
	private AppInitializer appDb;

	public void execute() throws MojoExecutionException {
		appDb = new AppInitializer();

		BdbTerminologyStore store = appDb.getDB();
		ViewCoordinate vc = appDb.getVC();
		ConceptVersionBI con = appDb.getBloodPressureConcept();
		ConceptVersionBI newCon = null;

		
		try {
			createNewDescription(con);
			ConceptChronicleBI newConChron = createNewConcept(con);
			newCon = newConChron.getVersion(appDb.getVC());
			
			createNewRelationship(con, newCon);
			appDb.getDB().commit();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidCAB e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		
		
		ConceptPrinter printer = new ConceptPrinter(store, vc);
		printer.printConcept(con);
		System.out.println("\n\n\n******** NEW CONCEPT **********");
		printer.printConcept(newCon);
	}

	private ConceptChronicleBI createNewConcept(ConceptVersionBI con) throws IOException, InvalidCAB, ContradictionException {
		String fsn = "New Test Concept (observable entity)";
		String prefTerm = "New Test Concept PT";
		LanguageCode lc = LanguageCode.EN_US;
		UUID isA = Snomed.IS_A.getUuids()[0];
		IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        UUID parents[] = new UUID[1];
        parents[0] = con.getPrimordialUuid();

		ConceptCB newConCB = new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parents);
										
		ConceptChronicleBI newCon = appDb.getBuilder().construct(newConCB);
		appDb.getDB().addUncommitted(newCon);

		return newCon;

	}

	private void createNewRelationship(ConceptVersionBI con, ConceptVersionBI newCon) throws IOException, InvalidCAB, ContradictionException {
		UUID typeUid =  Snomed.ASSOCIATED_WITH.getLenient().getPrimordialUuid();
		int group = 0;
		RelationshipType relType = RelationshipType.STATED_ROLE;
		IdDirective idDir = IdDirective.GENERATE_HASH;

				
		RelationshipCAB newRel = new RelationshipCAB(con.getPrimordialUuid(), typeUid, newCon.getPrimordialUuid(),
													 group, relType, idDir);
		appDb.getBuilder().construct(newRel);
		appDb.getDB().addUncommitted(con);
		
	}

	private void createNewDescription(ConceptVersionBI con) throws IOException, InvalidCAB, ContradictionException {
		DescriptionCAB newDesc = new DescriptionCAB(con.getConceptNid(), SnomedMetadataRfx.getDES_SYNONYM_NID(), LanguageCode.EN_US, 
													"Test Description #1", true, IdDirective.GENERATE_HASH);
		appDb.getBuilder().construct(newDesc);
		appDb.getDB().addUncommitted(con);
	}

}
