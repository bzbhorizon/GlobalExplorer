package bzb.se;

import bzb.se.installations.Multi;
import bzb.se.meta.Packages;

public class Main {

	public static void main(String[] args) {
		
		try {
			if (args[0].equals("clean")) {
				System.out.println("Cleaning DB");
			} else if (args[0] != null) {
				Multi m = (Multi) Class.forName(Packages.multiPackage + "." + args[0]).newInstance();
			}
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

}
