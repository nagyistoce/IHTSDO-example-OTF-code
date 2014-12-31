package gov.va.demo;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

public class AppInitializer {

	private ConceptVersionBI con = null;
	private ViewCoordinate vc = null;
	private EditCoordinate ec = null;
	private BdbTerminologyStore store = null;
	private final UUID searchConUuid = UUID.fromString("49064bb7-cda5-3cb3-b8f7-085139486fa8");
	private final UUID refsetIdentityUid = UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da");
	private BdbTermBuilder builder;

	public AppInitializer() {
		try {
			store = getDB();
			
			vc = getVC();
			ec = getEC();

			con = getBloodPressureConcept();
			
			builder = new BdbTermBuilder(ec, vc);
		} catch (Exception e) {
			// TODO (artf231871) Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeDb() {
		store.shutdown();
	}
	
	EditCoordinate getEC() throws ValidationException, IOException {
        int authorNid   = TermAux.USER.getLenient().getConceptNid();
        int module = Snomed.CORE_MODULE.getLenient().getNid();
        int editPathNid = TermAux.SNOMED_CORE.getLenient().getConceptNid();

        return new EditCoordinate(authorNid, module, editPathNid);
	}

	BdbTerminologyStore getDB() {
		if (store == null) {
			try {
				store = new BdbTerminologyStore();
			} catch (Exception e) {
				// TODO (artf231871) Auto-generated catch block
				e.printStackTrace();
			}
		}

		return store;
	}

	ViewCoordinate getVC() {
		if (vc == null) {
			try {
				vc = StandardViewCoordinates.getSnomedStatedLatest();
			} catch (IOException e) {
				// TODO (artf231871) Auto-generated catch block
				e.printStackTrace();
			}
		}

		return vc;
	}

	ConceptVersionBI getBloodPressureConcept() {

		if (con == null) {
			try {
				ConceptChronicleBI bpConcept = getDB().getConcept(searchConUuid);
				con = bpConcept.getVersion(getVC());

				return con;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return con;
	}

	public TerminologyBuilderBI getBuilder() {
		if (builder == null) {
			try {
				builder = new BdbTermBuilder(getEC(), getVC());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return builder;
	}

	ConceptVersionBI getConcept(UUID conUid) {
		try {
			ConceptChronicleBI reqCon = getDB().getConcept(conUid);
			return reqCon.getVersion(getVC());
		} catch (Exception e) {
			// TODO (artf231871) Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public ConceptVersionBI getRefsetIdentity() {
		return getConcept(refsetIdentityUid);
	}
}
