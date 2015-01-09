package neo4JUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import model.TripletRelation;

import org.apache.commons.lang.StringEscapeUtils;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

public class Neo4JDb {
	private final static String PROP_NAME = "value";
	private final static String PROP_CONFIDENCE = "conf";
	private final static String PROP_OCCURRENCES = "occurrences";
	private final static String LAB_SUBJECT = "subject";
	private final static String LAB_PREDICATE = "predicate";
	private final static String LAB_OBJECT = "object";

	protected GraphDatabaseService graphDb;

	private static enum RelTypes implements RelationshipType {
		RELATES
	}

	public Neo4JDb(String dbUrl) {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbUrl);
		registerShutdownHook(graphDb);
	}

	// TODO define relation for pronouns
	public void insertTriplet(TripletRelation triplet, boolean mergeSubject) {
		final Label label_sub = DynamicLabel.label(LAB_SUBJECT);
		final Label label_obj = DynamicLabel.label(LAB_OBJECT);

		Node arg1 = null;
		if (mergeSubject) {
			arg1 = getNode(null, PROP_NAME, triplet.getArg1()
					.toLowerCase());
		}

		try (Transaction tx = graphDb.beginTx()) {
			if (arg1 == null) {
				arg1 = graphDb.createNode(label_sub);
				arg1.setProperty(PROP_OCCURRENCES, 1);
				arg1.setProperty(PROP_NAME, triplet.getArg1().toLowerCase());
			} else {
				int occurences = (Integer) arg1.getProperty(PROP_OCCURRENCES) + 1;
				arg1.setProperty(PROP_OCCURRENCES, occurences);
			}
			Node arg2 = graphDb.createNode(label_obj);
			arg2.setProperty(PROP_OCCURRENCES, 1);
			arg2.setProperty(PROP_NAME, triplet.getArg2().toLowerCase());

			Relationship relationship = arg1.createRelationshipTo(arg2,
					RelTypes.RELATES);
			relationship.setProperty(PROP_NAME, triplet.getRelation()
					.toLowerCase());
			//confidence
			relationship.setProperty(PROP_CONFIDENCE, triplet.getConfidence());
			tx.success();
		}
	}

	// NOTE: here we return a node supposed to be unique by label, key and value
	// null if the nodeList is empty
	private Node getNode(Label label, String key, Object value) {
		Node node = null;
		try (Transaction tx = graphDb.beginTx()) {
			ResourceIterator<Node> nodes = null;
			if (label != null){
				nodes = graphDb.findNodesByLabelAndProperty(
					label, key, value).iterator();
			}
			else {
				String validValue = StringEscapeUtils.escapeJavaScript((String) value);
				ExecutionEngine engine = new ExecutionEngine(graphDb);
				nodes = engine.execute(
						"START n=node(*)"
						+ " WHERE n." + key + "=\"" + validValue + "\""
						+ " RETURN n").columnAs("n");
				
			}
			if (nodes.hasNext()) {
				node = nodes.next();
			}
			nodes.close();
		}
		return node;
	}

	public void createIndexes() {
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			schema.indexFor(DynamicLabel.label(LAB_SUBJECT))
					.on(PROP_NAME).create();
			tx.success();
		}
	}

	public GraphDatabaseService getDb() {
		return graphDb;
	}

	public void shutdown() {
		graphDb.shutdown();
	}

	public void writeOutContent(String filename) {
		GlobalGraphOperations ops = GlobalGraphOperations.at(graphDb);
		ExecutionEngine engine = new ExecutionEngine(graphDb);

		try (FileWriter writer = new FileWriter(filename);
				Transaction tx = graphDb.beginTx()) {
			for (Node n : ops.getAllNodes()) {
				writer.write("[" + n.getId() + "," + n.getProperty(PROP_NAME)
						+ ",[");
				Iterator<Node> connected = engine.execute(
						"START s=node(" + n.getId()
								+ ") MATCH s-[r]->n RETURN n").columnAs("n");
				for (Node e : IteratorUtil.asIterable(connected)) {
					Iterator<String> rel = engine.execute(
							"START s=node(" + n.getId() + "), e=node("
									+ e.getId()
									+ ") MATCH s-[r]->e RETURN r.value")
							.columnAs("r.value");
					String relVal = rel.hasNext()? rel.next() : "";
					writer.write("[" + e.getId() + ","
							+ relVal + "],");
				}
				writer.write("]]\n");
			}
			tx.success();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public TripletRelation getRelation(long id){
		TripletRelation t = new TripletRelation();
		try (Transaction tx = graphDb.beginTx()) {
			Relationship rel = graphDb.getRelationshipById(id);
			t.setArg1(rel.getStartNode().getProperty(PROP_NAME).toString());
			t.setRelation(rel.getProperty(PROP_NAME).toString());
			t.setArg2(rel.getEndNode().getProperty(PROP_NAME).toString());
			t.setConfidence(Double.valueOf(rel.getProperty(PROP_CONFIDENCE).toString()));
		}
		return t;
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
