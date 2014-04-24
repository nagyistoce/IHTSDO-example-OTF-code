package gov.va.demo;

import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Hello world!
 * 
 * @goal refset-creation
 */
public class RefsetCreation extends AbstractMojo {
	private static final UUID memberRefsetUid = UUID.fromString("62f00cad-e2f0-5160-b87b-adc581f8b967");
	private static final UUID annotatedMemberRefsetUid = UUID.fromString("92dc393e-65a6-57b7-9139-6e5f5bc0c3c8");
	private static final UUID extensionRefsetUid = UUID.fromString("1de6eb94-dd31-5119-b89c-1fbfeaacc1c0");
	private static final UUID annotatedExtensionRefsetUid = UUID.fromString("d8c615a1-c9e9-5db7-a6ff-fbaf8908645e");

	private AppInitializer appDb;
	private BdbTerminologyStore store;
	private ViewCoordinate vc;
	
	private ConceptVersionBI memberRefsetCon = null;
	private ConceptVersionBI annotatedMemberRefsetCon = null;
	private ConceptVersionBI extensionRefsetCon = null;
	private ConceptVersionBI annotatedExtensionRefsetCon = null;
	private ConceptChronicleBI concept1 = null;
	private ConceptChronicleBI concept2 = null;
	
	public void execute() throws MojoExecutionException {
		appDb = new AppInitializer();

		store = appDb.getDB();
		vc = appDb.getVC();

		try {
			concept1 = appDb.getDB().getConcept(UUID.fromString("b0e20b80-bb2e-38b1-97a4-77a3135559a4"));
			concept2 = appDb.getDB().getConcept(UUID.fromString("7560feb1-0778-314d-bc76-2d5071def2fa"));

			createRefsetConcepts();
			addMembers();

			appDb.getDB().commit();

			RefsetPrinter printer = new RefsetPrinter(appDb);

			printer.printRefsetConcepts();
			printer.printMembers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private void createRefsetConcepts() throws IOException, ContradictionException, InvalidCAB {
		// Member Refsets
		memberRefsetCon = appDb.getConcept(memberRefsetUid);
		annotatedMemberRefsetCon = appDb.getConcept(annotatedMemberRefsetUid);
		
		
		boolean isAnnotated = false;
		ConceptVersionBI parentRefsetCon = appDb.getRefsetIdentity();
		
		if (memberRefsetCon.getDescriptions().size() == 0) {
			isAnnotated = false;
			memberRefsetCon = createNewRefsetConcept(parentRefsetCon, "Test Member Refset Concept (refset)", "Test Member Refset Concept PT", isAnnotated).getVersion(vc);
		} 
		
		if (annotatedMemberRefsetCon.getDescriptions().size() == 0) {
			isAnnotated = true;
			annotatedMemberRefsetCon = createNewRefsetConcept(parentRefsetCon, "Test Annotated Member Refset Concept (refset)", "Test Annotated Member Refset Concept PT", isAnnotated).getVersion(vc);
		} 

		
		// Extension Refsets
		extensionRefsetCon = appDb.getConcept(extensionRefsetUid);
		annotatedExtensionRefsetCon = appDb.getConcept(annotatedExtensionRefsetUid);
		
		if (extensionRefsetCon.getDescriptions().size() == 0) {
			isAnnotated = false;
			extensionRefsetCon = createNewRefsetConcept(parentRefsetCon, "Test Extension Refset Concept (refset)", "Test Extension Refset Concept PT", isAnnotated).getVersion(vc);
		} 
		
		if (annotatedExtensionRefsetCon.getDescriptions().size() == 0) {
			isAnnotated = true;
			annotatedExtensionRefsetCon = createNewRefsetConcept(parentRefsetCon, "Test Annotated Extension Refset Concept (refset)", "Test Annotated Extension Refset Concept PT", isAnnotated).getVersion(vc);
		} 
	}

	private void addMembers() throws IOException, InvalidCAB, ContradictionException {
		// Member Refsets
		addMember(memberRefsetCon, concept1);
		addMember(memberRefsetCon ,concept2);
		addMember(annotatedMemberRefsetCon, concept1);
		addMember(annotatedMemberRefsetCon ,concept2);
	
		// Extension Refsets
		addExtensionMember(extensionRefsetCon, concept1);
		addExtensionMember(extensionRefsetCon ,concept2);
		addExtensionMember(annotatedExtensionRefsetCon, concept1);
		addExtensionMember(annotatedExtensionRefsetCon ,concept2);
	}

	private ConceptChronicleBI createNewRefsetConcept(ConceptVersionBI parent, String fsn, String prefTerm, boolean isAnnotated) throws IOException, InvalidCAB, ContradictionException {
		LanguageCode lc = LanguageCode.EN_US;
		UUID isA = Snomed.IS_A.getUuids()[0];
		IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        UUID parents[] = new UUID[1];
        parents[0] = parent.getPrimordialUuid();

		ConceptCB newConCB = new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parents);
										
		ConceptChronicleBI newCon = appDb.getBuilder().construct(newConCB);

		newCon.setAnnotationStyleRefex(isAnnotated);
		
		appDb.getDB().addUncommitted(newCon);

		return newCon;
	}


	private void addMember(ConceptVersionBI refCon, ConceptChronicleBI refComp) throws IOException, InvalidCAB, ContradictionException {
		RefexCAB newMember = new RefexCAB(RefexType.MEMBER,refComp.getConceptNid(),  refCon.getNid(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
		
		RefexChronicleBI<?> newMemChron = appDb.getBuilder().construct(newMember);

		if (!refCon.isAnnotationStyleRefex()) {
			appDb.getDB().addUncommitted(refCon);
		} else {
			appDb.getDB().addUncommitted(refComp);
		}
	}
	
	private void addExtensionMember(ConceptVersionBI refCon, ConceptChronicleBI refComp) throws IOException, InvalidCAB, ContradictionException {

		RefexCAB newMember = new RefexCAB(RefexType.CID_STR,refComp.getConceptNid(),  refCon.getNid(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);

		int parentRefsetFSN = appDb.getRefsetIdentity().getVersion(vc).getFullySpecifiedDescription().getNid();
		newMember.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, parentRefsetFSN);
		newMember.put(ComponentProperty.STRING_EXTENSION_1, "Testing String");
		
		RefexChronicleBI<?> newMemChron = appDb.getBuilder().construct(newMember);

		if (!refCon.isAnnotationStyleRefex()) {
			appDb.getDB().addUncommitted(refCon);
		} else {
			appDb.getDB().addUncommitted(refComp);
		}
	}
}
