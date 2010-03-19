package bzb.se.adverts;

import java.sql.ResultSet;
import java.sql.SQLException;

import bzb.se.DB;
import bzb.se.exceptions.InstallationNotFoundException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.meta.Requirements;
import bzb.se.visitors.Visitor;

public class Advert implements Comparable {

	public abstract class Priorities {
		static final int STORYLINE = 0;
		static final int OPEN = 1;
		static final int NONINSTALLATION = 2;
		static final int VISITED = 3;
		static final int VISITED_NONINSTALLATION = 4;
		static final int FULL_STORYLINE = 5;
		static final int FULL_OPEN = 6;
		static final int FULL_VISITED = 7;
		static final int UNSUITABLE = 8;
		public static final int NEVER_SUITABLE = -1;
	}

	private String instanceName;

	private boolean partOfStoryline;

	private boolean prerequisitesMet;

	private boolean alreadyVisited;

	private boolean full;

	private boolean compatible;

	private String description;

	public Advert(String instanceName, Visitor visitor, DB db)
			throws InstallationNotFoundException, NoSuchVisitorException {
		this.instanceName = instanceName;

		System.out.println("Checking for installations registered with name "
				+ instanceName);

		try {
			ResultSet rs = db.stmt
					.executeQuery("SELECT * FROM installations WHERE instanceName='"
							+ instanceName + "'");
			if (rs.next()) {
				System.out.println("Installation found with name "
						+ instanceName);

				description = rs.getString("description");

				if (visitor.getCompleteExperiences().contains(instanceName)) {
					System.out.println("Complete experience at " + instanceName
							+ " by " + visitor.getVisitorHost() + " found");
					alreadyVisited = true;
				} else {
					System.out.println("No complete experiences at "
							+ instanceName + " by " + visitor.getVisitorHost()
							+ " found");
					alreadyVisited = false;
				}

				int freeCapacity = rs.getInt("freeCapacity");
				if (freeCapacity > 0) {
					full = false;
					System.out.println(instanceName
							+ " is not full and has space for visitor on "
							+ visitor.getVisitorHost());
				} else {
					full = true;
					System.out
							.println(instanceName
									+ " is full and therefore unsuitable for visitor on "
									+ visitor.getVisitorHost());
				}

				if (rs.getInt("requiresBT") == Requirements.HARD_REQUIRED
						&& !visitor.getVisitorHasBT()) {
					compatible = false;
					System.out.println("Visitor on " + visitor.getVisitorHost()
							+ " does not have the required BT capability for "
							+ instanceName);
				} else if (rs.getInt("requiresFaceCamera") == Requirements.HARD_REQUIRED
						&& !visitor.getVisitorHasFaceCamera()) {
					compatible = false;
					System.out
							.println("Visitor on "
									+ visitor.getVisitorHost()
									+ " does not have the required face camera capability for "
									+ instanceName);
				} else if (rs.getInt("requiresMainCamera") == Requirements.HARD_REQUIRED
						&& !visitor.getVisitorHasMainCamera()) {
					compatible = false;
					System.out
							.println("Visitor on "
									+ visitor.getVisitorHost()
									+ " does not have the required main camera capability for "
									+ instanceName);
				} else {
					compatible = true;
					System.out.println("Visitor on " + visitor.getVisitorHost()
							+ " has the required tech capabilities for "
							+ instanceName);
				}

				ResultSet rs2 = db.stmt
						.executeQuery("SELECT * FROM storylines WHERE instanceName='"
								+ instanceName + "' OR prerequisiteInstanceName='"
								+ instanceName + "'");
				if (rs2.next()) {
					partOfStoryline = true;
					System.out.println(instanceName + " has prerequisites");
				} else {
					partOfStoryline = false;
					System.out.println(instanceName
							+ " does not have prerequisites");
				}

				if (!isPartOfStoryline()) {
					prerequisitesMet = true;
					System.out
							.println(instanceName
									+ " is not part of a storyline hence prerequisites have been met by "
									+ visitor.getVisitorHost());
				} else {
					prerequisitesMet = true;
					rs2 = db.stmt
						.executeQuery("SELECT * FROM storylines WHERE instanceName='"
							+ instanceName + "'");
					while (rs2.next()) {
						String prerequisiteInstanceName = rs2
								.getString("prerequisiteInstanceName");
						System.out.println("Has visitor completed experience at " + prerequisiteInstanceName + "?");
						if (!visitor.getCompleteExperiences().contains(
								prerequisiteInstanceName)) {
							prerequisitesMet = false;
							System.out.println(prerequisiteInstanceName
									+ " not yet visited by "
									+ visitor.getVisitorHost()
									+ "; setting prereqisitesMet to false");
							break;
						} else {
							System.out.println("Visitor has completed required experience at " + prerequisiteInstanceName);
						}
					}
				}
			} else {
				throw (new InstallationNotFoundException());
			}
		} catch (SQLException e) {
			throw (new InstallationNotFoundException());
		}
	}

	public Advert(String instanceName, boolean partOfStoryline,
			boolean prerequisitesMet, boolean alreadyVisited, boolean full,
			boolean compatible, String description) {
		this.instanceName = instanceName;
		this.partOfStoryline = partOfStoryline;
		this.prerequisitesMet = prerequisitesMet;
		this.alreadyVisited = alreadyVisited;
		this.full = full;
		this.compatible = compatible;
		this.description = description;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public int getPriority() {
		int priority;
		if (prerequisitesHaveBeenMet() && isCompatible()) {
			if (hasAlreadyBeenVisited()) {
				if (isFull()) {
					priority = Priorities.FULL_VISITED;
				} else {
					priority = Priorities.VISITED;
				}
			} else {
				if (isFull()) {
					if (isPartOfStoryline()) {
						priority = Priorities.FULL_STORYLINE;
					} else {
						priority = Priorities.FULL_OPEN;
					}
				} else {
					if (isPartOfStoryline()) {
						priority = Priorities.STORYLINE;
					} else {
						priority = Priorities.OPEN;
					}
				}
			}
		} else {
			if (!isCompatible()) {
				priority = Priorities.NEVER_SUITABLE;
			} else {
				priority = Priorities.UNSUITABLE;
			}
		}
		return priority;
	}

	public boolean isPartOfStoryline() {
		return partOfStoryline;
	}

	public boolean prerequisitesHaveBeenMet() {
		return prerequisitesMet;
	}

	public boolean hasAlreadyBeenVisited() {
		return alreadyVisited;
	}

	public boolean isFull() {
		return full;
	}

	public boolean isCompatible() {
		return compatible;
	}

	public boolean isSuitable() {
		if (prerequisitesHaveBeenMet() && !isFull() && isCompatible()) {
			return true;
		} else {
			return false;
		}
	}

	public int compareTo(Object twin) {
		Advert temp = (Advert) twin;
		return new Integer(getPriority()).compareTo(new Integer(temp
				.getPriority()));
	}
	
	public String getDescription () {
		return description;
	}

}
