package gov.va.demo;

import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Hello world!
 * 
 * @goal refset-modify
 */
public class RefsetModify extends AbstractMojo
{
	private AppInitializer appDb;
	BdbTerminologyStore store;
	ViewCoordinate vc;
	private static final UUID memberRefsetUid = UUID.fromString("62f00cad-e2f0-5160-b87b-adc581f8b967");
	private static final UUID annotatedMemberRefsetUid = UUID.fromString("92dc393e-65a6-57b7-9139-6e5f5bc0c3c8");
	private static final UUID extensionRefsetUid = UUID.fromString("1de6eb94-dd31-5119-b89c-1fbfeaacc1c0");
	private static final UUID annotatedExtensionRefsetUid = UUID.fromString("d8c615a1-c9e9-5db7-a6ff-fbaf8908645e");
	
	ConceptVersionBI memberRefsetCon = null;
	ConceptVersionBI annotatedMemberRefsetCon = null;
	ConceptVersionBI extensionRefsetCon = null;
	ConceptVersionBI annotatedExtensionRefsetCon = null;

	RefexChronicleBI<?> member = null;
	RefexChronicleBI<?> aMember = null;
	RefexChronicleBI<?> extMember = null;
	RefexChronicleBI<?> extAnnotatedMember = null;
	
	public void execute() throws MojoExecutionException
	{
		appDb = new AppInitializer();

		store = appDb.getDB();
		vc = appDb.getVC();

		BdbTerminologyStore store = appDb.getDB();
		ViewCoordinate vc = appDb.getVC();

		memberRefsetCon = appDb.getConcept(memberRefsetUid);
		annotatedMemberRefsetCon = appDb.getConcept(annotatedMemberRefsetUid);
		extensionRefsetCon = appDb.getConcept(extensionRefsetUid);
		annotatedExtensionRefsetCon = appDb.getConcept(annotatedExtensionRefsetUid);
		
		
		ConceptChronicleBI concept1 = null;

		try {
			concept1 = appDb.getDB().getConcept(UUID.fromString("b0e20b80-bb2e-38b1-97a4-77a3135559a4"));
	
			
			// Member Refset
			member = memberRefsetCon.getRefsetMembers().iterator().next();
			for (RefexChronicleBI<?> annotations : concept1.getAnnotations()) {
				RefexChronicleBI<?> annot = annotations;
				if (annot.getAssemblageNid() == annotatedMemberRefsetCon.getNid()) {
					aMember = annot;
				}
			}

			extMember = extensionRefsetCon.getRefsetMembers().iterator().next();
			for (RefexChronicleBI<?> annotations : concept1.getAnnotations()) {
				RefexChronicleBI<?> annot = annotations;
				if (annot.getAssemblageNid() == annotatedExtensionRefsetCon.getNid()) {
					extAnnotatedMember = annot;
				}
			}

			modifyMembers();
			retireMembers();
			
			printAllVersions();
			
		} catch (Exception e) {
			throw new MojoExecutionException("Unexpected Failure", e);
		}
	}
	
	private void modifyMembers() throws ContradictionException, InvalidCAB, IOException {
		modifyExtensionRefsetMember(extMember);
		modifyExtensionAnnotatedRefset(extAnnotatedMember);
	}

	private void modifyExtensionAnnotatedRefset(RefexChronicleBI<?> memberChron) throws ContradictionException, InvalidCAB, IOException {
		RefexVersionBI<?> mem = memberChron.getVersion(vc);
		RefexCAB bp = mem.makeBlueprint(vc,  IdDirective.PRESERVE, RefexDirective.INCLUDE);
		if (bp.getMemberUUID() == null) {
			bp.setMemberUuid(mem.getPrimordialUuid());
		}
		
		// Change String
		bp.put(ComponentProperty.STRING_EXTENSION_1, "Modified Testing String");

		
		// Change Description to Pref Term
		int parentRefsetPT = appDb.getRefsetIdentity().getVersion(vc).getPreferredDescription().getNid();
		bp.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, parentRefsetPT);
		
		RefexChronicleBI<?> cabi = appDb.getBuilder().constructIfNotCurrent(bp);
		ConceptVersionBI refCon = appDb.getDB().getConcept(mem.getReferencedComponentNid()).getVersion(appDb.getVC());

		appDb.getDB().addUncommitted(refCon);
	}

	private void modifyExtensionRefsetMember(RefexChronicleBI<?> memberChron) throws ContradictionException, InvalidCAB, IOException {
		RefexVersionBI<?> mem = memberChron.getVersion(vc);
		RefexCAB bp = mem.makeBlueprint(vc,  IdDirective.PRESERVE, RefexDirective.INCLUDE);
		if (bp.getMemberUUID() == null) {
			bp.setMemberUuid(mem.getPrimordialUuid());
		}
		
		// Change String
		bp.put(ComponentProperty.STRING_EXTENSION_1, "Modified Testing String");

		
		// Change Description to Pref Term
		int parentRefsetPT = appDb.getRefsetIdentity().getVersion(vc).getPreferredDescription().getNid();
		bp.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, parentRefsetPT);
		
		RefexChronicleBI<?> cabi = appDb.getBuilder().constructIfNotCurrent(bp);
		ConceptVersionBI refCon = appDb.getDB().getConcept(mem.getAssemblageNid()).getVersion(appDb.getVC());

		appDb.getDB().addUncommitted(refCon);
	}

	private void retireMembers() throws ContradictionException, IOException, InvalidCAB {
		retireRefsetMember(member);
		retireAnnotatedRefsetMember(aMember);
		retireRefsetMember(extMember);
		retireAnnotatedRefsetMember(extAnnotatedMember);
	}

	private void retireAnnotatedRefsetMember(RefexChronicleBI<?> memberChron) throws ContradictionException, IOException, InvalidCAB {
		RefexVersionBI<?> mem = memberChron.getVersion(vc);
		RefexCAB bp = mem.makeBlueprint(vc,  IdDirective.PRESERVE, RefexDirective.INCLUDE);
		if (bp.getMemberUUID() == null) {
			bp.setMemberUuid(mem.getPrimordialUuid());
		}
		
		bp.setStatus(Status.INACTIVE);
		RefexChronicleBI<?> cabi = appDb.getBuilder().constructIfNotCurrent(bp);
		ConceptVersionBI refCon = appDb.getDB().getConcept(mem.getReferencedComponentNid()).getVersion(appDb.getVC());

		appDb.getDB().addUncommitted(refCon);
	}

	private void retireRefsetMember(RefexChronicleBI<?> memberChron) throws IOException, ContradictionException, InvalidCAB {
		RefexVersionBI<?> mem = memberChron.getVersion(vc);
		RefexCAB bp = mem.makeBlueprint(vc,  IdDirective.PRESERVE, RefexDirective.INCLUDE);
		if (bp.getMemberUUID() == null) {
			bp.setMemberUuid(mem.getPrimordialUuid());
		}
		bp.setStatus(Status.INACTIVE);
		RefexChronicleBI<?> cabi = appDb.getBuilder().constructIfNotCurrent(bp);
		ConceptVersionBI refCon = appDb.getDB().getConcept(mem.getAssemblageNid()).getVersion(appDb.getVC());

		appDb.getDB().addUncommitted(refCon);
	}

	private void printAllVersions() throws Exception {
		RefsetPrinter printer = new RefsetPrinter(appDb);
		
		System.out.println("\n\n\n\n\n****Printing two versions of regular member ****");
		printer.printAllVersionsOfMember((RefexChronicleBI)member);
		
		System.out.println("\n\n\n\n\n****Printing two versions of annotated member ****");
		printer.printAllVersionsOfAnnotated((RefexChronicleBI)aMember);

		System.out.println("\n\n\n\n\n****Printing three versions of regular extension ****");
		printer.printAllVersionsOfMember((RefexChronicleBI)extMember);
		
		System.out.println("\n\n\n\n\n****Printing three versions of annotated extension****");
		printer.printAllVersionsOfAnnotated((RefexChronicleBI)extAnnotatedMember);
}

}
