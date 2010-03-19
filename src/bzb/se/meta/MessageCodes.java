package bzb.se.meta;

public final class MessageCodes {

	public final class In {
		
		// reserve messageCodes 0-99 for multi
		public final class Multi {
			public static final int ASSIGN_INSTANCE = 0;
		
			// reserve messageCodes 100-199 for visitor monitor multi
			
			// reserve messageCodes 200-299 for specific multis
		}
		
		// reserve messageCodes 300-399 for instance
		public final class Instance {
			public static final int ACTIVITY = 300;
			public static final int PULL_DIRECTIONS = 301;
			public static final int COMPLETED_EXPERIENCE = 302;
			public static final int COUPLED = 303;
			public static final int DECOUPLED = 304;
			public static final int ARRIVED = 305;
			public static final int LEFT = 306;
			public static final int DECOUPLING = 307;
		
			// reserve messageCodes 400-499 for visitor monitor instance
			public final class VisitorMonitor {
				public static final int REGISTER_CAPABILITY_PROFILE = 400;
				public static final int LOCATE_INSTALLATION = 401;
				public static final int PULL_ADVERTS = 402;
				public static final int PULL_DIRECTIONS = 403;
				public static final int PULL_POIS = 404;
				public static final int UPDATE_LOCATION = 405;
				public static final int PULL_FRIENDS = 406;
			}
			
			// reserve messageCodes 500-599 for specific instances
		}
	}
	
	public final class Out {
	
		// reserve messageCodes 0-99 for multi
		public final class Multi {	
			public static final int INSTANCE_PORT = 0; // message is tcp server port number of instance
			public static final int NO_FREE_INSTANCE = 1;
		
			// reserve messageCodes 100-199 for visitor monitor
			
			// reserve messageCodes 200-299 for specific multis
		}
		
		// reserve messageCodes 300-399 for instance
		public final class Instance {
			public static final int COUPLED = 300;
			public static final int DECOUPLED = 301;
			public static final int COMPLETED_EXPERIENCE = 302;
			
			// reserve messageCodes 400-499 for visitor monitor
			public final class VisitorMonitor {
				public static final int INSTALLATION_LOCATION = 400;
				public static final int LOCATION_UNKNOWN = 401;
			}
			
			// reserve messageCodes 500-599 for specific instances
		}
	}	
}