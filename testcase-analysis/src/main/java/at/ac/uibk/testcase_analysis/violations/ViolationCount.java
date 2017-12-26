package at.ac.uibk.testcase_analysis.violations;

public class ViolationCount {
	private int featureCount = 0;
	private int violationCount = 0;
	
	public ViolationCount(){
	}
	
	public ViolationCount(int featureCount, int violationCount){
		this.featureCount = featureCount;
		this.violationCount = violationCount;
	}
	
	public void addViolationsCount(int num){
		featureCount ++;
		violationCount += num;
	}
	
	public void increaseFeatureCount(){
		featureCount++;
	}
	
	public void increaseViolationCount(){
		violationCount++;
	}
	
	public void addViolations(int num){
		violationCount += num;
	}
	
	public int getFeatureCount(){
		return featureCount;
	}
	
	public int getViolationCount(){
		return violationCount;
	}
}
