package gov.va.demo;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Hello world!
 * 
 * @goal retire
 */
public class ComponentRetire extends AbstractMojo
{
	private AppInitializer appDb;
	ViewCoordinate vc;

	public void execute() throws MojoExecutionException
	{
		try
		{
			appDb = new AppInitializer();

			BdbTerminologyStore store = appDb.getDB();
			vc = appDb.getVC();

			ConceptVersionBI newConcept = store.getConcept(UUID.fromString("68f2819c-4080-5e81-868d-868e50a80ac8")).getVersion(vc);

			retireDesc(newConcept.getDescriptionsActive().iterator().next());
			retireRelationship(newConcept.getRelationshipsOutgoing().iterator().next().getVersion(vc));
			
			//TODO this is broken, don't know why.
			//retireConcept(newConcept);

		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unexpected Failure", e);
		}
	}

	private void retireDesc(DescriptionVersionBI description) throws ContradictionException, InvalidCAB, IOException
	{
		DescriptionCAB dcab = description.makeBlueprint(vc, IdDirective.PRESERVE, RefexDirective.INCLUDE);
		dcab.setRetired();
		DescriptionChronicleBI dcbi = appDb.getBuilder().constructIfNotCurrent(dcab);
		appDb.getDB().addUncommitted(dcbi.getEnclosingConcept());
		appDb.getDB().commit();
	}

	private void retireConcept(ConceptVersionBI concept) throws IOException, ContradictionException, InvalidCAB
	{
		ConceptCB blueprint = concept.makeBlueprint(vc, IdDirective.PRESERVE, RefexDirective.INCLUDE);
		blueprint.setRetired();
		ConceptChronicleBI cc = appDb.getBuilder().constructIfNotCurrent(blueprint);
		appDb.getDB().addUncommitted(cc);
		appDb.getDB().commit();
		
	}

	private void retireRelationship(RelationshipVersionBI<?> rel) throws IOException, InvalidCAB, ContradictionException
	{
		RelationshipCAB relCab = rel.makeBlueprint(vc, IdDirective.PRESERVE, RefexDirective.INCLUDE);
		relCab.setRetired();
		RelationshipChronicleBI relcbi = appDb.getBuilder().constructIfNotCurrent(relCab);
		appDb.getDB().addUncommitted(relcbi.getEnclosingConcept());
		appDb.getDB().commit();
	}
	
	public static void main(String[] args) throws MojoExecutionException
	{
		new ComponentRetire().execute();
	}
}
