package models;

import java.util.*;

import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Tag extends Model implements Comparable<Tag> {
	
	public String name;
	
	// Lazy creation
	public static Tag findOrCreateByName(String name) {
		Tag tag = find("byName", name).first();
		if(tag == null) {
			tag = new Tag(name);
		}
		return tag;
	}
	
	private Tag(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(Tag otherTag) {
		return name.compareTo(otherTag.name);
	}
	
	public static List<Map> getCloud() {
		List<Map> result = find(
			"select new map(t.name as tag, count(p.id) as pound) from Post p join p.tags as t group by t.name order by t.name"
		).fetch();
		return result;
	}
	
}