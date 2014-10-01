package model;

public class TripletRelation {
	private String arg1 = ""; // subject
	private String relation = ""; // predicate
	private String arg2 = ""; // object
	private double confidence;

	public TripletRelation() {
	}

	public TripletRelation(String arg1, String relation, String arg2) {
		setArg1(arg1);
		setRelation(relation);
		setArg2(arg2);
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}
	
	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public boolean isComplete() {
		if (arg1.isEmpty() || relation.isEmpty() || arg2.isEmpty())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "arg1 = " + arg1 + "\n" + "relation = " + relation + "\n"
				+ "arg2 = " + arg2 + "\n";
	}
}
