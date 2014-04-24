package gov.va.demo;

import java.util.Collection;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;


/**
 * Hello world!
 * 
 * @goal refset-printer
 */

public class RefsetPrinter extends AbstractMojo {
	private AppInitializer appDb;
	private static final UUID memberRefsetUid = UUID.fromString("62f00cad-e2f0-5160-b87b-adc581f8b967");
	private static final UUID annotatedMemberRefsetUid = UUID.fromString("92dc393e-65a6-57b7-9139-6e5f5bc0c3c8");
	private static final UUID extensionRefsetUid = UUID.fromString("1de6eb94-dd31-5119-b89c-1fbfeaacc1c0");
	private static final UUID annotatedExtensionRefsetUid = UUID.fromString("d8c615a1-c9e9-5db7-a6ff-fbaf8908645e");

	private ConceptVersionBI memberRefsetCon = null;
	private ConceptVersionBI annotatedMemberRefsetCon = null;
	private ConceptVersionBI extensionRefsetCon = null;
	private ConceptVersionBI annotatedExtensionRefsetCon = null;
	private ConceptChronicleBI concept1 = null;
	private ConceptChronicleBI concept2 = null;

	private ConceptPrinter printer = null;
	
	public RefsetPrinter() {
    
    }
    
	public RefsetPrinter(AppInitializer appDb) {
		this.appDb = appDb;
		
		printer = new ConceptPrinter(appDb.getDB(), appDb.getVC());

		setupConcepts();
	}

	public void execute() throws MojoExecutionException {
		appDb = new AppInitializer();
		printer = new ConceptPrinter(appDb.getDB(), appDb.getVC());
		
		setupConcepts();

		printRefsetConcepts();
		printMembers();
	}
	

	private void setupConcepts() {
		try {
			concept1 = appDb.getDB().getConcept(UUID.fromString("b0e20b80-bb2e-38b1-97a4-77a3135559a4"));
			concept2 = appDb.getDB().getConcept(UUID.fromString("7560feb1-0778-314d-bc76-2d5071def2fa"));
			
			memberRefsetCon = appDb.getConcept(memberRefsetUid);
			annotatedMemberRefsetCon = appDb.getConcept(annotatedMemberRefsetUid);
			extensionRefsetCon = appDb.getConcept(extensionRefsetUid);
			annotatedExtensionRefsetCon = appDb.getConcept(annotatedExtensionRefsetUid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printRefsetConcepts() {
		// Member Refsets
		System.out.println("\n\n\n******** NEW Member Refset CONCEPTS **********");
		printer.printConcept(memberRefsetCon);
		System.out.println("\n\n\n");
		printer.printConcept(annotatedMemberRefsetCon);
		System.out.println("\n\n\n");

		// Extension Refsets
		System.out.println("\n\n\n******** NEW Extension Refset CONCEPTS **********");
		printer.printConcept(extensionRefsetCon);
		System.out.println("\n\n\n");
		printer.printConcept(annotatedExtensionRefsetCon);
		System.out.println("\n\n\n");

	}

	public void printMembers() {
		try {
			// Member Refsets
			System.out.println("\n\n\n******** Refset Members **********");
			printRefsetMembers(memberRefsetCon);
			System.out.println("\n\n\n");
			printRefsetMembers(annotatedMemberRefsetCon);
			System.out.println("\n\n\n");
			
			System.out.println("\n\n\n******** Annotated Members **********");
			printAnnotatedRefsetMembers(concept1);
			System.out.println("\n\n\n");
			printAnnotatedRefsetMembers(concept2);
	
			// Extension Refsets
			System.out.println("\n\n\n******** Refset Extensions **********");
			printRefsetMembers(extensionRefsetCon);
			System.out.println("\n\n\n");
			printRefsetMembers(annotatedExtensionRefsetCon);
			System.out.println("\n\n\n");
			
			System.out.println("\n\n\n******** Annotated Extensions **********");
			printAnnotatedRefsetMembers(concept1);
			System.out.println("\n\n\n");
			printAnnotatedRefsetMembers(concept2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printAnnotatedRefsetMembers(ConceptChronicleBI con) throws Exception {
		System.out.println("Annotated Concept: " + con.getVersion(appDb.getVC()).getPreferredDescription().getText());
		Collection<? extends RefexChronicleBI<?>> members = con.getAnnotations();
		System.out.println("With " + members.size() + " Members");
		
		for (RefexChronicleBI rChron : members) {
			printAllVersionsOfAnnotated(rChron);
		}
	}

	private void printRefsetMembers(ConceptVersionBI extensionRefsetCon) throws Exception {
		System.out.println("Refset Concept: " + extensionRefsetCon.getPreferredDescription().getText());
		Collection<? extends RefexChronicleBI<?>> members = extensionRefsetCon.getRefsetMembers();
		System.out.println("With " + members.size() + " Members: ");
		
		for (RefexChronicleBI rChron : members) {
			printAllVersionsOfMember(rChron);
		}
	}

	public void printAllVersionsOfMember(RefexChronicleBI refsetChron) throws Exception {
		for (int i = 0; i < refsetChron.getVersions().size(); i++) {
			System.out.println("Version #: " + (i + 1));
			RefexVersionBI member = (RefexVersionBI)refsetChron.getVersions().toArray()[i];
			ConceptVersionBI con = appDb.getDB().getConcept(member.getReferencedComponentNid()).getVersion(appDb.getVC());
			 
			if (member.getRefexType() == RefexType.MEMBER) {
				System.out.println(con.getPreferredDescription().getText() + " with Status: " + member.getStatus() + "\n");
			} else if (member.getRefexType() == RefexType.CID_STR) {
				RefexNidStringVersionBI extensionMember = (RefexNidStringVersionBI)member;
				String strExt = extensionMember.getString1();
				int cidExtNid = extensionMember.getNid1();
		        DescriptionVersionBI cidExtCon = (DescriptionVersionBI) appDb.getDB().getComponent(cidExtNid).getVersion(appDb.getVC());

				System.out.println(con.getPreferredDescription().getText() + " of Member Type with Status: " + member.getStatus());
				System.out.println("Is extended with CID: " + cidExtCon.getText() + " and String: " + strExt+ "\n");
			} 
		}
	}

	public void printAllVersionsOfAnnotated(RefexChronicleBI refsetChron) throws Exception {
		for (int i = 0; i < refsetChron.getVersions().size(); i++) {
			System.out.println("Version #: " + (i + 1));
			RefexVersionBI member = (RefexVersionBI)refsetChron.getVersions().toArray()[i];
			
			ConceptVersionBI refCon = appDb.getDB().getConcept(member.getAssemblageNid()).getVersion(appDb.getVC());
			
			if (member.getRefexType() == RefexType.MEMBER) {
				System.out.println(refCon.getPreferredDescription().getText() + " with Status: " + member.getStatus() + "\n");
			} else if (member.getRefexType() == RefexType.CID_STR) {
				RefexNidStringVersionBI extensionMember = (RefexNidStringVersionBI)member;
				String strExt = extensionMember.getString1();
				int cidExtNid = extensionMember.getNid1();
				DescriptionVersionBI cidExtCon = (DescriptionVersionBI) appDb.getDB().getComponent(cidExtNid).getVersion(appDb.getVC());

				System.out.println(refCon.getPreferredDescription().getText() + " of CID_STR Type with Status: " + member.getStatus());
				System.out.println("Is extended with CID: " + cidExtCon.getText() + " and String: " + strExt);
				System.out.println("\n");
			} 
		} 
	}
}

