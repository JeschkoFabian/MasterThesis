package at.ac.uibk.testcase_gatherer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RepositoryContext implements Serializable{
	private static final long serialVersionUID = -8640570807969462706L;
	
	private Integer id;
	private String name;
	private String pushedAt;
	private String updatedAt;
	private List<String> languages = new ArrayList<>();
	private List<FeatureContext> features = new ArrayList<>();
	
	public RepositoryContext(){
	}

	public RepositoryContext(Integer id, String name, String pushedAt, String updatedAt){
		this.id = id;
		this.name = name;
		this.pushedAt = pushedAt;
		this.updatedAt = updatedAt;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPushedAt() {
		return pushedAt;
	}
	public void setPushedAt(String pushedAt) {
		this.pushedAt = pushedAt;
	}
	public String getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	public List<String> getLanguages() {
		return languages;
	}
	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	public List<FeatureContext> getFeatures() {
		return features;
	}
	public void setFeatures(List<FeatureContext> features) {
		this.features = features;
	}
}
