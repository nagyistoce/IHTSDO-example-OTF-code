package gov.va.demo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.SpecFactory;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptVersion;

public class ConceptPrinter {
	private static int snomedAssemblageNid;
	private final UUID searchConUuid = UUID
			.fromString("49064bb7-cda5-3cb3-b8f7-085139486fa8");
	protected static ViewCoordinate vc;
	protected static BdbTerminologyStore store;

	public ConceptPrinter(BdbTerminologyStore store, ViewCoordinate vc) {
		this.vc = vc;
		this.store = store;
	}

	static void printConcept(ConceptVersionBI con) {
		try {
			printConceptAttributes(con);
			System.out.println();

			printAllDescriptions(con);
			System.out.println();

			printAllRelationships(con);

		} catch (IOException io) {
			io.printStackTrace();
		} catch (ContradictionException e) {
			// TODO (artf231874) Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printConceptAttributes(ConceptVersionBI con)
			throws IOException, ContradictionException {
		printIds(con);

		System.out.println("Concept Fully Defined: "
				+ con.getConceptAttributes().getVersion(vc).isDefined());

		printStamp(con);

	}

	public void printAllVersions(DescriptionChronicleBI fullDesc) throws IOException {
		int i = 0;
		
		for (DescriptionVersionBI desc : fullDesc.getVersions()) {
			System.out.println("Version #" + i++);
			printDescription(desc);

			System.out.println("\n\n");
		}
	}
	
	public void printAllVersions(RelationshipChronicleBI fullRel) throws IOException {
		int i = 0;

		for (RelationshipVersionBI rel : fullRel.getVersions()) {
			System.out.println("Version #" + i++);
			printRelationship(rel);
			
			System.out.println("\n\n");
		}
	}

	public void printAllVersions(ConceptChronicleBI fullCon) throws IOException {
		int i = 0;
		
		for (ConceptAttributeVersionBI con : fullCon.getConceptAttributes().getVersions()) {
			System.out.println("Version #" + i++);
			printConceptAttributes(con);

			System.out.println("\n\n");
		}
	}


	private static void printAllDescriptions(ConceptChronicleBI con)
			throws IOException, ContradictionException {
		Collection<? extends DescriptionChronicleBI> allDesc = con
				.getDescriptions();
		int i = 0;

		for (DescriptionChronicleBI descAllVersoins : allDesc) {
			DescriptionVersionBI desc = descAllVersoins.getVersion(vc);

			// Description-based attributes
			if (desc != null) {
				System.out.println("Description #" + ++i);
				printDescription(desc);
			} else {
				System.out
						.println("No Description available at View cordinate");
			}
		}
	}

	private static void printDescription(DescriptionVersionBI desc) throws IOException {
		String text = desc.getText();
		String type = getDescription(desc.getTypeNid());
		boolean initCap = desc.isInitialCaseSignificant();
		String lang = desc.getLang();

		printIds(desc);
		printStamp(desc);

		System.out.println("Desc Text: " + text);
		System.out.println("Desc Type: " + type);
		System.out.println("Desc Initial Cap Status: " + initCap);
		System.out.println("Desc Language: " + lang + "\n");
	}

	private static void printIds(ComponentVersionBI comp) throws IOException {
		// Ids
		int nid = comp.getNid();

		UUID primUuid = comp.getPrimordialUuid();
		List<UUID> uuids = comp.getUUIDs();
		Collection<? extends IdBI> ids = comp.getAllIds();

		System.out.println("Nid: " + nid + " and primUuid: "
				+ primUuid.toString());
		System.out.print("Other UUIDs: ");

		if (uuids.size() == 1) {
			System.out.println("None");
		} else {
			System.out.println();
			for (UUID uid : uuids) {
				System.out.println(uid);
			}
		}

		System.out.println("Other Ids");
		String sctidString = null;

		if (ids.size() == 0) {
			System.out.println("No other Ids");
		} else {
			for (RefexChronicleBI<?> annotation : comp.getAnnotations()) {
				if (annotation.getAssemblageNid() == snomedAssemblageNid) {
					RefexLongVersionBI sctid = (RefexLongVersionBI) annotation
							.getPrimordialVersion();
					sctidString = Long.toString(sctid.getLong1());

					System.out.println(sctidString);
				}
			}

		}

	}

	private static void printStamp(ComponentVersionBI comp) throws IOException {
		// STAMP
		Status status = comp.getStatus();
		String time = translateTime(comp.getTime());
		String author = getDescription(comp.getAuthorNid());
		String module = getDescription(comp.getModuleNid());
		String path = getDescription(comp.getPathNid());

		System.out.println("Stamp: " + status + " - " + time + " - " + author
				+ " - " + module + " - " + path);
	}

	private static String getDescription(int nid) throws IOException {
		ConceptChronicle conChron = Bdb.getConcept(nid);
		ConceptVersion con = conChron.getVersion(vc);

		ConceptSpec conSpec = SpecFactory.get(con);

		return conSpec.getDescription();
	}

	private static String translateTime(long time) {
		return TimeHelper.formatDate(time);
	}

	private static void printAllRelationships(ConceptVersionBI con)
			throws IOException, ContradictionException {
		Collection<? extends ConceptVersionBI> children = con
				.getRelationshipsIncomingOriginsActiveIsa();
		Collection<? extends ConceptVersionBI> parents = con
				.getRelationshipsOutgoingDestinationsActiveIsa();
		Collection<? extends RelationshipVersionBI> incomingRels = con
				.getRelationshipsIncomingActive();
		Collection<? extends RelationshipVersionBI> outgoingRels = con
				.getRelationshipsOutgoingActive();

		int printed = 0;
		int i = 0;
		for (RelationshipVersionBI rel : outgoingRels) {
			if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
				System.out.print("Concept Parent #" + ++i + ":  ");
				System.out.println(getDescription(rel.getDestinationNid()));
			}
		}
		System.out.println();
		printed += i;

		i = 0;
		for (RelationshipVersionBI rel : incomingRels) {
			if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
				System.out.print("Concept Child #" + ++i + ":  ");
				System.out.println(getDescription(rel.getOriginNid()));
			}
		}
		System.out.println();
		printed += i;

		i = 0;
		for (RelationshipVersionBI rel : outgoingRels) {
			if (rel.getTypeNid() != Snomed.IS_A.getNid()) {
				System.out.println("Source Role #" + ++i);
				printRelationship(rel);
				System.out.println();
			}
		}
		System.out.println();
		printed += i;

		i = 0;
		for (RelationshipVersionBI rel : incomingRels) {
			if (rel.getTypeNid() != Snomed.IS_A.getNid()) {
				System.out.println("Destination Role #" + ++i);
				printRelationship(rel);
				System.out.println();
			}
		}
		printed += i;
		if (printed == 0) {
			System.out.println("No Relationships were found on the concept");
		}
	}

	private static void printRelationship(RelationshipVersionBI rel)
			throws IOException {
		printIds(rel);
		printStamp(rel);

		// Relationship Information
		String type = getDescription(rel.getTypeNid());
		String origin = getDescription(rel.getOriginNid());
		String dest = getDescription(rel.getDestinationNid());

		// Relatoinship-based attributes
		String charId = getDescription(rel.getCharacteristicNid());
		int group = rel.getGroup();
		String refine = getDescription(rel.getRefinabilityNid());
		boolean stated = rel.isStated();

		System.out.println("Relationship Origin Concept: " + origin);
		System.out.println("Relationship Type: " + type);
		System.out.println("Relationship Destination Concept: " + dest);

		System.out.println("Relationship Characteristic Id: " + charId);
		System.out.println("Relationship Group Id: " + group);
		System.out.println("Relationship Refinability: " + charId);
		System.out.println("Relationship is Stated?: " + stated);

	}

	private void printConceptAttributes(ConceptAttributeVersionBI con) throws IOException {
		printIds(con);
		printStamp(con);
		System.out.println("Concept isFullyDefined: " + con.isDefined());

	}

	private static void printAllPathsTest() {

		Set<Path> allPaths = Bdb.getPathManager().getAll();

		for (Path p : allPaths) {
			ConceptSpec spec;
			try {
				spec = p.getConceptSpec();
				String s = spec.getDescription();
				System.out.println(s);
			} catch (IOException e) {
				// TODO (artf231874) Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
